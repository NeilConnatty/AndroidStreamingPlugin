package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;

/**
 * Created by neilconnatty on 2016-11-07.
 */

public class PhotoUploader
{
    private static final String TAG = PhotoUploader.class.getSimpleName();

    public static void uploadNewFile (File file)
    {
        QBContent.uploadFileTask(file, true, "bcch", new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                Log.d(TAG, "file upload success");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "file upload error: " + e.toString());
            }
        });
    }

    public static void updateFile (File file)
    {
        QBContent.updateFileTask(file, 6304083, "bcch", new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                Log.d(TAG, "file update success");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "File update error: " + e.toString());
            }
        });
    }

}
