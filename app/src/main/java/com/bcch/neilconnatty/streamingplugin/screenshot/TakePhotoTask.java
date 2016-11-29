package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by neilconnatty on 2016-11-09.
 */

public abstract class TakePhotoTask implements Runnable, TextureView.SurfaceTextureListener
{
    private final String TAG = TakePhotoTask.class.getSimpleName();

    protected Context _context;

    private PhotoTaskCallback _callback = null;


    TakePhotoTask (Context context)
    {
        Log.d(TAG, "contructor called");
        _context = context;
    }

    public void registerCallback (PhotoTaskCallback callback)
    {
        _callback = callback;
    }

    @Override
    public abstract void run ();

    /********** Private Methods **********/


    protected File createImageFile (Context context) throws IOException
    {
        Log.d(TAG, "create image file called");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private void returnSuccess ()
    {
        if (_callback == null) return;
        _callback.onSuccess();
    }

    private void returnError ()
    {
        if (_callback == null) return;
        _callback.onError();
    }

    /********** SurfaceTextureListener Callbacks **********/

    @Override
    public abstract void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height);

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /******* PhotoTaskCallback ********/
    public interface PhotoTaskCallback
    {
        void onSuccess ();
        void onError ();
    }

    /** PhotoCallback */
    protected class PhotoTakerCallback implements PhotoTaker.PhotoCallback
    {
        private final String TAG = PhotoTakerCallback.class.getSimpleName();

        @Override
        public void onCameraNotOpened(File file) {
            Log.e(this.TAG, "camera not opened");
            file.delete();
            returnError();
        }

        @Override
        public void onIllegalFilePath(FileNotFoundException e, File file) {
            Log.e(this.TAG, "file not found: " + e.toString());
            file.delete();
            returnError();
        }

        @Override
        public void onIOError (IOException e, File file) {
            Log.e(this.TAG, "Error in IOStream: " + e.toString());
            file.delete();
            returnError();
        }

        @Override
        public void onBitmapNotCompressed (File file) {
            Log.e(TAG, "error compressing bitmap");
            file.delete();
            returnError();
        }

        @Override
        public void onPhotoTaken(final File file) {
            PhotoUploader.updateFile(file, new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    Log.d(TAG, "File upload success");
                    file.delete();
                    returnSuccess();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "File upload error: " + e.toString());
                    file.delete();
                    returnError();
                }
            });
        }
    }
}