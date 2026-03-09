package com.rudra.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class ReviewPageForm {
  @NotBlank
  @Size(max = 50)
  private String language;

  @Size(max = 200_000)
  private String code;

  private MultipartFile file;

  public ReviewPageForm() {}

  public ReviewPageForm(String language, String code) {
    this.language = language;
    this.code = code;
  }

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
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

