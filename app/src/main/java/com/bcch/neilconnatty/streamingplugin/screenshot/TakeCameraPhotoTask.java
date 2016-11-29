package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by neilconnatty on 2016-11-28.
 */

public class TakeCameraPhotoTask extends TakePhotoTask
{
    private final String TAG = TakeCameraPhotoTask.class.getSimpleName();
    private Camera _camera;
    private CameraCallback _callback;

    public TakeCameraPhotoTask (Context context)
    {
        super(context);
    }

    @Override
    public void run()
    {
        Log.d(TAG, "onHandleIntent called");
        try {
            final File photoFile = createImageFile(_context);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(TAG, "photoFile != null");
                Log.d(TAG, "No stream rendering, taking photo");
                setUpPhotoService(_context, new CameraCallback() {
                    @Override
                    public void onCameraSetUpComplete(Camera camera) {
                        Log.d(TAG, "on camera set up complete called");
                        PhotoTaker photoTaker = new PhotoTaker(photoFile, camera, new PhotoTakerCallback());
                        photoTaker.run();
                    }
                });
            } else {
                Log.d(TAG, "photo file == null");
            }
        } catch (IOException e) {
            Log.e(TAG, "error creating file: " + e.toString());
        }
    }

    /********* Private Methods **********/

    private void releaseCamera ()
    {
        if (_camera != null) {
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }

    private void setUpCamera (SurfaceTexture surface)
    {
        if (_camera != null) {
            try {
                _camera.setPreviewTexture(surface);
            } catch (IOException e) {
                Log.e(TAG, "error setting preview display: " + e.toString());
            }
            _camera.startPreview();
            _callback.onCameraSetUpComplete(_camera);
        }
    }

    private void setUpPhotoService (Context context, CameraCallback callback) {
        try {
            releaseCamera();
            _camera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "failed to open camera: " + e.toString());
            e.printStackTrace();
        }

        _callback = callback;

        TextureView textureView = new TextureView(context);
        textureView.setSurfaceTextureListener(this);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSPARENT);
        params.alpha = 0;
        wm.addView(textureView, params);
        if (textureView.isAvailable()) {
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
    }


    /********** SurfaceTextureListener Callbacks **********/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable called");
        setUpCamera(surface);
    }


    /********** Nested Classes ***********/

    private interface CameraCallback
    {
        void onCameraSetUpComplete (Camera camera);
    }
}
