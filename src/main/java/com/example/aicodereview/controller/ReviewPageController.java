package com.example.aicodereview.controller;

import com.example.aicodereview.dto.AiReviewRequest;
import com.example.aicodereview.dto.AiReviewResponse;
import com.example.aicodereview.dto.ReviewPageForm;
import com.example.aicodereview.service.AiReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/review")
public class ReviewPageController {
  private final AiReviewService aiReviewService;

  public ReviewPageController(AiReviewService aiReviewService) {
    this.aiReviewService = aiReviewService;
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

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String submit(
      @Valid @ModelAttribute("form") ReviewPageForm form, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      return "review";
    }

    try {
      AiReviewResponse response = aiReviewService.review(new AiReviewRequest(form.getLanguage(), form.getCode()));
      model.addAttribute("reviewResponse", response);
    } catch (RuntimeException ex) {
      model.addAttribute("reviewError", ex.getMessage() == null ? "Review failed." : ex.getMessage());
    }

    return "review";
  }
}

