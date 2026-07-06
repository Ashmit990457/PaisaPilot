package com.example.paisapilot.repository;

import android.graphics.Bitmap;
import com.example.paisapilot.data.remote.GeminiVisionService;
import com.example.paisapilot.model.ReceiptResult;
import com.example.paisapilot.utils.ReceiptParser;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReceiptRepository {
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
                String text = response.getText();
                ReceiptResult result = ReceiptParser.parse(text);
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("Failed to parse AI response. Raw: " + text);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t.getMessage());
            }
        }, executor);
    }
}
