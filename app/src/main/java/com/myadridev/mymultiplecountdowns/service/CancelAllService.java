package com.myadridev.mymultiplecountdowns.service;

import android.app.Service;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;

public class CancelAllService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int numberJobsLaunched = SharedPreferencesHelper.loadNumberActiveJobs(this);
        if (numberJobsLaunched == 0) {
            return Service.START_STICKY;
        }

        JobScheduler jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        for (int i = 0; i < numberJobsLaunched; i++) {
            jobScheduler.cancel(i);
        }
        SharedPreferencesHelper.saveNumberActiveJobs(this, 0);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
