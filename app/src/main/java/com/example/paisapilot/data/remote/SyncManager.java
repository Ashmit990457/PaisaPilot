package com.example.paisapilot.data.remote;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SyncManager {

    private final WorkManager workManager;

    public SyncManager(Context context) {
        this.workManager = WorkManager.getInstance(context);
    }

    public void triggerSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork("CloudSync", ExistingWorkPolicy.KEEP, syncRequest);
    }
}
