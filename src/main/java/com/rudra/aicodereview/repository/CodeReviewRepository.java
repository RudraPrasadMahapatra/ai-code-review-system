package com.rudra.aicodereview.repository;

import com.rudra.aicodereview.entity.CodeReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeReviewRepository extends JpaRepository<CodeReviewRecord, Long> {}

