package com.sai.airesumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Column(columnDefinition = "LONGTEXT")
    private String detectedSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String missingSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String improvementAdvice; // <-- NEW FIELD

    private int score;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDetectedSkills() { return detectedSkills; }
    public void setDetectedSkills(String detectedSkills) { this.detectedSkills = detectedSkills; }

    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }

    public String getImprovementAdvice() { return improvementAdvice; }
    public void setImprovementAdvice(String improvementAdvice) { this.improvementAdvice = improvementAdvice; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}