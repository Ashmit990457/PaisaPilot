package com.example.paisapilot.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.ReceiptResult;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.ReceiptRepository;

public class ReceiptViewModel extends AndroidViewModel {
    private final ReceiptRepository repository;
    private final MutableLiveData<Resource<ReceiptResult>> scanResult = new MutableLiveData<>();

    public ReceiptViewModel(@NonNull Application application) {
        super(application);
        // Normally the API key should be in a secure location or BuildConfig
        this.repository = new ReceiptRepository("YOUR_API_KEY"); 
    }

    public LiveData<Resource<ReceiptResult>> getScanResult() {
        return scanResult;
    }

    public void analyze(Bitmap bitmap) {
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
