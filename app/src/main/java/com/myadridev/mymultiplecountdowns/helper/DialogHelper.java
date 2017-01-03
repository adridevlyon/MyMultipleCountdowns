package com.myadridev.mymultiplecountdowns.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class DialogHelper {

    public static Observable<Boolean> displayAlertDialog(Context context, int title, int message, int positiveButton, int negativeButton) {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {
            final AlertDialog alertDialogBuilder = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveButton, (dialog, which) -> {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    })
                    .setNegativeButton(negativeButton, (dialog, which) -> {
                        subscriber.onNext(false);
                        subscriber.onCompleted();
                    })
                    .create();
            // cleaning up
            subscriber.add(Subscriptions.create(alertDialogBuilder::dismiss));
            alertDialogBuilder.show();
        });
    }
}
