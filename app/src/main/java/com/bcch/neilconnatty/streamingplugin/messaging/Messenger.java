package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by neilconnatty on 2016-10-31.
 */

public class Messenger {

    private final String TAG = Messenger.class.getSimpleName();

    private Handler _handler;
    private TextView _textView;

    static {
        System.loadLibrary("MessagingService");
    }

    public Messenger (Handler handler, TextView textView)
    {
        _handler = handler;
        _textView = textView;
    }

    public void displayMessage (String msg)
    {
        _handler.post(new MessageUIUpdater(msg, _textView, _handler));
    }

    public void receiveInput (String input)
    {
        Log.d(TAG, "Received input: " + input);
    }

    /********** Native Methods **********/

    public native int initializeMessenger ();
    public native void stopMessenger ();

}
