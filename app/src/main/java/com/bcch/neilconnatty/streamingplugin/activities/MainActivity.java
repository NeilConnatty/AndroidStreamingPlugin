package com.bcch.neilconnatty.streamingplugin.activities;

import android.os.Bundle;
import android.view.Window;

import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.streamingplugin.R;
import com.crashlytics.android.Crashlytics;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        setViewReferences();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        plugin.StartStreamingPlugin ();
    }

    @Override
    protected void setViewReferences()
    {
        opponentView = null;
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }
}
