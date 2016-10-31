package com.bcch.neilconnatty.streamingplugin.messaging;

import android.util.Log;

/**
 * Created by neilconnatty on 2016-10-31.
 */

class MessageUIUpdater implements Runnable
{
    private final String TAG = MessageUIUpdater.class.getSimpleName();

    private String _message;

    MessageUIUpdater (String message)
    {
        _message = message;
    }

    @Override
    public void run()
    {
        Log.d(TAG, "Message received: " + _message);
    }
}
