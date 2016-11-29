package com.bcch.neilconnatty.streamingplugin.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.opengl.GLException;
import android.util.Log;
import android.view.TextureView;

import org.webrtc.SurfaceViewRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by neilconnatty on 2016-11-28.
 *
 * Algorithm found in method createBitmapFromGLSurface(...) by StackOverflow user Dalinaum
 * Source: http://stackoverflow.com/questions/5514149/capture-screen-of-glsurfaceview-to-bitmap
 *
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
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {}


    /*********** Private Methods **********/

    private void captureBitmap (final ScreenshotCallback callback)
    {
        _width = _viewRenderer.getWidth();
        _height = _viewRenderer.getHeight();

        /*
        _viewRenderer.post(new Runnable() {
            @Override
            public void run() {
                _viewRenderer.layout(0, 0, _width, _height);

                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                _image = Bitmap.createBitmap(_width, _height, conf);

                Canvas canvas = new Canvas(_image);
                _viewRenderer.draw(canvas);
                callback.onScreenshotSuccess(_image);
            }
        });

        _viewRenderer.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap;
                if (_viewRenderer.isDrawingCacheEnabled()) {
                    bitmap = Bitmap.createBitmap(_viewRenderer.getDrawingCache(true));
                } else {
                    _viewRenderer.setDrawingCacheEnabled(true);
                    bitmap = Bitmap.createBitmap(_viewRenderer.getDrawingCache(true));
                    _viewRenderer.setDrawingCacheEnabled(false);
                }

                callback.onScreenshotSuccess(bitmap);
            }
        });
        */

        _viewRenderer.post(new Runnable() {
            @Override
            public void run() {
                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                Bitmap bitmap = createBitmapFromGLSurface(0, 0, _width, _height, gl);

                callback.onScreenshotSuccess(bitmap);
            }
        });

        /*
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
        */
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

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e(TAG, "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    /************* Nested Classes **************/

    private interface ScreenshotCallback
    {
        void onScreenshotSuccess (Bitmap bitmap);
        void onScreenshotFailed (Exception e);
    }

}
