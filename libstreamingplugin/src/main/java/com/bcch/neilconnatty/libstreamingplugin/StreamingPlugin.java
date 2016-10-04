package com.bcch.neilconnatty.libstreamingplugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;

/**
 * Created by neilconnatty on 2016-09-28.
 */

public class StreamingPlugin extends CallbacksListener {

    private final String TAG = StreamingPlugin.class.getSimpleName();

    //private boolean calledFromUnity;
    //private TestActivity _testActivity;


    /************ Public Methods ************/

    /** Constructors */
    public StreamingPlugin (Activity currentActivity)
    {
        super(currentActivity);
        //calledFromUnity = true;
    }

    /*
    public StreamingPlugin (TestActivity activity, boolean calledFromUnity)
    {
        super (activity);
        this.calledFromUnity = false;
        _testActivity = activity;
    }
    */

    public void StartStreamingPlugin ()
    {
        initialization();
        return;
    }

    /*
    @Override
    public void onLocalVideoTrackReceive (QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack)
    {
        super.onLocalVideoTrackReceive(qbrtcSession, qbrtcVideoTrack);
        if (calledFromUnity) return;
        _testActivity.renderVideo (qbrtcVideoTrack, false);
    }

    @Override
    public void onRemoteVideoTrackReceive (QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer)
    {
        super.onRemoteVideoTrackReceive(qbrtcSession, qbrtcVideoTrack, integer);
        if (calledFromUnity) return;
        _testActivity.renderVideo (qbrtcVideoTrack, true);
    }
    */


    /*********** Private Methods ************/

    private void initialization ()
    {
        initApp ();
        createSession ();
    }

    private void initApp ()
    {
        QBSettings.getInstance().init (_mActivity.getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey (Consts.ACCOUNT_KEY);
    }

    private String getCurrentDeviceId ()
    {
        return Utils.generateDeviceId(_mActivity);
    }

    private void createSession ()
    {
        UserLoginHelper helper = new UserLoginHelper();
        final QBUser user = helper.createUserWithDefaultData();
        helper.startSignUpNewUser (user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "successfully created session");
                addSignalingManager();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "error creating new session, attempting again");
                createSession();
            }
        });
    }

    private void addSignalingManager ()
    {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(_mActivity).addSignaling((QBWebRTCSignaling) qbSignaling);
                        }
                        registerCallbacksListener();
                    }
                });
    }


    private void registerCallbacksListener ()
    {
        QBRTCClient rtcClient = QBRTCClient.getInstance(_mActivity);
        rtcClient.addSessionCallbacksListener(this);
        rtcClient.prepareToProcessCalls();
    }
}
