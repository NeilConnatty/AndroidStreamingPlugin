package com.bcch.neilconnatty.streamingplugin.pushNotifications.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.activities.ActivityLifecycle;
import com.bcch.neilconnatty.streamingplugin.activities.MainActivity;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.constants.GcmConsts;
import com.bcch.neilconnatty.streamingplugin.utils.NotificationUtils;
import com.bcch.neilconnatty.streamingplugin.utils.ResourceUtils;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by neilconnatty on 2016-10-17.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class GcmPushListenerService extends GcmListenerService
{
    private static final String TAG = GcmPushListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }

        sendPushMessageBroadcast(message);
    }

    private void showNotification(String message)
    {
        NotificationUtils.showNotification(this, MainActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }

    protected void sendPushMessageBroadcast(String message)
    {
        Intent gcmBroadcastIntent = new Intent(GcmConsts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(GcmConsts.EXTRA_GCM_MESSAGE, message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}
