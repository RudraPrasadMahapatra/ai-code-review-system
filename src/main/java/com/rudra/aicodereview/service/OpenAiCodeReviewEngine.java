package com.rudra.aicodereview.service;

import com.rudra.aicodereview.entity.Severity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import com.rudra.aicodereview.entity.IssueCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "aicodereview.ai", name = "provider", havingValue = "openai")
public class OpenAiCodeReviewEngine implements CodeReviewEngine {
  private static final Logger log = LoggerFactory.getLogger(OpenAiCodeReviewEngine.class);

  private static final String SYSTEM_PROMPT =
      """
      You are a senior software engineer performing a thorough code review.
      You MUST respond with ONLY a single valid JSON object. No markdown, no code fences, no explanations.

      The JSON object MUST have exactly these top-level keys:
      {
        "score": <integer 1-10>,
        "timeComplexity": "<string, e.g. O(N)>",
        "spaceComplexity": "<string, e.g. O(1)>",
        "optimizedCode": "<string containing the complete improved version of the code>",
        "findings": [
          {
            "severity": "ERROR" | "WARNING" | "INFO",
            "category": "BUG" | "SECURITY" | "PERFORMANCE" | "STYLE" | "DOCS" | "OTHER",
            "ruleId": "<short identifier, e.g. NULL_CHECK>",
            "message": "<concise description of the issue>",
            "lineStart": <integer or null>,
            "lineEnd": <integer or null>,
            "suggestion": "<how to fix it>"
          }
        ]
      }

      Rules:
      - "score" is an integer from 1 (terrible) to 10 (excellent).
      - "optimizedCode" must contain the COMPLETE refactored version. No backticks or markdown.
      - Return at most 5 findings, ordered by severity (ERROR first, then WARNING, then INFO).
      - Do NOT wrap the JSON in markdown code fences or add any text outside the JSON object.
      """;

  private static final String USER_PROMPT_TEMPLATE =
      """
      Review the following code.

      Language: {language}
      File path: {filePath}

      Code:
      ```{language}
      {code}
      ```
      """;

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  public OpenAiCodeReviewEngine(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
    this.chatClient = chatClientBuilder.build();
    this.objectMapper = objectMapper;
  }

  @Override
  public ReviewResponse review(String language, String content, String filePath) {
    long start = System.currentTimeMillis();

    ChatResponse chatResponse =
        chatClient
            .prompt()
            .system(SYSTEM_PROMPT)
            .user(
                user ->
                    user.text(USER_PROMPT_TEMPLATE)
                        .param("language", nullToEmpty(language))
                        .param("filePath", nullToEmpty(filePath))
                        .param("code", nullToEmpty(content)))
            .call()
            .chatResponse();

    long executionTimeMs = System.currentTimeMillis() - start;
    log.info("AI review completed in {} ms", executionTimeMs);

    String raw = chatResponse.getResult().getOutput().getText();
    Integer tokens = null;
    if (chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
      tokens = Math.toIntExact(chatResponse.getMetadata().getUsage().getTotalTokens());
      log.info("Tokens used: {}", tokens);
    }

    String json = extractJson(raw);
    if (json.isBlank()) {
      log.warn("AI returned empty/unparseable response");
      return emptyResponse(tokens, executionTimeMs);
    }

    try {
      GptResponse response = objectMapper.readValue(json, GptResponse.class);
      List<Finding> mappedFindings = toFindings(response.findings());
      return new ReviewResponse(
          response.score(),
          response.timeComplexity(),
          response.spaceComplexity(),
          response.optimizedCode(),
          mappedFindings,
          tokens,
          executionTimeMs);
    } catch (Exception ex) {
      log.warn("Failed to parse AI JSON response: {}", ex.getMessage());
      return new ReviewResponse(
          null, null, null, null,
          List.of(
              new Finding(
                  Severity.WARNING,
                  IssueCategory.OTHER,
                  "AI_PARSE",
                  "AI response was not valid JSON in the expected schema.",
                  null,
                  null,
                  truncate(raw, 4000))),
          tokens, executionTimeMs);
    }
  }

  private ReviewResponse emptyResponse(Integer tokens, Long executionTimeMs) {
    return new ReviewResponse(null, null, null, null, Collections.emptyList(), tokens, executionTimeMs);
  }

  private static List<Finding> toFindings(List<GptFinding> gptFindings) {
    if (gptFindings == null || gptFindings.isEmpty()) {
      return Collections.emptyList();
    }

    return gptFindings.stream()
        .filter(f -> f.message() != null && !f.message().isBlank())
        .map(
            f ->
                new Finding(
                    parseSeverity(f.severity()),
                    parseCategory(f.category()),
                    blankToNull(f.ruleId()),
                    f.message().trim(),
                    f.lineStart(),
                    f.lineEnd(),
                    blankToNull(f.suggestion())))
        .toList();
  }

  private static Severity parseSeverity(String severity) {
    if (severity == null) {
      return Severity.INFO;
    }
    return switch (severity.trim().toUpperCase()) {
      case "ERROR" -> Severity.ERROR;
      case "WARNING", "WARN" -> Severity.WARNING;
      default -> Severity.INFO;
    };
  }

  private static IssueCategory parseCategory(String cat) {
    if (cat == null) {
      return IssueCategory.OTHER;
    }
    return switch (cat.trim().toUpperCase()) {
      case "BUG" -> IssueCategory.BUG;
      case "SECURITY" -> IssueCategory.SECURITY;
      case "PERFORMANCE" -> IssueCategory.PERFORMANCE;
      case "STYLE" -> IssueCategory.STYLE;
      case "DOCS", "DOCUMENTATION" -> IssueCategory.DOCS;
      default -> IssueCategory.OTHER;
    };
  }

  private static String extractJson(String raw) {
    if (raw == null) {
      return "";
    }

    String trimmed = raw.trim();

    if (trimmed.startsWith("```") || trimmed.startsWith("```json")) {
      int firstNewline = trimmed.indexOf('\n');
      if (firstNewline >= 0) {
        trimmed = trimmed.substring(firstNewline + 1);
      }

      int lastFence = trimmed.lastIndexOf("```");
      if (lastFence >= 0) {
        trimmed = trimmed.substring(0, lastFence);
      }

      trimmed = trimmed.trim();
    }

    int objStart = trimmed.indexOf('{');
    int arrStart = trimmed.indexOf('[');
    int start;
    if (objStart < 0) {
      start = arrStart;
    } else if (arrStart < 0) {
      start = objStart;
    } else {
      start = Math.min(objStart, arrStart);
    }
    if (start < 0) {
      return "";
    }
    trimmed = trimmed.substring(start).trim();

    int objEnd = trimmed.lastIndexOf('}');
    int arrEnd = trimmed.lastIndexOf(']');
    int end = Math.max(objEnd, arrEnd);
    if (end < 0) {
      return "";
    }
    return trimmed.substring(0, end + 1).trim();
  }

  private static String truncate(String value, int maxChars) {
    if (value == null) {
      return null;
    }
    if (value.length() <= maxChars) {
      return value;
    }
    return value.substring(0, maxChars) + "...(truncated)";
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record GptResponse(
      Integer score,
      String timeComplexity,
      String spaceComplexity,
      String optimizedCode,
      List<GptFinding> findings) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record GptFinding(
      String severity,
      String category,
      String ruleId,
      String message,
      Integer lineStart,
      Integer lineEnd,
      String suggestion) {}
}
