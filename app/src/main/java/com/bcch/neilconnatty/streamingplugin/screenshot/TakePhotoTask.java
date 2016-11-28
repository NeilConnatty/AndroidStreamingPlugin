package com.bcch.neilconnatty.streamingplugin.screenshot;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.webrtc.SurfaceViewRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by neilconnatty on 2016-11-09.
 */

public abstract class TakePhotoTask implements Runnable, TextureView.SurfaceTextureListener
{
    private final String TAG = TakePhotoTask.class.getSimpleName();

    protected TextureView _texture;
    protected Context _context;


    TakePhotoTask (Context context)
    {
        Log.d(TAG, "contructor called");
        _context = context;
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
//        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
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



    /** PhotoCallback */
    protected class PhotoTakerCallback implements PhotoTaker.PhotoCallback
    {
        private final String TAG = PhotoTakerCallback.class.getSimpleName();

        @Override
        public void onCameraNotOpened(File file) {
            Log.e(this.TAG, "camera not opened");
            file.delete();
        }

        @Override
        public void onIllegalFilePath(FileNotFoundException e, File file) {
            Log.e(this.TAG, "file not found: " + e.toString());
            file.delete();
        }

        @Override
        public void onIOError (IOException e, File file) {
            Log.e(this.TAG, "Error in IOStream: " + e.toString());
            file.delete();
        }

        @Override
        public void onBitmapNotCompressed (File file)
        {
            Log.e(TAG, "error compressing bitmap");
            file.delete();
        }

        @Override
        public void onPhotoTaken(final File file) {
            PhotoUploader.updateFile(file, new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    Log.d(TAG, "File upload success");
                    file.delete();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "File upload error: " + e.toString());
                    file.delete();
                }
            });
        }
    }
}