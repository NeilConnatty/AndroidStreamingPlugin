package com.bcch.neilconnatty.streamingplugin.pushNotifications;

import android.app.Service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by neilconnatty on 2016-10-17.
 */
public class MyFcmListenerService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map data = message.getData();
    }
}
