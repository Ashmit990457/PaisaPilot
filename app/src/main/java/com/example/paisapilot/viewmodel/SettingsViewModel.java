package com.example.paisapilot.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.data.session.SessionManager;
import com.example.paisapilot.data.session.SettingsManager;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.UserProfile;
import com.example.paisapilot.repository.UserProfileRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final UserProfileRepository profileRepository;
    private final SettingsManager settingsManager;
    private final SessionManager sessionManager;
    private final MutableLiveData<Resource<Boolean>> accountActionState = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.profileRepository = new UserProfileRepository(application);
        this.settingsManager = SettingsManager.getInstance(application);
        this.sessionManager = SessionManager.getInstance(application);
    }

    public LiveData<UserProfile> getUserProfile() {
        return profileRepository.getUserProfile();
    }

    public LiveData<Resource<Boolean>> getAccountActionState() {
        return accountActionState;
    }

    public int getThemeMode() {
        return settingsManager.getThemeMode();
    }

    public void setThemeMode(int mode) {
        settingsManager.setThemeMode(mode);
    }

    public String getCurrency() {
        return settingsManager.getCurrency();
    }

    public void setCurrency(String currency) {
        settingsManager.setCurrency(currency);
    }

    public String getDateFormat() {
        return settingsManager.getDateFormat();
    }

    public void setDateFormat(String format) {
        settingsManager.setDateFormat(format);
    }

    public void logout() {
        // Logic handled in Activity usually to manage backstack
        accountActionState.setValue(Resource.success(true));
    }

    public void deleteAccount() {
        accountActionState.setValue(Resource.loading());
        // In a real app, call repository to delete from Firebase and Room
        // For now, simulate success
        new android.os.Handler().postDelayed(() -> {
            accountActionState.postValue(Resource.success(true));
        }, 1000);
    }
}
