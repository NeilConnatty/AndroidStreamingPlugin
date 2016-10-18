package com.bcch.neilconnatty.streamingplugin;

import android.app.Application;

import com.bcch.neilconnatty.streamingplugin.activities.ActivityLifecycle;

/**
 * Created by neilconnatty on 2016-10-18.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class App extends Application
{
    private static App instance;

    @Override
    public void onCreate ()
    {
        super.onCreate();
        instance = this;
        ActivityLifecycle.init(this);
    }

    public static synchronized App getInstance ()
    {
        return instance;
    }
}
