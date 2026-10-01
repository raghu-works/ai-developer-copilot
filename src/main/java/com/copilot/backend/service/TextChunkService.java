package com.copilot.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TextChunkService {

    public List<String> splitText(String text, int chunkSize) {

        List<String> chunks = new ArrayList<>();

        int overlap = 150;

        int start = 0;

        while (start < text.length()) {

            int end = Math.min(
                    start + chunkSize,
                    text.length()
            );

            String chunk = text.substring(start, end);

            chunks.add(chunk);

            if (end == text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}

