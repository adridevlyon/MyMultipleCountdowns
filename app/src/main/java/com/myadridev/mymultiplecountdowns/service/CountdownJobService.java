package com.myadridev.mymultiplecountdowns.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.activity.MainActivity;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;
import com.myadridev.mymultiplecountdowns.model.CountdownItem;

public class CountdownJobService extends JobService {
    public static final String dayMaxKey = "dayMaxKey";
    public static final String idKey = "idKey";
    private int day = 1;
    private int maxDay;
    private int itemId;
    private final Handler itemHandler = new Handler();
    private Runnable runnable;
    private boolean isActive;

    @Override
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getJobId();
        PersistableBundle extras = params.getExtras();
        maxDay = extras.getInt(dayMaxKey);
        itemId = extras.getInt(idKey);
        CountdownItem item = SharedPreferencesHelper.getCountdownItem(this, itemId);
        if (item == null) {
            return false;
        }
        isActive = true;
        runnable = new Runnable() {
            public void run() {
                if (!isActive) {
                    itemHandler.removeCallbacks(this);
                    return;
                }
                Intent resultIntent = new Intent(CountdownJobService.this, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(CountdownJobService.this, jobId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                String notificationContent = getString(R.string.notification_day, day, maxDay);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(CountdownJobService.this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(CountdownJobService.this, R.color.colorPrimary))
                        .setContentTitle(item.label)
                        .setContentText(notificationContent)
                        .setContentIntent(resultPendingIntent)
                        .setCategory(ALARM_SERVICE)
                        .setAutoCancel(true);

                Notification notification = notificationBuilder.build();
                notification.defaults |= Notification.DEFAULT_ALL;

                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(jobId, notification);

                if (day < maxDay) {
                    day++;
                    itemHandler.postDelayed(this, AutoStartService.NumberMinutesInADay * 60 * 1000);
                } else {
                    itemHandler.removeCallbacks(this);
                }
            }
        };
        itemHandler.post(runnable);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        isActive = false;
        itemHandler.removeCallbacks(runnable);
        return false;
    }
}
