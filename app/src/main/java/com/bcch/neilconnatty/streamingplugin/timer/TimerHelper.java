package com.bcch.neilconnatty.streamingplugin.timer;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by neilconnatty on 2016-10-25.
 */

public class TimerHelper
{
    public void createTimer (final Handler handler, final TimerCallback callback,
                                     final long period)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onTimerTick();
                } finally {
                    handler.postDelayed(this, period);
                }
            }
        };
        handler.post(runnable);
    }

    public Timer createTimer (final Handler handler, final TimerCallback callback,
                              final long delay, final long period)
    {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.onTimerTick();
            }
        };
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, delay, period);
        return timer;
    }
}
