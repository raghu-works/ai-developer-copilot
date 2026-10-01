package com.copilot.backend.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;



@Service
public class VectorService {

    private final JdbcTemplate postgresJdbcTemplate;
    private final EmbeddingService embeddingService;

    public VectorService(
            JdbcTemplate postgresJdbcTemplate,
            EmbeddingService embeddingService) {

        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.embeddingService = embeddingService;
    }

    // Test PostgreSQL connection
    public Integer testPostgresConnection() {

        return postgresJdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );
    }

    // Save code embedding into PostgreSQL
    public void saveEmbedding(
            String code,
            String language,
            String operation) {

        // 1. Generate embedding using Ollama
        List<Double> embedding =
                embeddingService.generateEmbedding(code);

        // 2. Convert embedding to pgvector format
        String vector = embedding.toString();

        // 3. SQL query to save the embedding
        String sql = """
                INSERT INTO code_embeddings
                (code, language, operation, embedding)
                VALUES (?, ?, ?, ?::vector)
                """;

        // 4. Execute INSERT
        postgresJdbcTemplate.update(
                sql,
                code,
                language,
                operation,
                vector
        );

        System.out.println("=================================");
        System.out.println("Embedding saved successfully!");
        System.out.println("Embedding size: " + embedding.size());
        System.out.println("=================================");
    }

    // Search for similar code
    public List<String> searchSimilarCode(String query) {

        // 1. Generate embedding for the search query
        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(query);

        // 2. Convert embedding to pgvector format
        String vector = queryEmbedding.toString();

        // 3. SQL similarity search
        String sql = """
                SELECT
                    id,
                    code,
                    language,
                    operation,
                    embedding <=> ?::vector AS distance
                FROM code_embeddings
                ORDER BY distance
                LIMIT 5
                """;

        // 4. Execute similarity search
        List<String> results =
                postgresJdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                "ID: " + rs.getLong("id")
                                + " | Code: " + rs.getString("code")
                                + " | Language: " + rs.getString("language")
                                + " | Operation: " + rs.getString("operation")
                                + " | Distance: " + rs.getDouble("distance"),
                        vector
                );

        // 5. Display results
        System.out.println("=================================");
        System.out.println("Similar Code Results:");

        for (String result : results) {
            System.out.println(result);
        }

        System.out.println("=================================");
        return results;
    }

  
}
