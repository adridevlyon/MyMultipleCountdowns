package com.myadridev.mymultiplecountdowns.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = CountdownItem.class)
public class CountdownItem implements Comparable<CountdownItem> {

    public int id;
    public String label;
    public int delay;
    public int duration;

    @Override
    public int compareTo(@NonNull CountdownItem otherItem) {
        int compare = id - otherItem.id;
        if (compare < 0) {
            return -1;
        } else if (compare > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
