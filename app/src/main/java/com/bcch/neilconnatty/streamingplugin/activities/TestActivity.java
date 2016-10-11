package com.bcch.neilconnatty.streamingplugin.activities;

import android.os.Bundle;

import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.crashlytics.android.Crashlytics;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import io.fabric.sdk.android.Fabric;

public class TestActivity extends BaseActivity {

    private final String TAG = TestActivity.class.getSimpleName();

    /*
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_test);

        setViewReferences();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        plugin.StartStreamingPlugin ();
    }

    @Override
    protected void setViewReferences()
    {
        opponentView = (QBRTCSurfaceView) findViewById(R.id.opponentView);
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }


    /************ Public Methods *************/


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.

    public native String stringFromJNI();
    */

    /************* Private Methods ************/

}
