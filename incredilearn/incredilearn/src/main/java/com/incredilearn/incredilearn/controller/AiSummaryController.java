package com.incredilearn.incredilearn.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incredilearn.incredilearn.dto.SummarizeRequest;
import com.incredilearn.incredilearn.service.AiSummaryService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    public AiSummaryController(AiSummaryService aiSummaryService) {
        this.aiSummaryService = aiSummaryService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarize(@RequestBody SummarizeRequest request) {
        String summary = aiSummaryService.generateSummary(
            request.getContentIds(),
            request.getLessonDescriptions()
        );
        return ResponseEntity.ok(Map.of("summary", summary));
    }
}
