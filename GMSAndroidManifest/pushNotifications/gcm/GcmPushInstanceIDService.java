package com.bcch.neilconnatty.streamingplugin.pushNotifications.gcm;

import com.bcch.neilconnatty.streamingplugin.pushNotifications.GooglePlayServicesHelper;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.constants.GcmConsts;
import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by neilconnatty on 2016-10-17.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class GcmPushInstanceIDService extends InstanceIDListenerService
{
    @Override
    public void onTokenRefresh() {
        GooglePlayServicesHelper playServicesHelper = new GooglePlayServicesHelper();
        if (playServicesHelper.checkPlayServicesAvailable()) {
            playServicesHelper.registerForGcm(getSenderId());
        }
    }

    private String getSenderId ()
    {
        return GcmConsts.GCM_SENDER_ID;
    }

}
