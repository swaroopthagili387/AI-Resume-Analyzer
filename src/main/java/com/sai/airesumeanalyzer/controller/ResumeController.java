package com.sai.airesumeanalyzer.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sai.airesumeanalyzer.service.ResumeService;

@RestController
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file) throws IOException {

        // Just pass file to service (ALL LOGIC INSIDE SERVICE)
        return resumeService.processResume(file);
    }
}