package com.bcch.neilconnatty.streamingplugin.utils;


import android.app.Activity;
import android.app.Service;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.Map;

/**
 * Created by neilconnatty on 2016-09-28.
 */

public abstract class CallbacksListener implements QBRTCSessionConnectionCallbacks, QBRTCClientVideoTracksCallbacks, QBRTCClientSessionCallbacks
{
    protected Activity _mActivity;
    protected QBRTCSession rtcSession;

    /** Constructor */
    public CallbacksListener (Activity currentActivity)
    {
        _mActivity = currentActivity;
    }

    /************** QBRTCSessionConnectionCallbacks ***********/

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        rtcSession = qbrtcSession;
        qbrtcSession.addVideoTrackCallbacksListener(this);
        qbrtcSession.addSessionCallbacksListener(this);

    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        rtcSession = null;
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {

    }


    /************ QBRTCClientVideoTracksCallBacks ***********/

    @Override
    public abstract void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack);

    @Override
    public abstract void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer);


    /************* QBRTCClientSessionCallbacks *************/

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }
}
