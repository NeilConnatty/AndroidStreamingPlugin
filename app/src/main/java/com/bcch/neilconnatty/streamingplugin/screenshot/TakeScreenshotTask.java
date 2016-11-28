package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.webrtc.SurfaceViewRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by neilconnatty on 2016-11-28.
 */

public class TakeScreenshotTask extends TakePhotoTask
{
    private final String TAG = TakeScreenshotTask.class.getSimpleName();

    private SurfaceViewRenderer _viewRenderer;
    private ScreenshotCallback _callback;
    private Bitmap _image;

    private int _width;
    private int _height;

    public TakeScreenshotTask (Context context, SurfaceViewRenderer renderer)
    {
        super(context);
        _viewRenderer = renderer;
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
                Log.d(TAG, "Stream rendering, so taking screenshot");
                takeScreenshot(photoFile, new PhotoTakerCallback());
            } else {
                Log.d(TAG, "photo file == null");
            }
        } catch (IOException e) {
            Log.e(TAG, "error creating file: " + e.toString());
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        onTextureAvailable(_texture, _callback, _image, _width, _height);
    }


    /*********** Private Methods **********/

    private void onTextureAvailable (TextureView tView, ScreenshotCallback callback, Bitmap image, int width, int height)
    {
        try {
            Canvas canvas = tView.lockCanvas();
            canvas.setBitmap(image);
            _viewRenderer.draw(canvas);

            Bitmap screen = Bitmap.createBitmap(image, 0, 0, width, height);
            tView.unlockCanvasAndPost(canvas);

            callback.onScreenshotSuccess(screen);
        } catch (Exception e) {
            callback.onScreenshotFailed(e);
        }
    }

    private void captureBitmap (final ScreenshotCallback callback)
    {
        _width = _viewRenderer.getWidth();
        _height = _viewRenderer.getHeight();

        Bitmap.Config conf = Bitmap.Config.RGB_565;
        _image = Bitmap.createBitmap(_width, _height, conf);

        _texture = new TextureView(_context);
        _texture.setSurfaceTextureListener(this);

        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSPARENT);
        params.alpha = 0;
        wm.addView(_texture, params);

        if (_texture.isAvailable()) {
            onTextureAvailable(_texture, callback, _image, _width, _height);
        }
    }

    private void takeScreenshot (final File photoFile, final PhotoTaker.PhotoCallback callback)
    {
        _callback = new ScreenshotCallback() {
            @Override
            public void onScreenshotSuccess(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                try
                {
                    FileOutputStream ioStream = new FileOutputStream(photoFile);
                    ioStream.write(byteArray);
                    stream.close();
                    ioStream.close();
                    callback.onPhotoTaken(photoFile);
                } catch (IOException e)
                {
                    Log.e(TAG, e.toString());
                    callback.onIOError(e, photoFile);
                }
            }

            @Override
            public void onScreenshotFailed(Exception e) {
                Log.e(TAG, e.toString());
                callback.onBitmapNotCompressed(photoFile);
            }
        };
        captureBitmap(_callback);
    }

    /************* Nested Classes **************/

    private interface ScreenshotCallback
    {
        void onScreenshotSuccess (Bitmap bitmap);
        void onScreenshotFailed (Exception e);
    }

}
