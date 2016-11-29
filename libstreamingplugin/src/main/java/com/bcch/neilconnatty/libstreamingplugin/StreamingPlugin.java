package com.bcch.neilconnatty.libstreamingplugin;

import android.os.Bundle;
import android.util.Log;

import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.libstreamingplugin.callbacks.QBSessionCallback;
import com.bcch.neilconnatty.libstreamingplugin.utils.CallbacksListener;
import com.bcch.neilconnatty.libstreamingplugin.utils.Consts;
import com.bcch.neilconnatty.libstreamingplugin.utils.UserLoginHelper;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Utils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.HashMap;
import java.util.Map;

import static com.quickblox.videochat.webrtc.QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;

/**
 * Created by neilconnatty on 2016-09-28.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class StreamingPlugin extends CallbacksListener {

    private final String TAG = StreamingPlugin.class.getSimpleName();


    /************ Public Methods ************/

    /** Constructors */
    public StreamingPlugin (BaseActivity currentActivity)
    {
        super(currentActivity);
        registerCallback(new StreamingCallbackImpl());
    }


    public void StartStreamingPlugin (QBSessionCallback callback)
    {
        createSession(callback);
    }

    public void initApp ()
    {
        QBSettings.getInstance().init(_baseActivity.getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);
    }

    public void endStreaming(final PluginCallback callback)
    {
        registerCallback(new StreamingCallback() {
            @Override
            public void onCallEnded() {
                callback.onStreamingEnded();
                unregisterCallback(this);
            }
        });

        Map<String,String> userInfo = new HashMap<>();
        userInfo.put("Key", "Value");

        currentSession.hangUp(userInfo);
    }

    @Override
    public void onLocalVideoTrackReceive (QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack)
    {
        super.onLocalVideoTrackReceive(qbrtcSession, qbrtcVideoTrack);
        Log.d (TAG, "onLocalVideoTrackReceive()");

        _baseActivity.renderVideo (qbrtcVideoTrack, false);
    }

    @Override
    public void onRemoteVideoTrackReceive (QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer)
    {
        super.onRemoteVideoTrackReceive(qbrtcSession, qbrtcVideoTrack, integer);
        Log.d (TAG, "onRemoteVideoTrackReceive()");
        _baseActivity.renderVideo (qbrtcVideoTrack, true);
    }


    /*********** Private Methods ************/

    private String getCurrentDeviceId ()
    {
        return Utils.generateDeviceId(_baseActivity);
    }

    private void createSession (final QBSessionCallback callback)
    {
        final UserLoginHelper helper = new UserLoginHelper();
        final QBUser user = helper.createUserWithDefaultData();
        helper.startSignUpNewUser (user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "successfully created session");
                addSignalingManager(callback);
                callback.onSuccess();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "error creating new session");
                callback.onError(e);
            }
        });
    }

    private void addSignalingManager (final QBSessionCallback callback)
    {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(_baseActivity).addSignaling((QBWebRTCSignaling) qbSignaling);
                        }
                        registerCallbacksListener();
                    }
                });
    }


    private void registerCallbacksListener ()
    {
        QBRTCClient rtcClient = QBRTCClient.getInstance(_baseActivity);
        rtcClient.addSessionCallbacksListener(this);
        rtcClient.prepareToProcessCalls();
        Log.d (TAG, "signalling manager and callbacks listener registered");
    }


    /********** Nested Classes **********/

    private class StreamingCallbackImpl implements StreamingCallback
    {
        public void onCallEnded ()
        {
            _baseActivity.onCallEnded();
        }
    }

    public interface PluginCallback {
        void onStreamingEnded();
    }
}
