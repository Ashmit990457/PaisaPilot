package com.example.paisapilot.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.NavigationTarget;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.SplashRepository;

/**
 * SplashViewModel
 *
 * Responsibility: Drive splash screen logic: wait the required delay, then consult
 * SplashRepository to determine the next navigation target. Exposes LiveData<Resource<NavigationTarget>>
 * so the Activity can react to loading, success and error states.
 */
public class SplashViewModel extends AndroidViewModel {
    private static final long SPLASH_DELAY_MS = 2000L;
    private final SplashRepository repository;
    private final MutableLiveData<Resource<NavigationTarget>> state = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SplashRepository(application);
    }

    public LiveData<Resource<NavigationTarget>> getState() { return state; }

    /**
     * Start the splash flow: emit loading, wait delay, then check auth/profile.
     */
    public void start() {
        state.setValue(Resource.loading());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            repository.checkAuthAndProfile(new SplashRepository.Callback() {
                @Override
                public void onResult(@NonNull NavigationTarget target) {
                    state.postValue(Resource.success(target));
                }

                @Override
                public void onError(@NonNull String message) {
                    state.postValue(Resource.error(message));
                }
            });
        }, SPLASH_DELAY_MS);
    }
}
