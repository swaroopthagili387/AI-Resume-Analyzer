package com.sai.airesumeanalyzer.controller;

import com.sai.airesumeanalyzer.entity.ResumeAnalysis;
import com.sai.airesumeanalyzer.service.ResumeService;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Please select a file to upload.");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Error: Only PDF documents are supported.");
        }

        try {
            // Updated to hold the ResumeAnalysis object returned by the service
            ResumeAnalysis analysis = resumeService.processResume(file);
            return ResponseEntity.ok(analysis);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading the PDF file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI Analysis Error: " + e.getMessage());
        }
    }
}