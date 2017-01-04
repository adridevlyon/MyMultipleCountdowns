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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Date lastLaunchDate = SharedPreferencesHelper.loadLastLaunchDate(this);
        if (lastLaunchDate == null) {
            return Service.START_STICKY;
        }

        jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.cancelAll();
        launchNeededScheduledJobs(SharedPreferencesHelper.loadCountdownItems(this), lastLaunchDate);

        return Service.START_STICKY;
    }

    private void launchNeededScheduledJobs(List<CountdownItem> countdownItems, Date lastLaunchDate) {
        for (CountdownItem item : countdownItems) {
            Calendar calNow = Calendar.getInstance();
            Calendar calLastDayItem = Calendar.getInstance();
            calLastDayItem.setTime(lastLaunchDate);
            calLastDayItem.add(Calendar.DATE, item.delay + item.duration - 1);
            if (calNow.before(calLastDayItem)) {
                launchScheduledJob(item, lastLaunchDate);
            }
        }
    }

    private void launchScheduledJob(CountdownItem item, Date lastLaunchDate) {
        Calendar calNow = Calendar.getInstance();
        Calendar calNextLaunchDate = Calendar.getInstance();
        calNextLaunchDate.setTime(lastLaunchDate);
        calNextLaunchDate.add(Calendar.DATE, item.delay);

        for (int i = 0; i < item.duration; i++) {
            if (calNow.after(calNextLaunchDate)) {
                continue;
            } else {
                ComponentName serviceComponent = new ComponentName(this, CountdownJobService.class);
                JobInfo.Builder builder = new JobInfo.Builder(item.id, serviceComponent);
                long delayBeforeLaunch = Math.max(0, calNextLaunchDate.getTimeInMillis() - calNow.getTimeInMillis());
                PersistableBundle extras = new PersistableBundle(2);
                extras.putInt(CountdownJobService.dayKey, i + 1);
                extras.putInt(CountdownJobService.dayMaxKey, item.duration);
                builder.setExtras(extras);
                builder.setMinimumLatency(delayBeforeLaunch);
                builder.setOverrideDeadline(delayBeforeLaunch);

                jobScheduler.schedule(builder.build());
                calNextLaunchDate.add(Calendar.DATE, 1);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
