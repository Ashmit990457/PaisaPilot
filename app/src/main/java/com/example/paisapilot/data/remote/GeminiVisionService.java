package com.example.paisapilot.data.remote;

import android.graphics.Bitmap;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

public class GeminiVisionService {
    private final GenerativeModelFutures model;

    public GeminiVisionService(String apiKey) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        this.model = GenerativeModelFutures.from(gm);
    }

    public ListenableFuture<GenerateContentResponse> extractReceiptData(Bitmap bitmap) {
        String prompt = "Extract receipt information into this exact JSON format. " +
                "Return ONLY the raw JSON string, no markdown code blocks, no backticks, no explanation. " +
                "JSON format: " +
                "{\"merchant\":\"string\", \"title\":\"string\", \"amount\":number, \"currency\":\"string\", " +
                "\"category\":\"string\", \"paymentMethod\":\"string\", \"date\":\"YYYY-MM-DD\", " +
                "\"time\":\"HH:MM\", \"tax\":number, \"confidence\":number, \"items\":[{\"name\":\"string\", \"price\":number}]}. " +
                "Infer category from merchant or items if not clear. Confidence should be 0-100.";

        Content content = new Content.Builder()
                .addText(prompt)
                .addImage(bitmap)
                .build();

        return model.generateContent(content);
    }
}
