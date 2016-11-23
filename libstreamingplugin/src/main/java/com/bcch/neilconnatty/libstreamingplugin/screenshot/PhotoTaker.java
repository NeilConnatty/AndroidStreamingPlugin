package com.bcch.neilconnatty.libstreamingplugin.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;

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

    PhotoTaker (File file, Camera camera, PhotoCallback callback)
    {
        _file = file;
        _callback = callback;
        _camera = camera;
        _cameraOpened = (_camera != null);
    }

    @Override
    public void run ()
    {
        if (_cameraOpened) {
            Camera.Parameters params = _camera.getParameters();
            params.setJpegQuality(100);
            _camera.setParameters(params);
            _camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    FileOutputStream ioStream = null;
                    try {
                        ioStream = new FileOutputStream(_file);
                        ioStream.write(data);
                        ioStream.close();
                        _callback.onPhotoTaken(_file);

                    } catch (FileNotFoundException e) {
                        _callback.onIllegalFilePath(e, _file);
                    } catch (IOException e) {
                        _callback.onIOError(e, _file);
                    } finally {
                        bmp.recycle();
                        releaseCamera();
                    }
                }
            });
        } else {
            _callback.onCameraNotOpened(_file);
        }
    }

    /************ Private Methods ***********/

    private void releaseCamera ()
    {
        if (_camera != null) {
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }

    /********* Nested Interfaces **********/

    public interface PhotoCallback
    {
        void onCameraNotOpened (File file);
        void onIllegalFilePath (FileNotFoundException e, File file);
        void onBitmapNotCompressed (File file);
        void onIOError(IOException e, File file);
        void onPhotoTaken (File file);
    }
}
