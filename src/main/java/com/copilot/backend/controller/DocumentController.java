package com.copilot.backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.copilot.backend.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public List<String> uploadDocument(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        return documentService.extractAndChunkText(file);
    }

    @GetMapping("/search")
    public List<String> searchDocuments(
            @RequestParam("query") String query) {

        return documentService.searchSimilarDocuments(query);
    }
}