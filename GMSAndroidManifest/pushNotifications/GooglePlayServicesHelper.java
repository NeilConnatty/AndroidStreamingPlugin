package com.bcch.neilconnatty.streamingplugin.pushNotifications;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.bcch.neilconnatty.streamingplugin.App;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.constants.GcmConsts;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Utils;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by neilconnatty on 2016-10-17.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class GooglePlayServicesHelper
{
    private static final String TAG = GooglePlayServicesHelper.class.getSimpleName();

    private static final String PREF_APP_VERSION = "appVersion";
    private static final String PREF_GCM_REG_ID = "registration_id";

    private static final int PLAY_SERVICES_REQUEST_CODE = 9000;

    public static String getGcmSenderId ()
    {
        return GcmConsts.GCM_SENDER_ID;
    }

    private String _gcmRegId = null;


    /*********** Public Methods **********/

    public void registerForGcm (String senderId)
    {
        if (_gcmRegId == null) {
            registerInGcmInBackground(senderId, App.getInstance());
        }
    }

    public void unregisterFromGcm (String senderId)
    {
        if (_gcmRegId != null) {
            unregisterInGcmInBackground(senderId, App.getInstance());
        }
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *
     * @param activity activity where you check Google Play Services availability
     */
    public boolean checkPlayServicesAvailable (Activity activity)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_REQUEST_CODE)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //activity.finish();
            }
            return false;
        }
        return true;
    }

    public boolean checkPlayServicesAvailable ()
    {
        return getPlayServicesAvailabilityResultCode() == ConnectionResult.SUCCESS;
    }

    private int getPlayServicesAvailabilityResultCode ()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.isGooglePlayServicesAvailable(App.getInstance());
    }



    /********** Private Methods **********/

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInGcmInBackground (final String senderId, final Context context)
    {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    InstanceID instanceID = InstanceID.getInstance(context);
                    return instanceID.getToken(params[0], GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                } catch (IOException e) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    Log.w(TAG, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute (final String gcmRegId)
            {
                if (TextUtils.isEmpty(gcmRegId)) {
                    Log.w(TAG, "Device wasn't registered in GCM");
                } else {
                    Log.i(TAG, "Device registered in GCM, regId=" + gcmRegId);

                    QBSubscription qbSubscription = new QBSubscription();
                    qbSubscription.setNotificationChannel(QBNotificationChannel.GCM);
                    String androidId = Utils.generateDeviceId(context);
                    qbSubscription.setDeviceUdid(androidId);
                    qbSubscription.setRegistrationID(gcmRegId);
                    qbSubscription.setEnvironment(QBEnvironment.DEVELOPMENT); // Don't forget to change QBEnvironment to PRODUCTION when releasing application

                    QBPushNotifications.createSubscription(qbSubscription, new QBEntityCallback<ArrayList<QBSubscription>>() {
                                @Override
                                public void onSuccess(ArrayList<QBSubscription> qbSubscriptions, Bundle bundle) {
                                    Log.i(TAG, "Successfully subscribed for QB push messages");
                                    _gcmRegId = gcmRegId;
                                }

                                @Override
                                public void onError(QBResponseException error) {
                                    Log.e(TAG, "Unable to subscribe for QB push messages; " + error.toString());
                                }
                            });
                }
            }
        }.execute(senderId);
    }

    private void unregisterInGcmInBackground (String senderId, final Context context)
    {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    InstanceID instanceID = InstanceID.getInstance(context);
                    instanceID.deleteToken(params[0], GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                    return null;
                } catch (IOException e) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    Log.w(TAG, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute (Void gcmRegId) {
                _gcmRegId = null;
            }
        }.execute(senderId);
    }
}