package com.example.paisapilot.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.NavigationTarget;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.SplashRepository;

public class SplashViewModel extends AndroidViewModel {
    private static final String TAG = "SplashViewModel";
    private final SplashRepository repository;
    private final MutableLiveData<Resource<NavigationTarget>> state = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SplashRepository(application);
    }

    public LiveData<Resource<NavigationTarget>> getState() { return state; }

    /**
     * Start the splash flow: check auth/profile immediately.
     * The repository handles background threading and Firestore timeouts.
     */
    public void start() {
        Log.d(TAG, "start: Initializing...");
        state.setValue(Resource.loading());
        
        repository.checkAuthAndProfile(new SplashRepository.Callback() {
            @Override
            public void onResult(@NonNull NavigationTarget target) {
                Log.d(TAG, "onResult: Navigating to " + target);
                state.postValue(Resource.success(target));
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "onError: " + message);
                state.postValue(Resource.error(message));
            }
        });
    }
}
