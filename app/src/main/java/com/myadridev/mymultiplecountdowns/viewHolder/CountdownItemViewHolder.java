package com.myadridev.mymultiplecountdowns.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.myadridev.mymultiplecountdowns.R;

public class CountdownItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView labelView;
    private final TextView delayView;
    private final TextView durationView;
    private final ImageView editView;
    private final ImageView deleteView;

    private CountdownItemViewHolder(View itemView) {
        super(itemView);
        labelView = (TextView) itemView.findViewById(R.id.countdown_label);
        delayView = (TextView) itemView.findViewById(R.id.countdown_delay);
        durationView = (TextView) itemView.findViewById(R.id.countdown_duration);
        editView = (ImageView) itemView.findViewById(R.id.countdown_edit);
        deleteView = (ImageView) itemView.findViewById(R.id.countdown_delete);
    }

    public static CountdownItemViewHolder newInstance(View view) {
        return new CountdownItemViewHolder(view);
    }

    public void setLabel(String label) {
        labelView.setText(label);
    }

    public void setDelay(int delay) {
        delayView.setText(String.valueOf(delay));
    }

    public void setDuration(int duration) {
        durationView.setText(String.valueOf(duration));
    }

    public ImageView getEditView() {
        return editView;
    }

    public ImageView getDeleteView() {
        return deleteView;
    }
}
