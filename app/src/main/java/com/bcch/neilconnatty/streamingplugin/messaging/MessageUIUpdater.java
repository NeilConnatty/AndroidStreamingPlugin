package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by neilconnatty on 2016-10-31.
 */

class MessageUIUpdater implements Runnable
{
    private final String TAG = MessageUIUpdater.class.getSimpleName();

    // amount of time the notification is displayed in milliseconds
    private final int TIME_DISPLAYED = 10000;

    private String _message;
    private TextView _textView;
    private Handler _handler;

    MessageUIUpdater (String message, TextView textView, Handler handler)
    {
        _message = message;
        _textView = textView;
        _handler = handler;
    }

    @Override
    public void run ()
    {
        Log.d(TAG, "Message received: " + _message);
        _textView.setText(_message);
        _textView.setVisibility(View.VISIBLE);
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _textView.setVisibility(View.INVISIBLE);
            }
        }, TIME_DISPLAYED);
    }
}
