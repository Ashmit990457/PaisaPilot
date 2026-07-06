package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Insight;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.InsightRepository;

import java.util.List;

public class InsightViewModel extends AndroidViewModel {

    private final InsightRepository repository;
    private final MediatorLiveData<Resource<List<Insight>>> insightsState = new MediatorLiveData<>();

    public InsightViewModel(@NonNull Application application) {
        super(application);
        this.repository = new InsightRepository(application);
        
        insightsState.addSource(repository.getInsights(), insights -> {
            if (insights != null) {
                insightsState.setValue(Resource.success(insights));
            }
        });
    }

    public LiveData<Resource<List<Insight>>> getInsightsState() {
        return insightsState;
    }

    public void loadInsights() {
        // Automatically observed
    }
}
