package com.bcch.neilconnatty.streamingplugin.pushNotifications;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bcch.neilconnatty.libstreamingplugin.callbacks.QBSessionCallback;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.util.ArrayList;

/**
 * Created by neilconnatty on 2016-10-17.
 */

public class PushNotificationSubscriber
{
    private static final String TAG = PushNotificationSubscriber.class.getSimpleName();

    public PushNotificationSubscriber () {};

    public static void subscribe (String registrationID, Activity activity, final QBSessionCallback callback) {
        QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
        subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
        //
        String deviceId;
        final TelephonyManager mTelephony = (TelephonyManager) activity.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId(); //*** use for mobiles
        } else {
            deviceId = Settings.Secure.getString(activity.getContentResolver(),
                    Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        subscription.setDeviceUdid(deviceId);
        //
        subscription.setRegistrationID(registrationID);
        //
        QBPushNotifications.createSubscription(subscription, new QBEntityCallback<ArrayList<QBSubscription>>() {

            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                Log.d(TAG, "Success subscribing to push notifications");
                callback.onSuccess();
            }

            @Override
            public void onError(QBResponseException error) {
                Log.e(TAG, "Error subscribing to push notifications");
                callback.onError(error);
            }
        });
    }
}

