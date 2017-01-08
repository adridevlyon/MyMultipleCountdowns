package com.myadridev.mymultiplecountdowns.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.helper.DialogHelper;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;
import com.myadridev.mymultiplecountdowns.service.AutoStartService;
import com.myadridev.mymultiplecountdowns.service.CancelAllService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class HeaderManager {

    private final CoordinatorLayout coordinatorLayout;
    private final Button buttonGo;
    private final Button buttonCancel;
    private final TableRow tableRowNotificationTime;
    private final TextView textViewNotificationTime;
    private final TextView textViewLastLaunchDate;

    private Date notificationTime;

    private Date lastLaunchDate;

    private SimpleDateFormat timeFormat;

    private SimpleDateFormat dateFormat;
    private boolean is24HoursFormat;

    public HeaderManager(Context context, CoordinatorLayout coordinatorLayout, Button buttonGo, Button buttonCancel, TextView textViewNotificationTime,
                         TableRow tableRowNotificationTime, TextView textViewLastLaunchDate) {
        this.coordinatorLayout = coordinatorLayout;
        this.buttonGo = buttonGo;
        this.buttonCancel = buttonCancel;
        this.textViewNotificationTime = textViewNotificationTime;
        this.tableRowNotificationTime = tableRowNotificationTime;
        this.textViewLastLaunchDate = textViewLastLaunchDate;

        String timeFormatString = context.getString(R.string.utils_time_format);
        timeFormat = new SimpleDateFormat(timeFormatString);
        is24HoursFormat = !timeFormatString.endsWith("a");

        String dateFormatString = context.getString(R.string.utils_date_format);
        dateFormat = new SimpleDateFormat(dateFormatString);
    }

    public void loadStoredData(Context context) {
        setNotificationTime(SharedPreferencesHelper.loadNotificationTime(context));
        setLastLaunchDate(context, SharedPreferencesHelper.loadLastLaunchDate(context));
    }

    public void setEvents(Context context) {
        RxView.clicks(buttonGo)
                .flatMap(x -> DialogHelper.displayAlertDialog(context, R.string.title_go_confirmation, R.string.message_go_confirmation, R.string.ok_go_confirmation, R.string.no))
                .filter(x -> x)
                .subscribe(x -> saveLastLaunchDay(context));

        RxView.clicks(buttonCancel)
                .flatMap(x -> DialogHelper.displayAlertDialog(context, R.string.title_cancel_confirmation, R.string.message_cancel_confirmation, R.string.ok_cancel_confirmation, R.string.no))
                .filter(x -> x)
                .subscribe(x -> cancelAllJobs(context));

        RxView.clicks(tableRowNotificationTime)
                .flatMap(x -> displayDatePickerDialog(context, R.string.title_notification_time_picker, is24HoursFormat))
                .subscribe(x -> updateNotificationDate(context, x));
    }

    private void updateNotificationDate(Context context, Date date) {
        SharedPreferencesHelper.saveNotificationTime(context, date);
        setNotificationTime(date);
        if (lastLaunchDate != null) {
            saveLastLaunchDay(context, lastLaunchDate);
            displaySnackbar(context, context.getString(R.string.countdown_updated));
        }
    }

    private void displaySnackbar(Context context, String message) {
        if (coordinatorLayout == null) return;

        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(context.getString(R.string.ok), v -> snackbar.dismiss());
        snackbar.show();
    }

    private void setNotificationTime(Date date) {
        notificationTime = date;
        textViewNotificationTime.setText(timeFormat.format(date));
    }

    private void setLastLaunchDate(Context context, Date date) {
        lastLaunchDate = date;
        textViewLastLaunchDate.setText(date != null ? dateFormat.format(date) : context.getString(R.string.lastlaunch_never));
    }

    private void saveLastLaunchDay(Context context, Date lastLaunchDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastLaunchDate);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(notificationTime);
        cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));

        Date newLastLaunchDate = cal.getTime();
        SharedPreferencesHelper.saveLastLaunchDate(context, newLastLaunchDate);
        setLastLaunchDate(context, newLastLaunchDate);
        restartCountdownJobs(context);
    }

    private void saveLastLaunchDay(Context context) {
        Calendar cal = Calendar.getInstance();
        saveLastLaunchDay(context, cal.getTime());
        displaySnackbar(context, context.getString(R.string.countdown_launched));
    }

    private void restartCountdownJobs(Context context) {
        context.startService(new Intent(context, AutoStartService.class));
    }

    private void cancelAllJobs(Context context) {
        SharedPreferencesHelper.saveLastLaunchDate(context, null);
        setLastLaunchDate(context, null);
        context.startService(new Intent(context, CancelAllService.class));
        displaySnackbar(context, context.getString(R.string.countdown_cancelled));
    }

    @NonNull
    private Observable<Date> displayDatePickerDialog(Context context, int title, boolean is24HoursFormat) {
        return Observable.create((Subscriber<? super Date> subscriber) -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(notificationTime);

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {
                cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                cal.set(Calendar.MINUTE, selectedMinute);
                subscriber.onNext(cal.getTime());
            }, hour, minute, is24HoursFormat);

            timePickerDialog.setTitle(title);
            subscriber.add(Subscriptions.create(timePickerDialog::dismiss));
            timePickerDialog.show();
        });
    }
}
