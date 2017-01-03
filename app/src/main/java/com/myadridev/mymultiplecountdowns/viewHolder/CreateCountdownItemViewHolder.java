package com.myadridev.mymultiplecountdowns.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CreateCountdownItemViewHolder extends RecyclerView.ViewHolder {
    private CreateCountdownItemViewHolder(View itemView) {
        super(itemView);
    }

    public static CreateCountdownItemViewHolder newInstance(View view) {
        return new CreateCountdownItemViewHolder(view);
    }

    public View getItemView() {
        return itemView;
    }
}
