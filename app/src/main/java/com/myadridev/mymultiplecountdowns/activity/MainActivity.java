package com.myadridev.mymultiplecountdowns.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.adapter.CountdownItemsAdapter;
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

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private Button buttonGo;
    private Button buttonCancel;
    private TableRow tableRowNotificationTime;
    private TextView textViewNotificationTime;
    private TextView textViewLastLaunchDate;
    private RecyclerView recyclerViewCountdownList;

    private Date notificationTime;

    private Date lastLaunchDate;

    private SimpleDateFormat timeFormat;

    private SimpleDateFormat dateFormat;
    private boolean is24HoursFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String timeFormatString = getString(R.string.utils_time_format);
        timeFormat = new SimpleDateFormat(timeFormatString);
        is24HoursFormat = !timeFormatString.endsWith("a");

        String dateFormatString = getString(R.string.utils_date_format);
        dateFormat = new SimpleDateFormat(dateFormatString);

        setLayout();
        setEvents();
        loadStoredData();
    }

    private void setLayout() {
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        buttonGo = (Button) findViewById(R.id.button_go);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        textViewNotificationTime = (TextView) findViewById(R.id.textview_notification_time);
        tableRowNotificationTime = (TableRow) findViewById(R.id.tablerow_notification_time);
        textViewLastLaunchDate = (TextView) findViewById(R.id.textview_lastlaunch_date);
        recyclerViewCountdownList = (RecyclerView) findViewById(R.id.recyclerview_countdown_list);
    }

    private void loadStoredData() {
        setNotificationTime(SharedPreferencesHelper.loadNotificationTime(this));
        setLastLaunchDate(SharedPreferencesHelper.loadLastLaunchDate(this));

        recyclerViewCountdownList.setLayoutManager(new LinearLayoutManager(this));
        CountdownItemsAdapter recyclerViewCountdownAdapter = new CountdownItemsAdapter(this, coordinatorLayout, SharedPreferencesHelper.loadCountdownItems(this));
        recyclerViewCountdownList.setAdapter(recyclerViewCountdownAdapter);
    }

    private void setEvents() {
        RxView.clicks(buttonGo)
                .flatMap(x -> DialogHelper.displayAlertDialog(this, R.string.title_go_confirmation, R.string.message_go_confirmation, R.string.ok_go_confirmation, R.string.no))
                .filter(x -> x)
                .subscribe(x -> saveLastLaunchDay());

        RxView.clicks(buttonCancel)
                .flatMap(x -> DialogHelper.displayAlertDialog(this, R.string.title_cancel_confirmation, R.string.message_cancel_confirmation, R.string.ok_cancel_confirmation, R.string.no))
                .filter(x -> x)
                .subscribe(x -> cancelAllJobs());

        RxView.clicks(tableRowNotificationTime)
                .flatMap(x -> displayDatePickerDialog(this, R.string.title_notification_time_picker, is24HoursFormat))
                .subscribe(this::updateNotificationDate);
    }

    private void updateNotificationDate(Date date) {
        SharedPreferencesHelper.saveNotificationTime(this, date);
        setNotificationTime(date);
        if (lastLaunchDate != null) {
            saveLastLaunchDay(lastLaunchDate);
            displaySnackbar(getString(R.string.countdown_updated));
        }
    }

    private void displaySnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.ok), v -> snackbar.dismiss());
        snackbar.show();
    }

    private void setNotificationTime(Date date) {
        notificationTime = date;
        textViewNotificationTime.setText(timeFormat.format(date));
    }

    private void setLastLaunchDate(Date date) {
        lastLaunchDate = date;
        textViewLastLaunchDate.setText(date != null ? dateFormat.format(date) : getString(R.string.lastlaunch_never));
    }

    private void saveLastLaunchDay(Date lastLaunchDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastLaunchDate);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(notificationTime);
        cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));

        Date newLastLaunchDate = cal.getTime();
        SharedPreferencesHelper.saveLastLaunchDate(this, newLastLaunchDate);
        setLastLaunchDate(newLastLaunchDate);
        restartCountdownJobs();
    }

    private void saveLastLaunchDay() {
        Calendar cal = Calendar.getInstance();
        saveLastLaunchDay(cal.getTime());
        displaySnackbar(getString(R.string.countdown_launched));
    }

    private void restartCountdownJobs() {
        startService(new Intent(this, AutoStartService.class));
    }

    private void cancelAllJobs() {
        SharedPreferencesHelper.saveLastLaunchDate(this, null);
        setLastLaunchDate(null);
        startService(new Intent(this, CancelAllService.class));
        displaySnackbar(getString(R.string.countdown_cancelled));
    }

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
