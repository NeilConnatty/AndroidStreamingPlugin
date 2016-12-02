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
    private RemoteInputCallbackListener _listener;
    private MessageUIUpdater _uiUpdater;

    static {
        System.loadLibrary("MessagingService");
    }

    public Messenger (Handler handler, TextView textView, RemoteInputCallbackListener listener)
    {
        _handler = handler;
        _listener = listener;
        _uiUpdater = new MessageUIUpdater(textView, _handler);
    }

    public void displayMessage (String msg)
    {
        _uiUpdater.postMessage(msg);
    }

    public void receiveInput (String input)
    {
        _handler.post(new RemoteInputHandle(input, _listener));
    }

    /********** Native Methods **********/

    public native int initializeMessenger ();
    public native void stopMessenger ();

}
