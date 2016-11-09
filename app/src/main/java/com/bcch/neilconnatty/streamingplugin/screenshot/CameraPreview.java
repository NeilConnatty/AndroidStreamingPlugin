package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by neilconnatty on 2016-11-08.
 */

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback
{
    private final String TAG = CameraPreview.class.getSimpleName();

    private SurfaceView _view;
    private SurfaceHolder _holder;
    private Camera _camera;
    private CameraCallback _callback;

    public CameraPreview (Context context, CameraCallback callback)
    {
        super(context);

        try {
            releaseCamera();
            _camera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "failed to open camera: " + e.toString());
            e.printStackTrace();
        }

        _callback = callback;

        _view = new SurfaceView(context);
        _view.setVisibility(INVISIBLE);
        addView(_view);

        _holder = _view.getHolder();
        _holder.addCallback(this);
        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (_camera != null) {
            _camera.stopPreview();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private void releaseCamera ()
    {
        if (_camera != null) {
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }


    /***** Nested Classes ******/

    public interface CameraCallback
    {
        void onCameraSetUpComplete (Camera camera);
    }
}
