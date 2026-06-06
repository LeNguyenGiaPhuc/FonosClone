package hcmute.com.fonosclone.sync;


import hcmute.com.fonosclone.worker.FirebaseSyncWorker;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public final class SyncScheduler {
    private SyncScheduler() {
    }

    public static void enqueueUserSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(FirebaseSyncWorker.class)
                .setConstraints(constraints)
                .addTag(FirebaseSyncWorker.UNIQUE_WORK_NAME)
                .build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                FirebaseSyncWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }
}
