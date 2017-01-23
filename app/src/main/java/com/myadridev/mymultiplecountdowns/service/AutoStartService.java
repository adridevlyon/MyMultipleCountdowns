package com.myadridev.mymultiplecountdowns.service;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PersistableBundle;

import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;
import com.myadridev.mymultiplecountdowns.model.CountdownItem;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AutoStartService extends Service {
    private JobScheduler jobScheduler;
    private int jobId;
    public static final int NumberMinutesInADay = 1440;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Date lastLaunchDate = SharedPreferencesHelper.loadLastLaunchDate(this);
        int numberJobsLaunched = SharedPreferencesHelper.loadNumberActiveJobs(this);
        if (lastLaunchDate == null) {
            return Service.START_STICKY;
        }

        jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        for (int i = 0; i < numberJobsLaunched; i++) {
            jobScheduler.cancel(i);
        }
        jobId = 0;
        launchNeededScheduledJobs(SharedPreferencesHelper.loadCountdownItems(this), lastLaunchDate);
        SharedPreferencesHelper.saveNumberActiveJobs(this, jobId);

        return Service.START_STICKY;
    }

    private void launchNeededScheduledJobs(List<CountdownItem> countdownItems, Date lastLaunchDate) {
        for (CountdownItem item : countdownItems) {
            Calendar calNow = Calendar.getInstance();
            Calendar calLastDayItem = Calendar.getInstance();
            calLastDayItem.setTime(lastLaunchDate);
            calLastDayItem.add(Calendar.MINUTE, NumberMinutesInADay * item.delay);
            if (calNow.before(calLastDayItem)) {
                launchScheduledJob(item, lastLaunchDate);
            }
        }
    }

    private void launchScheduledJob(CountdownItem item, Date lastLaunchDate) {
        Calendar calNow = Calendar.getInstance();
        Calendar calNextLaunchDate = Calendar.getInstance();
        calNextLaunchDate.setTime(lastLaunchDate);
        calNextLaunchDate.add(Calendar.MINUTE, NumberMinutesInADay * item.delay);
        if (calNow.after(calNextLaunchDate)) {
            return;
        }
        ComponentName serviceComponent = new ComponentName(this, CountdownJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        jobId++;
        long delayBeforeLaunch = Math.max(0, calNextLaunchDate.getTimeInMillis() - calNow.getTimeInMillis());
        PersistableBundle extras = new PersistableBundle(2);
        extras.putInt(CountdownJobService.idKey, item.id);
        extras.putInt(CountdownJobService.dayMaxKey, item.duration);
        builder.setExtras(extras);
        builder.setMinimumLatency(delayBeforeLaunch);
        builder.setOverrideDeadline(delayBeforeLaunch);

        jobScheduler.schedule(builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
