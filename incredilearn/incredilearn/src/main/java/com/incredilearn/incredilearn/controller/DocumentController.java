package com.incredilearn.incredilearn.controller;

import com.incredilearn.incredilearn.model.UserDocument;
import com.incredilearn.incredilearn.repository.UserDocumentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class DocumentController {

    private final UserDocumentRepository documentRepository;

    public DocumentController(UserDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /** Upload one or more files for a user */
    @PostMapping("/upload/{userId}")
    public ResponseEntity<Void> uploadDocuments(
            @PathVariable Long userId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            UserDocument doc = new UserDocument();
            doc.setUserId(userId);
            doc.setFileName(file.getOriginalFilename());
            doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            doc.setData(file.getBytes());
            documentRepository.save(doc);
        }
        return ResponseEntity.ok().build();
    }

    /** List document metadata for a user (no binary data) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserDocuments(@PathVariable Long userId) {
        List<Map<String, Object>> docs = documentRepository.findByUserId(userId).stream()
            .map(d -> Map.<String, Object>of(
                "id", d.getId(),
                "fileName", d.getFileName(),
                "contentType", d.getContentType(),
                "uploadedAt", d.getUploadedAt().toString()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(docs);
    }

    /** Download / view a specific document */
    @GetMapping("/{docId}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long docId) {
        UserDocument doc = documentRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
            .contentType(MediaType.parseMediaType(doc.getContentType()))
            .body(doc.getData());
    }
}
