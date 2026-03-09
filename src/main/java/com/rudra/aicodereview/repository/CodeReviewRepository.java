package com.rudra.aicodereview.repository;

import com.rudra.aicodereview.entity.CodeReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeReviewRepository extends JpaRepository<CodeReviewRecord, Long> {
    List<CodeReviewRecord> findAllByOrderByCreatedAtDesc();
    
    CodeReviewRecord findFirstByContentHash(String contentHash);
}

