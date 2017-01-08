package com.myadridev.mymultiplecountdowns.activity;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.adapter.CountdownItemsAdapter;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity {

    private HeaderManager headerManager;
    private CoordinatorLayout coordinatorLayout;
    private Button buttonGo;
    private Button buttonCancel;
    private TableRow tableRowNotificationTime;
    private TextView textViewNotificationTime;
    private TextView textViewLastLaunchDate;

    private RecyclerView recyclerViewCountdownList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        headerManager = new HeaderManager(this, coordinatorLayout, buttonGo, buttonCancel, textViewNotificationTime, tableRowNotificationTime, textViewLastLaunchDate);
        loadStoredData();
        headerManager.setEvents(this);
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
        headerManager.loadStoredData(this);
        recyclerViewCountdownList.setLayoutManager(new LinearLayoutManager(this));
        CountdownItemsAdapter recyclerViewCountdownAdapter = new CountdownItemsAdapter(this, coordinatorLayout, SharedPreferencesHelper.loadCountdownItems(this));
        recyclerViewCountdownList.setAdapter(recyclerViewCountdownAdapter);
    }
}
