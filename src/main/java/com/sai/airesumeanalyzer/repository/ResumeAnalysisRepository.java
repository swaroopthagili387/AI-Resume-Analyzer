package com.sai.airesumeanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sai.airesumeanalyzer.entity.ResumeAnalysis;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    // Standard CRUD operations (like .save()) are automatically provided
}