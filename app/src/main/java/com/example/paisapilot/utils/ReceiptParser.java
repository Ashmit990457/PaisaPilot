package com.example.paisapilot.utils;

import com.example.paisapilot.model.ReceiptResult;
import com.google.gson.Gson;

public class ReceiptParser {
    public static ReceiptResult parse(String jsonString) {
        try {
            // Remove markdown code blocks if present
            String cleaned = jsonString.replaceAll("```json", "")
                                     .replaceAll("```", "")
                                     .trim();
            return new Gson().fromJson(cleaned, ReceiptResult.class);
        } catch (Exception e) {
            return null;
        }
    }
}
