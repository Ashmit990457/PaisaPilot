package com.example.paisapilot.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.BuildConfig;
import com.example.paisapilot.model.ReceiptResult;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.ReceiptRepository;

public class ReceiptViewModel extends AndroidViewModel {
    private final ReceiptRepository repository;
    private final MutableLiveData<Resource<ReceiptResult>> scanResult = new MutableLiveData<>();
    private final String apiKey;

    public ReceiptViewModel(@NonNull Application application) {
        super(application);
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        this.repository = new ReceiptRepository(apiKey); 
    }

    public LiveData<Resource<ReceiptResult>> getScanResult() {
        return scanResult;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY");
    }

    public void analyze(Bitmap bitmap) {
        if (!isApiKeyConfigured()) {
            scanResult.setValue(Resource.error("Gemini API key is not configured. Please add GEMINI_API_KEY=your_key to your local.properties file and sync."));
            return;
        }

        scanResult.setValue(Resource.loading());
        repository.analyzeReceipt(bitmap, new ReceiptRepository.ReceiptCallback() {
            @Override
            public void onSuccess(ReceiptResult result) {
                scanResult.postValue(Resource.success(result));
            }

            @Override
            public void onError(String message) {
                scanResult.postValue(Resource.error(message));
            }
        });
    }
}
