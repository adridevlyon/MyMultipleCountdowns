package com.myadridev.mymultiplecountdowns.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TitleCountdownItemViewHolder extends RecyclerView.ViewHolder {
    private TitleCountdownItemViewHolder(View itemView) {
        super(itemView);
    }

    public static TitleCountdownItemViewHolder newInstance(View view) {
        return new TitleCountdownItemViewHolder(view);
    }
}
