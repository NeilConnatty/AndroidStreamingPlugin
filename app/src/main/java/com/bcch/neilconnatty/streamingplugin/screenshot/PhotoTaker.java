package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by neilconnatty on 2016-11-08.
 */

public class PhotoTaker implements Runnable
{
    private final String TAG = PhotoTaker.class.getSimpleName();

    private Camera _camera;
    private Boolean _cameraOpened;
    private File _file;
    private PhotoCallback _callback;

    public PhotoTaker (File file, PhotoCallback callback)
    {
        _file = file;
        _callback = callback;
        _cameraOpened = false;

        try {
            releaseCamera();
            _camera = Camera.open();
            _cameraOpened = (_camera != null);
        } catch (Exception e) {
            Log.e(TAG, "failed to open camera: " + e.toString());
            e.printStackTrace();
        }
    }

    public boolean cameraOpened ()
    {
        return _cameraOpened;
    }

    @Override
    public void run ()
    {
        if (_cameraOpened) {
            _camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    FileOutputStream ioStream = null;
                    try {
                        ioStream = new FileOutputStream(_file);
                        if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, ioStream)) {
                            _callback.onPhotoTaken(_file);
                        } else {
                            _callback.onBitmapNotCompressedToFile(_file);
                        }
                    } catch (FileNotFoundException e) {
                        _callback.onIllegalFilePath(e);
                    }
                    try {
                        if (ioStream != null) ioStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "error closing ioStream: " + e.toString());
                    }
                    bmp.recycle();
                    releaseCamera();
                }
            });
        } else {
            _callback.onCameraNotOpened();
        }
    }

    /************ Private Methods ***********/

    private void releaseCamera ()
    {
        if (_camera != null) {
            _camera.release();
            _camera = null;
        }
    }

    /********* Nested Interfaces **********/

    public interface PhotoCallback
    {
        void onCameraNotOpened ();
        void onIllegalFilePath (FileNotFoundException e);
        void onBitmapNotCompressedToFile (File file);
        void onPhotoTaken (File file);
    }
}
