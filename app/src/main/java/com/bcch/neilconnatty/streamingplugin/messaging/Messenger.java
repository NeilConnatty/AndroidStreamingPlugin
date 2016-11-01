package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.Handler;
import android.widget.TextView;

import com.bcch.neilconnatty.streamingplugin.messaging.remoteInput.RemoteInputCallbackListener;
import com.bcch.neilconnatty.streamingplugin.messaging.remoteInput.RemoteInputHandle;

/**
 * Created by neilconnatty on 2016-10-31.
 */

public class Messenger {

    private final String TAG = Messenger.class.getSimpleName();

    private Handler _handler;
    private TextView _textView;
    private RemoteInputCallbackListener _listener;

    static {
        System.loadLibrary("MessagingService");
    }

    public Messenger (Handler handler, TextView textView, RemoteInputCallbackListener listener)
    {
        _handler = handler;
        _textView = textView;
        _listener = listener;
    }

    public void displayMessage (String msg)
    {
        _handler.post(new MessageUIUpdater(msg, _textView, _handler));
    }

    public void receiveInput (String input)
    {
        _handler.post(new RemoteInputHandle(input, _listener));
    }

    /********** Native Methods **********/

    public native int initializeMessenger ();
    public native void stopMessenger ();

}
