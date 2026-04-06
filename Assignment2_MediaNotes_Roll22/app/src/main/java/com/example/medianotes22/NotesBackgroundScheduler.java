package com.example.medianotes22;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class NotesBackgroundScheduler {

    private NotesBackgroundScheduler() {
        // Utility class.
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NotesReminderWorker.class,
                AssignmentConfig.getWorkIntervalMinutes(),
                TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AssignmentConfig.getWorkerUniqueName(),
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }
}
