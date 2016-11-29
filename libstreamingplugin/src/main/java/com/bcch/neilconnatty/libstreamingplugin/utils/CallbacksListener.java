package com.bcch.neilconnatty.libstreamingplugin.utils;


import android.support.annotation.CallSuper;
import android.util.Log;

import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neilconnatty on 2016-09-28.
 */

public abstract class CallbacksListener implements QBRTCSessionConnectionCallbacks, QBRTCClientVideoTracksCallbacks, QBRTCClientSessionCallbacks {
    private final String TAG = CallbacksListener.class.getSimpleName();

    protected BaseActivity _baseActivity;

    protected QBRTCSession currentSession;
    protected List<Integer> _opponents;

    private List<StreamingCallback> streamingCallbacks = null;
    private boolean inCurrentSession = false;


    /**
     * Constructor
     */
    public CallbacksListener(BaseActivity currentActivity) {
        _baseActivity = currentActivity;
        streamingCallbacks = new ArrayList<>();
    }

    protected void registerCallback (StreamingCallback callback)
    {
        streamingCallbacks.add(callback);
    }

    protected void unregisterCallback (StreamingCallback callback)
    {
        streamingCallbacks.remove(callback);
    }

    protected boolean isInCurrentSession () { return inCurrentSession; }

    /**************
     * QBRTCClientSessionCallbacks
     ***********/

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onReceiveNewSession");
        if (inCurrentSession) {
            Log.d(TAG, "In current session, ignoring incoming call");
            Map<String, String> userInfo = new HashMap<String, String>();
            userInfo.put("Key", "Value");
            qbrtcSession.rejectCall(userInfo);
            return;
        }

        _opponents = qbrtcSession.getOpponents();

        qbrtcSession.addVideoTrackCallbacksListener(this);
        qbrtcSession.addSessionCallbacksListener(this);

        // Set userInfo
        // User can set any string key and value in user info
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("Key", "Value");

        // Accept incoming call
        qbrtcSession.acceptCall(userInfo);
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNotAnswer");
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallRejectByUser");
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallAcceptByUser");
        _opponents = qbrtcSession.getOpponents();

        qbrtcSession.addVideoTrackCallbacksListener(this);
        qbrtcSession.addSessionCallbacksListener(this);
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onReceiveHangUpFromUser");

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "OnUserNoActions");
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionClosed");
        inCurrentSession = false;
        for (StreamingCallback callback : streamingCallbacks) {
            callback.onCallEnded();
        }
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionStartClose");
        qbrtcSession.removeSessionCallbacksListener(this);
        qbrtcSession.removeVideoTrackCallbacksListener(this);
        currentSession = null;
    }


    /************
     * QBRTCSessionConnectionCallbacks
     ***********/

    @Override
    @CallSuper
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive");
    }


    @Override
    @CallSuper
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        Log.d(TAG, "onRemoteVideoTrackReceive");
    }


    /*************
     * QBRTCClientSessionCallbacks
     *************/

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onStartConnectToUser");
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onConnectedToUser");
        inCurrentSession = true;
        currentSession = qbrtcSession;

        QBMediaStreamManager mediaStreamManager = qbrtcSession.getMediaStreamManager();
        mediaStreamManager.setAudioEnabled(true);
        mediaStreamManager.changeCaptureFormat(1080, 720, 30);
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onConnectionClosedForUser");
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onDisconnectedFromUser");
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onDisconnectedTimeoutFromUser");
        currentSession = null;
        inCurrentSession = false;
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onConnectionFailedWithUser");
        currentSession = null;
        inCurrentSession = false;

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        Log.d(TAG, "onError");
    }


    /**********
     * Nested Classes
     ***********/

    public interface StreamingCallback
    {
        void onCallEnded ();
    }

}