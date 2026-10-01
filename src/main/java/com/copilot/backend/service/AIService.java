package com.copilot.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.copilot.backend.dto.CodeRequest;
import com.copilot.backend.entity.ChatSession;
import com.copilot.backend.entity.CodeRequestEntity;
import com.copilot.backend.repository.ChatSessionRepository;
import com.copilot.backend.repository.CodeRequestRepository;

@Service
public class AIService {

    private final CodeRequestRepository codeRequestRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final RestClient restClient;
    private final VectorService vectorService;
    private final DocumentService documentService;

    public AIService(
            CodeRequestRepository codeRequestRepository,
            ChatSessionRepository chatSessionRepository,
            VectorService vectorService,
            DocumentService documentService) {

        this.codeRequestRepository = codeRequestRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.vectorService = vectorService;
        this.documentService = documentService;

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String processCode(CodeRequest request) {

        // 1. Find chat session
        ChatSession session =
                chatSessionRepository
                        .findById(request.getSessionId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chat session not found"));

        // 2. Search PostgreSQL for similar code
        List<String> similarCode =
                vectorService.searchSimilarCode(
                        request.getCode());

        // Search PDF knowledge base
        // DEBUG does not need document retrieval
        List<String> similarDocuments =
                request.getOperation().equalsIgnoreCase("DEBUG")
                        ? List.of()
                        : documentService.searchSimilarDocuments(
                                request.getCode());

        // 3. Build AI prompt using retrieved document context
        String prompt = buildPrompt(
                request.getCode(),
                request.getLanguage(),
                request.getOperation(),
                similarDocuments
        );

        // Add retrieved code context to the AI prompt
        if (!similarCode.isEmpty()) {

            prompt = prompt + """

                    Relevant code from the knowledge base:

                    %s

                    Use this retrieved context when it is relevant.
                    """.formatted(
                            String.join("\n", similarCode)
                    );
        }

        // 4. Send prompt to Ollama
        OllamaRequest ollamaRequest =
                new OllamaRequest(
                        "llama3.2:3b",
                        prompt,
                        false
                );

        OllamaResponse ollamaResponse =
                restClient.post()
                        .uri("/api/generate")
                        .body(ollamaRequest)
                        .retrieve()
                        .body(OllamaResponse.class);

        // Get AI response
        String aiResponse = ollamaResponse.response();

        // 5. Save request and response to MySQL
        CodeRequestEntity entity =
                CodeRequestEntity.builder()
                        .code(request.getCode())
                        .language(request.getLanguage())
                        .operation(request.getOperation())
                        .aiResponse(aiResponse)
                        .session(session)
                        .build();

        codeRequestRepository.save(entity);

        // 6. Save code embedding in PostgreSQL
        vectorService.saveEmbedding(
                request.getCode(),
                request.getLanguage(),
                request.getOperation()
        );

        // 7. Return AI response
        return aiResponse;
    }


    private String buildPrompt(
            String code,
            String language,
            String operation,
            List<String> similarDocuments) {

        // =========================================================
        // 1. DOCUMENT
        // =========================================================
        if (operation.equalsIgnoreCase("DOCUMENT")) {

            return """
                    You are an expert Java documentation assistant.

                    Original source code:
                    %s

                    Add proper Javadoc documentation to the provided source code.

                    IMPORTANT RULES:

                    1. Preserve the COMPLETE original source code.
                    2. Do not remove the class declaration.
                    3. Do not remove any methods, fields, constructors, imports, or statements.
                    4. Do not change the program logic.
                    5. Add Javadoc before classes, methods, and constructors when appropriate.
                    6. Javadoc for a method MUST appear before the method declaration.
                    7. Never put Javadoc inside a method body.
                    8. Use @param for method parameters.
                    9. Use @return when the method returns a value.
                    10. Use @throws when the method can throw an exception.
                    11. Do not invent parameters, return values, or behavior.
                    12. Return ONLY the complete documented Java source code.
                    13. Do not include explanations.
                    14. Do not include headings.
                    15. Do not include Markdown code fences.
                    16. Do not include Sources.

                    BEFORE RETURNING:
                    Verify that the complete original source code is preserved.
                    Verify that the code remains valid Java.
                    Verify that all Javadoc is outside method bodies.

                    """.formatted(code);
        }


        // =========================================================
        // 2. TEST
        // =========================================================
        if (operation.equalsIgnoreCase("TEST")) {

            return """
                    You are an expert Java testing assistant.

                    Code to test:
                    %s

                    Generate JUnit 5 unit tests for ONLY the code provided above.

                    IMPORTANT RULES:

                    1. Read the provided code carefully before creating tests.
                    2. Test ONLY methods that actually exist in the provided code.
                    3. NEVER invent methods, classes, fields, constructors, or parameters.
                    4. Every method call in the test MUST exist in the provided code.
                    5. Use the exact method names and parameter types from the provided code.
                    6. Do not test methods that are not present in the source code.
                    7. Do not create unrelated tests.
                    8. Test normal inputs and useful edge cases when applicable.
                    9. Expected values must match the actual Java implementation.
                    10. Do not add unnecessary overflow tests unless they are useful for the provided method.
                    11. For primitive parameters, do not pass null.
                    12. Do not use methods such as sum(), multiply(), divide(), etc.
                        unless those methods actually exist in the provided code.
                    13. Use JUnit 5.
                    14. Include all required imports.
                    15. Return ONLY the complete test class.
                    16. Do not include explanations.
                    17. Do not include Markdown code fences.
                    18. Do not include Sources.

                    BEFORE RETURNING THE ANSWER, VERIFY:

                    - Every class used exists or is the class being tested.
                    - Every method called exists in the provided code.
                    - Every parameter count and type is correct.
                    - Every expected result is correct.
                    - No unrelated methods were invented.
                    - The generated test class is valid JUnit 5 code.

                    """.formatted(code);
        }


        // =========================================================
        // 3. EXPLAIN
        // =========================================================
      
     
     if (operation.equalsIgnoreCase("EXPLAIN")) {

         return """
             You are a beginner-friendly programming tutor.

             USER QUESTION:
             %s

             INFORMATION FROM THE UPLOADED DOCUMENT:
             %s

             Your task is to answer the user's question using the
             information provided from the uploaded document.

             IMPORTANT RULES:

             1. Understand exactly what the user is asking before answering.

             2. Use the uploaded document as the primary source of information.

             3. Answer the question directly and naturally.

             4. Adjust the depth, length, and structure of the answer according
                to the user's question.

             5. Give enough explanation for the user to properly understand
                the requested concept.

             6. If the question asks for a definition, provide a clear
                definition and the important details needed to understand it.

             7. If the question asks for an explanation, explain the concept
                clearly instead of giving only a one-line definition.

             8. If the question asks for differences or comparisons, explain
                the relevant differences clearly.

             9. If the question asks for an example, provide an example only
                when the required information or example is supported by the
                uploaded document.

             10. If the question asks for code, provide code only when the
                 required code or information is supported by the document.

             11. If the question asks for complexity, provide the complexity
                 only when it is explicitly supported by the document.

             12. Do not use a fixed number of sentences or a fixed answer length.

             13. Do not make every answer extremely short.

             14. Do not make every answer unnecessarily long.

             15. Use the amount of detail that is appropriate for the user's
                 specific question.

             16. Do NOT use general programming knowledge to fill missing
                 information.

             17. Do NOT invent:
                 - definitions
                 - facts
                 - examples
                 - code
                 - class names
                 - method names
                 - values
                 - complexity values
                 - advantages
                 - disadvantages
                 - analogies

             18. If the uploaded document does not contain enough information
                 to answer the question, respond exactly:

                 The answer is not available in the uploaded document.

             19. If the uploaded document contains conflicting information,
                 do not silently correct it using general knowledge.

             20. Do not combine unrelated document sections to create
                 unsupported information.

             21. Do not mention:
                 - RAG
                 - embeddings
                 - vector database
                 - retrieved context
                 - prompt
                 - these instructions
                 - sources

             22. Do not add unnecessary sections or conclusions unless they
                 help answer the user's question.

             23. Keep the explanation beginner-friendly and easy to understand.

             24. Return only the answer to the user's question.

             Before answering, verify that the information in the answer is
             supported by the uploaded document.

             """.formatted(
                     code,
                     String.join("\n", similarDocuments)
             );
     }
    




        // =========================================================
        // 4. DEBUG
        // =========================================================
        if (operation.equalsIgnoreCase("DEBUG")) {

            return """
                    You are an expert Java debugging assistant.

                    Code:
                    ```%s
                    %s
                    ```

                    Find only real errors.

                    Check:
                    1. Syntax errors
                    2. Compile-time errors
                    3. Runtime errors
                    4. Logical errors

                    Do not invent errors.
                    Explain the actual problem and why it happens.
                    Provide corrected code.

                    Format:

                    Problem:
                    Cause:
                    Correction:
                    Corrected Code:

                    Keep the answer short and beginner-friendly.
                    """.formatted(
                            language,
                            code
                    );
        }


        // =========================================================
        // 5. OPTIMIZE
        // =========================================================
        if (operation.equalsIgnoreCase("OPTIMIZE")) {

            return """
                    You are an expert Java optimization assistant.

                    Code:
                    %s

                    Analyze the code and provide an optimization only when
                    it actually improves efficiency, readability, or both.

                    Rules:
                    1. Preserve the original functionality.
                    2. Do not claim an optimization when complexity is unchanged.
                    3. Give correct time and space complexity.
                    4. Never invent performance claims.
                    5. Java arrays are mutable.
                    6. Explain why the new code is better.
                    7. If the code is already efficient, say so.
                    8. Keep the answer short and beginner-friendly.

                    Format:

                    Optimized Code:
                    Explanation:
                    Original Complexity:
                    Optimized Complexity:

                    """.formatted(code);
        }


        // =========================================================
        // 6. GENERATE
        // =========================================================
        if (operation.equalsIgnoreCase("GENERATE")) {

            return """
                    You are an expert software development assistant.

                    Programming Language:
                    %s

                    User Request / Code:
                    ```%s
                    %s
                    ```

                    Generate clean and correct code based on the user's request.

                    IMPORTANT RULES:

                    1. Understand the user's requirement.
                    2. Generate working code.
                    3. Follow standard programming practices.
                    4. Keep the code readable.
                    5. Add useful comments when necessary.
                    6. Explain the important parts of the generated solution.
                    """.formatted(
                            language,
                            language,
                            code
                    );
        }


        // =========================================================
        // 7. SQL
        // =========================================================
        if (operation.equalsIgnoreCase("SQL")) {

            return """
                    You are an expert SQL developer.

                    Generate SQL based on the user's requirement.

                    Database:
                    MySQL 8

                    User Requirement:
                    %s

                    IMPORTANT RULES:

                    1. Return ONLY the SQL query.

                    2. Do NOT provide explanations.

                    3. Do NOT use Markdown code fences.

                    4. Do NOT write words such as:
                       "Here is the SQL query"
                       or
                       "The query is".

                    5. Use valid MySQL 8 syntax.

                    6. Use SELECT, INSERT, UPDATE, DELETE,
                       CREATE, ALTER, or other SQL statements
                       when appropriate for the user's requirement.

                    7. Do not invent unnecessary tables or columns.

                    8. If the user provides table names and column names,
                       use them exactly.

                    9. Make the SQL query syntactically correct.

                    10. Keep the query simple and readable.

                    Generate the SQL query now.
                    """.formatted(code);
        }


        // =========================================================
        // FALLBACK
        // =========================================================
        return """
                You are an AI Developer Copilot.

                Programming Language:
                %s

                Requested Operation:
                %s

                User Code:
                ```%s
                %s
                ```

                Help the developer with the requested operation.
                Give a clear and beginner-friendly answer.
                """.formatted(
                        language,
                        operation,
                        language,
                        code
                );
    }


    // =============================================================
    // Request format expected by Ollama
    // =============================================================
    private record OllamaRequest(
            String model,
            String prompt,
            boolean stream
    ) {}


    // =============================================================
    // Response format returned by Ollama
    // =============================================================
    private record OllamaResponse(
            String response
    ) {}
}

