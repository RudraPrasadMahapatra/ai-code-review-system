package com.example.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewPageForm {
  @NotBlank
  @Size(max = 50)
  private String language;

  @NotBlank
  @Size(max = 200_000)
  private String code;

  public ReviewPageForm() {}

  public ReviewPageForm(String language, String code) {
    this.language = language;
    this.code = code;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}

