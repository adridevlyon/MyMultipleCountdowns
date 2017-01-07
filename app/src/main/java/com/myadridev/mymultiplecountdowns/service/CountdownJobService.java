package com.myadridev.mymultiplecountdowns.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.activity.MainActivity;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;
import com.myadridev.mymultiplecountdowns.model.CountdownItem;

public class CountdownJobService extends JobService {
    public static final String dayKey = "dayKey";
    public static final String dayMaxKey = "dayMaxKey";
    public static final String idKey = "idKey";

    @Override
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getJobId();
        PersistableBundle extras = params.getExtras();
        int day = extras.getInt(dayKey);
        int maxDay = extras.getInt(dayMaxKey);
        int itemId = extras.getInt(idKey);
        CountdownItem item = SharedPreferencesHelper.getCountdownItem(this, itemId);
        if (item == null) {
            return false;
        }
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, jobId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationContent = getString(R.string.notification_day, day, maxDay);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(item.label)
                .setContentText(notificationContent)
                .setContentIntent(resultPendingIntent)
                .setCategory(ALARM_SERVICE)
                .setAutoCancel(true);

        Notification notification = notificationBuilder.build();
        notification.defaults |= Notification.DEFAULT_ALL;

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(jobId, notification);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
