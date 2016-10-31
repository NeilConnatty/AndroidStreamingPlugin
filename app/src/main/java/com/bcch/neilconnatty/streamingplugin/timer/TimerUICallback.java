package com.bcch.neilconnatty.streamingplugin.timer;

import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by neilconnatty on 2016-10-31.
 */

public class TimerUICallback implements TimerCallback
{
    private TextView _textView;

    public TimerUICallback (TextView textView)
    {
        _textView = textView;
    }

    @Override
    public void onTimerTick() {
        String time = getCurrentTime();
        _textView.setText(time);
    }

    private String getCurrentTime ()
    {
        Calendar c = Calendar.getInstance();
        String hour = String.valueOf(c.get(Calendar.HOUR));
        String min  = String.valueOf(c.get(Calendar.MINUTE));
        String sec  = String.valueOf(c.get(Calendar.SECOND));
        if (min.length() == 1) min = "0"+min;
        if (sec.length() == 1) sec = "0"+sec;
        return hour+":"+min+":"+sec;
    }
}
