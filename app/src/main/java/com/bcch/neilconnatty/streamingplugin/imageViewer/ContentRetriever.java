package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.os.Bundle;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by neilconnatty on 2016-10-19.
 */

public class ContentRetriever
{
    public static void retrieveFilesFromServer (final QBEntityCallback<ArrayList<QBFile>> callback)
    {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(10, 1);
        QBContent.getTaggedList(requestBuilder, new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                callback.onSuccess(qbFiles, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public static void downloadFile (final QBFile file, final QBEntityCallback<InputStream> callback)
    {
        final String fileUid = file.getUid();
        QBContent.downloadFile(fileUid, new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle bundle) {
                callback.onSuccess(inputStream, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int i) {

            }
        });
    }
}
