package com.copilot.backend.service;



import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmbeddingService {

    private final RestClient restClient;

    public EmbeddingService() {

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public List<Double> generateEmbedding(String text) {

        OllamaEmbeddingRequest request =
                new OllamaEmbeddingRequest(
                        "all-minilm",
                        text
                );

        OllamaEmbeddingResponse response =
                restClient.post()
                        .uri("/api/embed")
                        .body(request)
                        .retrieve()
                        .body(OllamaEmbeddingResponse.class);

        return response.embeddings().get(0);
    }

    private record OllamaEmbeddingRequest(
            String model,
            String input
    ) {}

    private record OllamaEmbeddingResponse(
            List<List<Double>> embeddings
    ) {}
    
    
  
    
}



