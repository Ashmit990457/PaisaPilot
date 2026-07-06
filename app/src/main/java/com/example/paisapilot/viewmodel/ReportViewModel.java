package com.example.paisapilot.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.data.local.entity.MonthlyArchiveEntity;
import com.example.paisapilot.model.MonthlyReport;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.repository.ReportRepository;

import java.util.List;

public class ReportViewModel extends AndroidViewModel {

    private final ReportRepository repository;
    private final MutableLiveData<Resource<MonthlyReport>> reportState = new MutableLiveData<>();

    public ReportViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ReportRepository(application);
    }

    public LiveData<Resource<MonthlyReport>> getReportState() {
        return reportState;
    }

    public LiveData<List<MonthlyArchiveEntity>> getArchives() {
        return repository.getAllArchives();
    }

    public void loadReport(String monthId) {
        reportState.setValue(Resource.loading());
        repository.loadReportForMonth(monthId, new ReportRepository.ReportCallback() {
            @Override
            public void onSuccess(@NonNull MonthlyReport report) {
                reportState.postValue(Resource.success(report));
            }

            @Override
            public void onError(@NonNull String message) {
                reportState.postValue(Resource.error(message));
            }
        });
    }
}
