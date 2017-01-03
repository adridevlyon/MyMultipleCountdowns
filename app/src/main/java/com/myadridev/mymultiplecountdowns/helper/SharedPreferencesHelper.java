package com.myadridev.mymultiplecountdowns.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myadridev.mymultiplecountdowns.model.CountdownItem;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SharedPreferencesHelper {

    private static final int openMode = Context.MODE_PRIVATE;
    private static final String parametersFileName = "params";
    private static final String notificationTimeKey = "notificationTime";

    private static final String countdownItemsFileName = "items";
    private static final String countdownItemKey = "item_";

    private static final int defaultNotificationHour = 10;
    private static final int defaultNotificationMinute = 0;
    private static final String defaultNotificationTime = "10:00";
    private static final SimpleDateFormat storageDateFormat = new SimpleDateFormat("HH:mm");

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static Date loadNotificationTime(Context context) {
        SharedPreferences storedSettings = context.getSharedPreferences(parametersFileName, openMode);

        String notificationTime = storedSettings.getString(notificationTimeKey, defaultNotificationTime);
        Date date;
        try {
            date = storageDateFormat.parse(notificationTime);
        } catch (ParseException e) {
            date = null;
        }
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, defaultNotificationHour);
            cal.set(Calendar.MINUTE, defaultNotificationMinute);
        }
        return cal.getTime();
    }

    public static List<CountdownItem> loadCountdownItems(Context context) {
        List<CountdownItem> items = new ArrayList<>();
        SharedPreferences storedSettings = context.getSharedPreferences(countdownItemsFileName, openMode);
        Map<String, ?> storedItems = storedSettings.getAll();
        for (Object storedItem : storedItems.values()) {
            if (!(storedItem instanceof String)) continue;

            CountdownItem item;
            try {
                item = jsonMapper.readValue((String) storedItem, CountdownItem.class);
                items.add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(items);
        return items;
    }

    public static void saveNotificationTime(Context context, Date date) {
        SharedPreferences storedSettings = context.getSharedPreferences(parametersFileName, openMode);
        SharedPreferences.Editor editor = storedSettings.edit();
        editor.putString(notificationTimeKey, storageDateFormat.format(date));
        editor.apply();
    }

    public static boolean saveCountdownItems(Context context, CountdownItem item) {
        SharedPreferences storedSettings = context.getSharedPreferences(countdownItemsFileName, openMode);
        String itemAsJson;
        try {
            itemAsJson = jsonMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        SharedPreferences.Editor editor = storedSettings.edit();
        editor.putString(countdownItemKey + item.id, itemAsJson);
        editor.apply();
        return true;
    }

    public static void removeCountdownItem(Context context, int itemId) {
        SharedPreferences storedSettings = context.getSharedPreferences(countdownItemsFileName, openMode);
        SharedPreferences.Editor editor = storedSettings.edit();
        editor.remove(countdownItemKey + itemId);
        editor.apply();
    }
}
