package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.DashboardData;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.DashboardRepository;

public class DashboardViewModel extends AndroidViewModel {

    private final DashboardRepository repository;
    private final MediatorLiveData<Resource<DashboardData>> dashboardState = new MediatorLiveData<>();
    private final MediatorLiveData<String> userNameState = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DashboardRepository(application);
        
        dashboardState.addSource(repository.getDashboardData(), data -> {
            if (data != null) {
                dashboardState.setValue(Resource.success(data));
            }
        });

        userNameState.addSource(repository.getUserName(), name -> {
            if (name != null) {
                userNameState.setValue(name);
            }
        });
    }

    public LiveData<Resource<DashboardData>> getDashboardState() {
        return dashboardState;
    }

    public LiveData<String> getUserNameState() {
        return userNameState;
    }

    public void loadDashboardData() {
        // Automatically observed from Repository
    }
}
