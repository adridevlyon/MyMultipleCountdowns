package com.myadridev.mymultiplecountdowns.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.myadridev.mymultiplecountdowns.R;
import com.myadridev.mymultiplecountdowns.helper.DialogHelper;
import com.myadridev.mymultiplecountdowns.helper.SharedPreferencesHelper;
import com.myadridev.mymultiplecountdowns.model.CountdownItem;
import com.myadridev.mymultiplecountdowns.viewHolder.CountdownItemViewHolder;
import com.myadridev.mymultiplecountdowns.viewHolder.CreateCountdownItemViewHolder;
import com.myadridev.mymultiplecountdowns.viewHolder.TitleCountdownItemViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class CountdownItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final CoordinatorLayout coordinatorLayout;
    private final List<CountdownItem> countdownItems;

    public CountdownItemsAdapter(Context context, CoordinatorLayout coordinatorLayout, List<CountdownItem> countdownItems) {
        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
        this.layoutInflater = LayoutInflater.from(this.context);
        this.countdownItems = countdownItems != null ? countdownItems : new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return countdownItems.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            // title
            return 0;
        }
        if (position == getItemCount() - 1) {
            // create new item
            return 2;
        }
        // item
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                // title
                view = layoutInflater.inflate(R.layout.item_title_countdown, parent, false);
                return TitleCountdownItemViewHolder.newInstance(view);
            case 1:
                // item
                view = layoutInflater.inflate(R.layout.item_countdown, parent, false);
                return CountdownItemViewHolder.newInstance(view);
            case 2:
                // create new item
                view = layoutInflater.inflate(R.layout.item_create_countdown, parent, false);
                return CreateCountdownItemViewHolder.newInstance(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            // title
            // do nothing
        } else if (position == getItemCount() - 1) {
            // create new item
            CreateCountdownItemViewHolder createItemHolder = (CreateCountdownItemViewHolder) holder;
            RxView.clicks(createItemHolder.getItemView())
                    .flatMap(x -> displayEditItemDialog(context, position, new CountdownItem()))
                    .filter(x -> x != null)
                    .flatMap(this::saveCountdownItem)
                    .subscribe(this::addDisplayedCountdownItem, e -> displayError(e.getMessage()));
        } else {
            // item
            CountdownItemViewHolder itemHolder = (CountdownItemViewHolder) holder;
            CountdownItem item = countdownItems.get(position - 1);
            if (item != null) {
                itemHolder.setLabel(item.label);
                itemHolder.setDelay(item.delay);
                itemHolder.setDuration(item.duration);

                RxView.clicks(itemHolder.getEditView())
                        .flatMap(x -> displayEditItemDialog(context, position, item))
                        .filter(x -> x != null)
                        .flatMap(this::saveCountdownItem)
                        .subscribe(x -> editDisplayedCountdownItem(position, x), e -> displayError(e.getMessage()));
                RxView.clicks(itemHolder.getDeleteView())
                        .flatMap(x -> DialogHelper.displayAlertDialog(context, R.string.title_countdown_delete_confirmation, R.string.message_countdown_delete_confirmation, R.string.ok, R.string.no))
                        .filter(x -> x)
                        .flatMap(x -> removeCountdownItem(position))
                        .subscribe(this::removeDisplayedCountdown);
            }
        }
    }

    private Observable<CountdownItem> displayEditItemDialog(Context context, int position, CountdownItem item) {
        return Observable.create((Subscriber<? super CountdownItem> subscriber) -> {
            final Dialog createCountdownItemDialog = new Dialog(context);
            createCountdownItemDialog.setContentView(R.layout.dialog_create_countdown);

            EditText labelView = (EditText) createCountdownItemDialog.findViewById(R.id.create_countdown_label);
            EditText delayView = (EditText) createCountdownItemDialog.findViewById(R.id.create_countdown_delay);
            EditText durationView = (EditText) createCountdownItemDialog.findViewById(R.id.create_countdown_duration);
            ImageView cancelView = (ImageView) createCountdownItemDialog.findViewById(R.id.imageview_cancel);
            ImageView confirmView = (ImageView) createCountdownItemDialog.findViewById(R.id.imageview_confirm);

            CountdownItem outputItem = new CountdownItem();

            if (item.id != 0) {
                labelView.setText(item.label);
                delayView.setText(String.valueOf(item.delay));
                durationView.setText(String.valueOf(item.duration));
            } else {
                item.id = position;
            }

            RxView.clicks(cancelView).subscribe(x -> {
                subscriber.onNext(null);
                subscriber.onCompleted();
            });

            RxView.clicks(confirmView).subscribe(x -> {
                outputItem.id = item.id;
                outputItem.label = labelView.getText().toString();
                String delayString = delayView.getText().toString();
                outputItem.delay = delayString.isEmpty() ? 0 : Integer.valueOf(delayString);
                String durationString = durationView.getText().toString();
                outputItem.duration = durationString.isEmpty() ? 0 : Integer.valueOf(durationString);

                subscriber.onNext(outputItem);
                subscriber.onCompleted();
            });

            // cleaning up
            subscriber.add(Subscriptions.create(createCountdownItemDialog::dismiss));
            createCountdownItemDialog.show();
        });
    }

    private Observable<CountdownItem> saveCountdownItem(CountdownItem item) {
        return Observable.create((Subscriber<? super CountdownItem> subscriber) -> {
            if (SharedPreferencesHelper.saveCountdownItems(context, item)) {
                subscriber.onNext(item);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new BackingStoreException(context.getString(R.string.save_countdownitem_error)));
                subscriber.onCompleted();
            }
        });
    }

    private void addDisplayedCountdownItem(CountdownItem item) {
        countdownItems.add(item);
        notifyDataSetChanged();
    }

    private void editDisplayedCountdownItem(int position, CountdownItem item) {
        countdownItems.set(position - 1, item);
        notifyDataSetChanged();
    }

    private Observable<Integer> removeCountdownItem(int position) {
        return Observable.create((Subscriber<? super Integer> subscriber) -> {
            SharedPreferencesHelper.removeCountdownItem(context, position);
            subscriber.onNext(position);
            subscriber.onCompleted();
        });
    }

    private void removeDisplayedCountdown(int position) {
        countdownItems.remove(position - 1);
        notifyDataSetChanged();
    }

    private void displayError(String errorMessage) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.ok, v -> snackbar.dismiss());
        snackbar.show();
    }
}