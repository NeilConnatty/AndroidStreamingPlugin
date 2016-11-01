package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.Handler;
import android.widget.TextView;

/**
 * Created by neilconnatty on 2016-10-31.
 */

public class Messenger {

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

    /********** Native Methods **********/

    public native int initializeMessenger ();
    public native void stopMessenger ();

}
