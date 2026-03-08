package com.rudra.aicodereview.service;

import com.rudra.aicodereview.entity.Severity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "aicodereview.ai", name = "provider", havingValue = "openai")
public class OpenAiCodeReviewEngine implements CodeReviewEngine {
  private static final String SYSTEM_PROMPT =
		  """
		  You are a senior software engineer performing a code review.

		  Return ONLY valid JSON. Do not include explanations, text, markdown, or code fences.

		  Output must strictly match one of these formats:

		  1) {"findings":[{...}]}
		  2) [{...}]

		  Each finding must include:
		  - severity: INFO, WARNING, or ERROR
		  - ruleId: optional string
		  - message: required string
		  - lineStart: integer or null
		  - lineEnd: integer or null
		  - suggestion: optional string

		  Return maximum 5 findings ordered by severity (ERROR first).
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
  public List<Finding> review(String language, String content, String filePath) {

	    long start = System.currentTimeMillis();

	    String raw =
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
	            .content();

	    long end = System.currentTimeMillis();
	    System.out.println("AI review time: " + (end - start) + " ms");

	    String json = extractJson(raw);
	    if (json.isBlank()) {
	      return Collections.emptyList();
	    }

	    try {
	      if (json.startsWith("[")) {
	        List<GptFinding> findings = objectMapper.readValue(json, new TypeReference<List<GptFinding>>() {});
	        return toFindings(findings);
	      }

	      GptResponse response = objectMapper.readValue(json, GptResponse.class);
	      return toFindings(response.findings());
	    } catch (Exception ex) {
	      return List.of(
	          new Finding(
	              Severity.WARNING,
	              "AI_PARSE",
	              "AI response was not valid JSON in the expected schema.",
	              null,
	              null,
	              truncate(raw, 4000)));
	    }
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
  private record GptResponse(List<GptFinding> findings) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record GptFinding(
      String severity,
      String ruleId,
      String message,
      Integer lineStart,
      Integer lineEnd,
      String suggestion) {}
}
