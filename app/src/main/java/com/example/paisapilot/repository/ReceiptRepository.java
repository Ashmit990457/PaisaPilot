package com.example.paisapilot.repository;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.paisapilot.data.remote.GeminiVisionService;
import com.example.paisapilot.model.ReceiptResult;
import com.example.paisapilot.utils.ReceiptParser;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ResponseStoppedException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReceiptRepository {
    private static final String TAG = "ReceiptRepository";
    private final GeminiVisionService geminiService;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ReceiptRepository(String apiKey) {
        this.geminiService = new GeminiVisionService(apiKey);
    }

    public interface ReceiptCallback {
        void onSuccess(ReceiptResult result);
        void onError(String message);
    }

    public void analyzeReceipt(Bitmap bitmap, ReceiptCallback callback) {
        ListenableFuture<GenerateContentResponse> future = geminiService.extractReceiptData(bitmap);
        
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse response) {
                try {
                    String text = response.getText();
                    if (text == null || text.isEmpty()) {
                        callback.onError("AI returned an empty response. Please try a clearer photo.");
                        return;
                    }

                    ReceiptResult result = ReceiptParser.parse(text);
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Failed to read receipt data. Please ensure the total amount and items are visible.");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing AI response", e);
                    callback.onError("Technical error parsing receipt data.");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini API failure", t);
                
                String error = t.toString().toLowerCase();
                String userMessage;
                
                if (t instanceof ResponseStoppedException) {
                    userMessage = "The scan was stopped due to safety filters. Please try another receipt.";
                } else if (error.contains("api_key") || error.contains("invalid")) {
                    userMessage = "Invalid API Key. Please check your configuration in local.properties.";
                } else if (error.contains("network") || error.contains("connection") || error.contains("timeout") || error.contains("resolve")) {
                    userMessage = "Network error. Please check your internet connection.";
                } else if (error.contains("not found") || error.contains("404")) {
                    userMessage = "The AI model is currently unavailable or unsupported for your region/key.";
                } else if (error.contains("serialization") || error.contains("missingfield")) {
                    // This handles the SDK crash by providing a more useful message
                    userMessage = "AI service error. This is a known SDK issue, please try again in a moment.";
                } else if (error.contains("quota") || error.contains("limit") || error.contains("429")) {
                    userMessage = "API quota exceeded. Please try again later.";
                } else {
                    userMessage = "AI analysis failed: " + t.getLocalizedMessage();
                }
                
                callback.onError(userMessage);
            }
        }, executor);
    }
}
