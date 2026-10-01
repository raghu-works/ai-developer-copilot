package com.copilot.backend.service;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final TextChunkService textChunkService;
    private final JdbcTemplate postgresJdbcTemplate;
    private final EmbeddingService embeddingService;

    public DocumentService(
            TextChunkService textChunkService,
            JdbcTemplate postgresJdbcTemplate,
            EmbeddingService embeddingService) {

        this.textChunkService = textChunkService;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.embeddingService = embeddingService;
    }

    public List<String> extractAndChunkText(MultipartFile file)
            throws IOException {

        byte[] pdfBytes = file.getBytes();

        String extractedText;

        try (var document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            extractedText = stripper.getText(document);
        }

        List<String> chunks =
                textChunkService.splitText(
                        extractedText,
                        800
                );

        String documentName = file.getOriginalFilename();

        String sql = """
                INSERT INTO document_chunks
                (document_name, chunk_text, embedding)
                VALUES (?, ?, ?::vector)
                """;

        for (String chunk : chunks) {

            List<Double> embedding =
                    embeddingService.generateEmbedding(chunk);

            String vector = embedding.toString();

            postgresJdbcTemplate.update(
                    sql,
                    documentName,
                    chunk,
                    vector
            );

            System.out.println(
                    "Embedding generated for chunk. Size: "
                    + embedding.size()
            );
        }

        System.out.println("=================================");
        System.out.println("PDF chunks and embeddings saved!");
        System.out.println("Document: " + documentName);
        System.out.println("Number of chunks: " + chunks.size());
        System.out.println("=================================");

        return chunks;
    }

    public List<String> searchSimilarDocuments(String query) {

        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(query);

        String vector = queryEmbedding.toString();

        String sql = """
                SELECT
                    id,
                    document_name,
                    chunk_text,
                    embedding <=> ?::vector AS distance
                FROM document_chunks
                WHERE embedding IS NOT NULL
                ORDER BY distance
                LIMIT 5
                """;

        List<String> results =
                postgresJdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                "[SOURCE] "
                                + "Document: " + rs.getString("document_name")
                                + " | Chunk ID: " + rs.getLong("id")
                                + " | Distance: " + rs.getDouble("distance")
                                + "\n"
                                + "Content: "
                                + rs.getString("chunk_text"),

                        vector
                );

        System.out.println("=================================");
        System.out.println("Similar Document Results:");

        for (String result : results) {
            System.out.println(result);
        }

        System.out.println("=================================");

        return results;
    
    }
}