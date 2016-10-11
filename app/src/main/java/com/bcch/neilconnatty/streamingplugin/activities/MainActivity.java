package com.bcch.neilconnatty.streamingplugin.activities;

import android.os.Bundle;

import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.streamingplugin.R;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void setViewReferences()
    {
        opponentView = (QBRTCSurfaceView) findViewById(R.id.opponentView);
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }
}
