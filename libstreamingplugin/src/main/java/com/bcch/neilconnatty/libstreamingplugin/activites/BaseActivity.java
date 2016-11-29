package com.bcch.neilconnatty.libstreamingplugin.activites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

public abstract class BaseActivity extends AppCompatActivity
{
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected QBRTCSurfaceView opponentView;
    protected QBRTCSurfaceView localView;

    protected boolean streamRendering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /*********** Public Methods **********/

    public void renderVideo (QBRTCVideoTrack videoTrack, boolean remoteRenderer)
    {
        if (remoteRenderer) {
            if (opponentView != null)
                fillVideoView(opponentView, videoTrack, remoteRenderer);
        } else {
            if (localView != null)
                fillVideoView(localView, videoTrack, remoteRenderer);
        }

        streamRendering = true;
    }

    public void onCallEnded ()
    {
        streamRendering = false;
    }


    /*********** Protected Methods **********/

    protected abstract void setViewReferences ();


    protected void fillVideoView(QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer)
    {
        Log.e (TAG, "fillVideoView");
        videoTrack.removeRenderer(videoTrack.getRenderer());
        videoTrack.addRenderer(new VideoRenderer(videoView));

        if (!remoteRenderer) {
            updateVideoView(videoView, false);
        }
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror)
    {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    private void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, RendererCommon.ScalingType scalingType)
    {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalingType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }
}
