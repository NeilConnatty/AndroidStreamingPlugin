package com.bcch.neilconnatty.streamingplugin.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.crashlytics.android.Crashlytics;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import io.fabric.sdk.android.Fabric;

public class TestActivity extends AppCompatActivity {

    private final String TAG = TestActivity.class.getSimpleName();

    private boolean isCurrentCameraFront = true;

    /*
    private QBRTCSurfaceView opponentView;
    private QBRTCSurfaceView localView;
    */
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
        /*
        opponentView = (QBRTCSurfaceView) findViewById(R.id.opponentView);
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
        */
        StreamingPlugin plugin = new StreamingPlugin (this);
        plugin.StartStreamingPlugin ();
    }


    /************ Public Methods *************/

    /*
    public void renderVideo (QBRTCVideoTrack videoTrack, boolean remoteRenderer)
    {
        if (remoteRenderer) {
            fillVideoView(opponentView, videoTrack, remoteRenderer);
        } else {
            fillVideoView(localView, videoTrack, remoteRenderer);
        }
    }
    */

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.

    public native String stringFromJNI();
    */

    /************* Private Methods ************/

    /*
    private void fillVideoView(QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.removeRenderer(videoTrack.getRenderer());
        videoTrack.addRenderer(new VideoRenderer(videoView));

        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront);
        }
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, RendererCommon.ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalingType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }
    */
}
