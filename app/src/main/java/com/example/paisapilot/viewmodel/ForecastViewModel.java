package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Forecast;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.ForecastRepository;

public class ForecastViewModel extends AndroidViewModel {

    private final ForecastRepository repository;
    private final MutableLiveData<Resource<Forecast>> forecastState = new MutableLiveData<>();

    public ForecastViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ForecastRepository(application);
    }

    public LiveData<Resource<Forecast>> getForecastState() {
        return forecastState;
    }

    public void loadForecast() {
        // Redundant as DashboardViewModel handles this now.
        // Keeping it for compilation.
    }
}
