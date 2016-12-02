package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by neilconnatty on 2016-10-31.
 */

class MessageUIUpdater
{
    private final String TAG = MessageUIUpdater.class.getSimpleName();

    // amount of time the notification is displayed in milliseconds
    private final long TIME_DISPLAYED = 10000;

    private TextView _textView;
    private Handler _handler;

    private CountDownTimer _timer;
    private boolean _timerFinished;
    private final ReentrantLock mutex = new ReentrantLock();

    MessageUIUpdater (TextView textView, Handler handler)
    {
        _textView = textView;
        _handler = handler;
        _timerFinished = true;
    }

    void postMessage (final String message)
    {
        Log.d(TAG, "Message received: " + message);
        mutex.lock();
        try {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    _textView.setText(message);
                    _textView.setVisibility(View.VISIBLE);
                }
            });

            if (!_timerFinished) {
                Log.d(TAG, "cancelling previous timer");
                _timer.cancel();
            }
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    spawnAsyncTask();
                }
            });
        } finally {
            mutex.unlock();
        }
    }

    private void spawnAsyncTask ()
    {
        mutex.lock();
        try {
            _timer = new CountDownTimerImpl(TIME_DISPLAYED, TIME_DISPLAYED);
            _timerFinished = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _timer.start();
                }
            }).run();
        } finally {
            mutex.unlock();
        }
    }

    private class CountDownTimerImpl extends CountDownTimer
    {
        CountDownTimerImpl (long millisInFuture, long interval)
        {
            super(millisInFuture, interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mutex.lock();
            try {
                _timerFinished = true;
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _textView.setVisibility(View.INVISIBLE);
                    }
                });
            } finally {
                mutex.unlock();
            }
        }
    }
}
