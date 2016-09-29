package com.bcch.neilconnatty.streamingplugin.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.utils.Consts;
import com.bcch.neilconnatty.streamingplugin.utils.ListenerTestImplementation;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private boolean isCurrentCameraFront = true;
    private QBRTCSurfaceView opponentView;
    private QBRTCSurfaceView localView;
    private QBChatService chatService;

    private ListenerTestImplementation listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        opponentView = (QBRTCSurfaceView) findViewById(R.id.opponentView);
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);

        listener = new ListenerTestImplementation (this);

        initialization();
    }

    /************ Public Methods *************/

    public void renderVideo (QBRTCVideoTrack videoTrack, boolean remoteRenderer)
    {
        if (remoteRenderer) {
            fillVideoView(opponentView, videoTrack, remoteRenderer);
        } else {
            fillVideoView(localView, videoTrack, remoteRenderer);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.

     public native String stringFromJNI();
     */

    /************* Private Methods ************/

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

    /*********** Private Methods ************/

    private void initialization ()
    {
        initApp ();
        createSession ();
        addSignalingManager ();

        registerCallbacksListener ();
    }

    private void initApp ()
    {
        QBSettings.getInstance().init (getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey (Consts.ACCOUNT_KEY);
    }

    private void createSession ()
    {
        final QBUser user = new QBUser (Consts.DEFAULT_USER_LOGIN, Consts.DEFAULT_USER_PASSWORD);

        QBAuth.createSession(Consts.DEFAULT_USER_LOGIN, Consts.DEFAULT_USER_PASSWORD, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                Log.e(TAG, "createSession onSuccess()");

                user.setId (qbSession.getId());
                chatService = QBChatService.getInstance();

                chatService.login(user, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        // success
                        Log.e(TAG, "login onSuccess()");
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        throw new RuntimeException ("Error Logging in");
                        //Log.e(TAG, "Login onError()");
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                throw new RuntimeException ("Error creating session");
                //Log.e(TAG, "createSession onError()");
                //createSession();
            }
        });
    }

    private void addSignalingManager()
    {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(getApplicationContext()).addSignaling((QBWebRTCSignaling) qbSignaling);
                        }
                    }
                });
    }


    private void registerCallbacksListener()
    {
        QBRTCClient rtcClient = QBRTCClient.getInstance(this);
        rtcClient.addSessionCallbacksListener(listener);
        rtcClient.prepareToProcessCalls();
    }
}
