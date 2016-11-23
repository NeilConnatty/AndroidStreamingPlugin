package com.bcch.neilconnatty.libstreamingplugin.screenshot;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;

import java.io.File;

/**
 * Created by neilconnatty on 2016-11-07.
 */

public class PhotoUploader
{
    private static final String TAG = PhotoUploader.class.getSimpleName();

    public static void uploadNewFile (final File file, QBEntityCallback<QBFile> callback)
    {
        QBContent.uploadFileTask(file, true, "bcch", callback);
    }

    public static void updateFile (final File file, QBEntityCallback<QBFile> callback)
    {
        QBContent.updateFileTask(file, 6316512, "bcch", callback);
    }
}
