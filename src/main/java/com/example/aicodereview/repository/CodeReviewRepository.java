package com.example.aicodereview.repository;

import com.example.aicodereview.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {}

