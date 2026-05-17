package com.cfanalyzer.ai;

import java.io.IOException;

import com.cfanalyzer.config.AppConfig;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

public class GeminiApiClient {

    private final Client client;
    private final String model;
    private final int maxTokens;

    public GeminiApiClient() {

        String apiKey = AppConfig.get("gemini.api.key");

        this.client = Client.builder()
                .apiKey(apiKey)
                .httpOptions(com.google.genai.types.HttpOptions.builder()
                        .apiVersion("v1beta")
                        .build())
                .build();

        // model mặc định
        this.model = AppConfig.get(
                "gemini.model",
                "gemini-1.5-flash"
        );

        this.maxTokens = AppConfig.getInt(
                "gemini.max.tokens",
                2048
        );
    }

    public String analyzeCode(String prompt) throws IOException {

        try {

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .temperature(0.1f)
                            .topP(0.95f)
                            .maxOutputTokens(maxTokens)
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            prompt,
                            config
                    );

            if (response == null || response.text() == null) {
                throw new IOException(
                        "Gemini trả về kết quả rỗng"
                );
            }

            return response.text();

        } catch (Exception e) {

            e.printStackTrace();

            throw new IOException(
                    "Lỗi Gemini API: " + e.getMessage(),
                    e
            );
        }
    }
}