package com.bcch.neilconnatty.streamingplugin.messaging;

import android.os.Handler;

/**
 * Created by neilconnatty on 2016-10-31.
 */

public class Messenger {

    private Handler _handler;

    static {
        System.loadLibrary("MessagingService");
    }

    public Messenger (Handler handler)
    {
        _handler = handler;
    }

    public void displayMessage (String msg)
    {
        _handler.post(new MessageUIUpdater(msg));
    }

    /********** Native Methods **********/

    public native int initializeMessenger ();
    public native void stopMessenger ();

}
