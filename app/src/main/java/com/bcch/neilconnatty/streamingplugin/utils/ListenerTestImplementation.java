package com.bcch.neilconnatty.streamingplugin.utils;

import android.app.Activity;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

/**
 * Created by neilconnatty on 2016-09-28.
 */

public class ListenerTestImplementation extends CallbacksListener {

    public ListenerTestImplementation (Activity activity)
    {
        super(activity);
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }
}
