package com.bcch.neilconnatty.streamingplugin.screenshot;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;

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

public class TakePhotoTask implements Runnable, SurfaceHolder.Callback, TextureView.SurfaceTextureListener
{
    private final String TAG = TakePhotoTask.class.getSimpleName();

    private SurfaceView _view;
    private SurfaceHolder _holder;
    private TextureView _texture;
    private Camera _camera;
    private CameraCallback _callback;
    private Context _context;

    public TakePhotoTask (Context context)
    {
        Log.d(TAG, "contructor called");
        _context = context;
    }

    @Override
    public void run ()
    {
        Log.d(TAG, "onHandleIntent called");
        try {
            final File photoFile = createImageFile(_context);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(TAG, "photoFile != null");
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

    /********** Private Methods **********/

    private void releaseCamera ()
    {
        if (_camera != null) {
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }

    private void setUpCamera (SurfaceHolder holder)
    {
        if (_camera != null) {
            try {
                _camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, "error setting preview display: " + e.toString());
            }
            _camera.startPreview();
            _callback.onCameraSetUpComplete(_camera);
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

    private File createImageFile (Context context) throws IOException
    {
        Log.d(TAG, "create image file called");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
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

        /*
        _texture = new TextureView(context);
        _texture.setSurfaceTextureListener(this);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, PixelFormat.TRANSPARENT);
        params.alpha = 0;
        wm.addView(_texture, params);
        if (_texture.isAvailable()) {
            onSurfaceTextureAvailable(_texture.getSurfaceTexture(), _texture.getWidth(), _texture.getHeight());
        }
        */
        _view = new SurfaceView(context);
        _holder = _view.getHolder();
        _holder.addCallback(this);
        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0, PixelFormat.UNKNOWN);
        wm.addView(_view, params);
    }


    /********** SurfaceHolder.Callback Methods **********/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        setUpCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged called");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed called");
        if (_camera != null) {
            _camera.stopPreview();
        }
    }

    /********** SurfaceTextureListener Callbacks **********/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable called");
        setUpCamera(surface);
    }

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


    /************* Nested Classes **************/

    public interface CameraCallback
    {
        void onCameraSetUpComplete (Camera camera);
    }

    /** PhotoCallback */
    private class PhotoTakerCallback implements PhotoTaker.PhotoCallback
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