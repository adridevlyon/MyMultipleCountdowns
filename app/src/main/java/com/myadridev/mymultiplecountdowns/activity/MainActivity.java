package com.myadridev.mymultiplecountdowns.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.adapter.CountdownItemsAdapter;
import com.myadridev.mymultiplecountdowns.helper.DialogHelper;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity {

    private String dateFormat;

    private CoordinatorLayout coordinatorLayout;
    private Button buttonGo;
    private TableRow tableRowNotificationTime;
    private TextView textViewNotificationTime;
    private RecyclerView recyclerViewCountdownList;

    private Calendar cal = Calendar.getInstance();
    private Date notificationTime;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormat = getString(R.string.utils_time_format);
        timeFormat = new SimpleDateFormat(dateFormat);

        setLayout();
        setEvents();
        loadStoredData();
    }

    private void setLayout() {
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        buttonGo = (Button) findViewById(R.id.button_go);
        textViewNotificationTime = (TextView) findViewById(R.id.textview_notification_time);
        tableRowNotificationTime = (TableRow) findViewById(R.id.tablerow_notification_time);
        recyclerViewCountdownList = (RecyclerView) findViewById(R.id.recyclerview_countdown_list);
    }

    private void loadStoredData() {
        setNotificationTime(SharedPreferencesHelper.loadNotificationTime(this));

        recyclerViewCountdownList.setLayoutManager(new LinearLayoutManager(this));
        CountdownItemsAdapter recyclerViewCountdownAdapter = new CountdownItemsAdapter(this, coordinatorLayout, SharedPreferencesHelper.loadCountdownItems(this));
        recyclerViewCountdownList.setAdapter(recyclerViewCountdownAdapter);
    }

    private void setEvents() {
        RxView.clicks(buttonGo)
                .flatMap(x -> DialogHelper.displayAlertDialog(this, R.string.title_go_confirmation, R.string.message_go_confirmation, R.string.ok_go_confirmation, R.string.no))
                .filter(x -> x)
                .subscribe(x -> restartCountdownJobs());

        RxView.clicks(tableRowNotificationTime)
                .flatMap(x -> displayDatePickerDialog(this, R.string.title_notification_time_picker, !dateFormat.endsWith("a")))
                .subscribe(this::updateNotificationDate);
    }

    private void updateNotificationDate(Date date) {
        setNotificationTime(date);
        SharedPreferencesHelper.saveNotificationTime(this, date);
        restartCountdownJobs();
    }

    private void setNotificationTime(Date date) {
        notificationTime = date;
        textViewNotificationTime.setText(timeFormat.format(date));
    }

    private void restartCountdownJobs() {
        // TODO : update jobs
        Toast.makeText(this, "Restart jobs !", Toast.LENGTH_SHORT).show();
//        startService(new Intent(this, AutoStartService.class));
    }

    private Observable<Date> displayDatePickerDialog(Context context, int title, boolean is24HoursFormat) {
        return Observable.create((Subscriber<? super Date> subscriber) -> {
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
