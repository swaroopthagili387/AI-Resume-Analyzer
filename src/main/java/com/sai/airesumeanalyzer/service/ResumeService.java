package com.sai.airesumeanalyzer.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sai.airesumeanalyzer.entity.ResumeAnalysis;
import com.sai.airesumeanalyzer.repository.ResumeAnalysisRepository;

@Service
public class ResumeService {

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    public String processResume(MultipartFile file) throws IOException {

        // 1. Extract text from PDF
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        // 2. Skills list
        String[] skills = {
            "Java",
            "Python",
            "MySQL",
            "Git",
            "Spring Boot",
            "REST API",
            "Maven",
            "Hibernate"
        };

        String detected = "Detected Skills:\n\n";
        String missing = "\nMissing Skills:\n\n";
        String suggestions = "\nSuggestions:\n\n";

        int foundSkills = 0;

        // 3. Analyze skills
        for (String skill : skills) {

            if (text.contains(skill)) {
                detected += "✓ " + skill + "\n";
                foundSkills++;
            } else {
                missing += "✗ " + skill + "\n";

                if (skill.equals("Spring Boot")) {
                    suggestions += "• Build Spring Boot project\n";
                } else if (skill.equals("REST API")) {
                    suggestions += "• Create REST API project\n";
                } else if (skill.equals("Maven")) {
                    suggestions += "• Use Maven in projects\n";
                } else if (skill.equals("Hibernate")) {
                    suggestions += "• Use Hibernate with MySQL\n";
                }
            }
        }

        // 4. Score calculation
        int score = (foundSkills * 100) / skills.length;

        // 5. SAVE TO DATABASE
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setFileName(file.getOriginalFilename());
        analysis.setDetectedSkills(detected);
        analysis.setMissingSkills(missing);
        analysis.setScore(score);

        resumeAnalysisRepository.save(analysis);

        // 6. Response
        return "Resume Score: " + score + "/100<br><br>"
                + detected.replace("\n", "<br>")
                + missing.replace("\n", "<br>")
                + suggestions.replace("\n", "<br>");
    }
}
