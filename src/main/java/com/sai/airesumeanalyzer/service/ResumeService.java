package com.sai.airesumeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.airesumeanalyzer.entity.ResumeAnalysis;
import com.sai.airesumeanalyzer.repository.ResumeAnalysisRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ResumeService {

    @Value("${gemini.api.key:MISSING_KEY}")
    private String apiKey;

    private final WebClient webClient;
    private final ResumeAnalysisRepository repository;
    private final ObjectMapper objectMapper;

    public ResumeService(WebClient.Builder webClientBuilder, 
                         ResumeAnalysisRepository repository, 
                         ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public ResumeAnalysis processResume(MultipartFile file) throws IOException {
        String activeKey = apiKey != null ? apiKey.trim() : "";
        if (activeKey.isEmpty() || "MISSING_KEY".equals(activeKey)) {
            throw new IllegalArgumentException("Gemini API key is missing. Set 'gemini.api.key' in application.properties.");
        }

        // 1. Extract text out of PDF using Apache PDFBox
        String parsedText;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            parsedText = stripper.getText(document);
        }

        if (parsedText == null || parsedText.isBlank()) {
            throw new IllegalArgumentException("Failed to extract readable text from the provided PDF.");
        }

        // 2. Build Request Body with Structured JSON Generation Output
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", "Analyze the following resume and return the analysis according to the specified JSON schema.\n\nResume Text:\n" + parsedText)
                ))
            ),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", Map.of(
                    "type", "OBJECT",
                    "properties", Map.of(
                        "detectedSkills", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                        "missingSkills", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                        "score", Map.of("type", "INTEGER"),
                        "improvementAdvice", Map.of("type", "STRING")
                    ),
                    "required", List.of("detectedSkills", "missingSkills", "score", "improvementAdvice")
                )
            )
        );

        // 3. Call Gemini REST API using query parameter authentication
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + activeKey;

        try {
            Map<?, ?> response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String rawJsonContent = extractTextFromResponse(response);
            JsonNode rootNode = objectMapper.readTree(rawJsonContent);

            // 4. Map JSON fields into the Database Entity
            ResumeAnalysis analysis = new ResumeAnalysis();
            analysis.setFileName(file.getOriginalFilename());
            analysis.setDetectedSkills(rootNode.get("detectedSkills").toString());
            analysis.setMissingSkills(rootNode.get("missingSkills").toString());
            analysis.setScore(rootNode.get("score").asInt());

            // Map improvement advice text safely
            if (rootNode.has("improvementAdvice") && !rootNode.get("improvementAdvice").isNull()) {
                analysis.setImprovementAdvice(rootNode.get("improvementAdvice").asText());
            }

            return repository.save(analysis);

        } catch (WebClientResponseException e) {
            System.err.println("Gemini API Response Error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API Error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API processing failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<?, ?> response) {
        if (response != null && response.containsKey("candidates")) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (!candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (!parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        }
        throw new IllegalStateException("Failed to extract text from Gemini API response payload.");
    }
}