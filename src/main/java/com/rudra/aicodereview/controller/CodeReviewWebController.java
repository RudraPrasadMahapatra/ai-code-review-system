package com.rudra.aicodereview.controller;

import com.rudra.aicodereview.dto.CodeReviewRequestDto;
import com.rudra.aicodereview.dto.CodeReviewResponseDto;
import com.rudra.aicodereview.dto.ReviewPageForm;
import com.rudra.aicodereview.service.AiCodeReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.rudra.aicodereview.repository.CodeReviewRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/review")
public class CodeReviewWebController {
  private final AiCodeReviewService aiReviewService;
  private final CodeReviewRepository repository;
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());

  public CodeReviewWebController(AiCodeReviewService aiReviewService, CodeReviewRepository repository) {
    this.aiReviewService = aiReviewService;
    this.repository = repository;
  }

  @ModelAttribute("languages")
  public List<String> languages() {
    return List.of("java", "kotlin", "python", "javascript", "typescript", "go", "rust", "csharp", "cpp", "ruby");
  }

  @GetMapping
  public String page(Model model) {
    if (!model.containsAttribute("form")) {
      model.addAttribute("form", new ReviewPageForm("java", ""));
    }
    return "review";
  }

  @Transactional(readOnly = true)
  @GetMapping("/history")
  public String history(Model model) {
    var records = repository.findAllByOrderByCreatedAtDesc();
    var reviews = records.stream().map(r -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", r.getId());
      map.put("language", r.getLanguage());
      map.put("createdAt", r.getCreatedAt() != null ? DATE_FMT.format(r.getCreatedAt()) : "N/A");
      map.put("score", r.getScore());
      map.put("findingsCount", r.getFindings() != null ? r.getFindings().size() : 0);
      map.put("timeComplexity", r.getTimeComplexity());
      map.put("spaceComplexity", r.getSpaceComplexity());
      map.put("status", r.getStatus() != null ? r.getStatus().name() : "UNKNOWN");
      map.put("tokensUsed", r.getTokensUsed());
      return map;
    }).toList();

    int totalCalls = records.size();
    long totalTokens = records.stream().mapToLong(r -> r.getTokensUsed() != null ? r.getTokensUsed() : 0).sum();
    long avgTokens = totalCalls > 0 ? (totalTokens / totalCalls) : 0;
    double estCost = totalTokens * 0.002 / 1000.0; // Assume $0.002 per 1K tokens

    model.addAttribute("totalCalls", totalCalls);
    model.addAttribute("avgTokens", avgTokens);
    model.addAttribute("estCost", String.format("%.4f", estCost));

    model.addAttribute("reviews", reviews);
    return "history";
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String submit(
      @Valid @ModelAttribute("form") ReviewPageForm form, BindingResult bindingResult, Model model) {
    
    if (form.getFile() != null && !form.getFile().isEmpty()) {
      try {
        String fileContent = new String(form.getFile().getBytes(), StandardCharsets.UTF_8);
        form.setCode(fileContent);
      } catch (IOException e) {
        model.addAttribute("reviewError", "Failed to read uploaded file.");
        return "review";
      }
    }

    if (form.getCode() == null || form.getCode().trim().isEmpty()) {
      model.addAttribute("reviewError", "Please provide code either via text editor or file upload.");
      return "review";
    }

    if (bindingResult.hasErrors()) {
      return "review";
    }

    try {
      CodeReviewResponseDto response = aiReviewService.review(new CodeReviewRequestDto(form.getLanguage(), form.getCode()));
      model.addAttribute("reviewResponse", response);
    } catch (RuntimeException ex) {
      model.addAttribute("reviewError", ex.getMessage() == null ? "Review failed." : ex.getMessage());
    }

    return "review";
  }
}

