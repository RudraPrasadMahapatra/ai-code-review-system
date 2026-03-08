package com.rudra.aicodereview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_findings")
public class CodeIssue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "code_review_id", nullable = false)
  private CodeReviewRecord codeReview;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Severity severity;

  @Column(length = 100)
  private String ruleId;

  @Column(nullable = false, length = 1000)
  private String message;

  private Integer lineStart;
  private Integer lineEnd;

  @Lob
  private String suggestion;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CodeReviewRecord getCodeReview() {
    return codeReview;
  }

  public void setCodeReview(CodeReviewRecord codeReview) {
    this.codeReview = codeReview;
  }

  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getLineStart() {
    return lineStart;
  }

  public void setLineStart(Integer lineStart) {
    this.lineStart = lineStart;
  }

  public Integer getLineEnd() {
    return lineEnd;
  }

  public void setLineEnd(Integer lineEnd) {
    this.lineEnd = lineEnd;
  }

  public String getSuggestion() {
    return suggestion;
  }

  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }
}

