package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import android.widget.ImageView;

import com.bcch.neilconnatty.streamingplugin.activities.MainActivity;

import java.lang.ref.WeakReference;

/** BitmapWorkertask */
public abstract class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
{
    private final WeakReference<ImageView> imageViewReference;
    private int _data = 0;

    protected Activity _activity;
    protected int _reqWidth;
    protected int _reqHeight;
    protected boolean _scaleImage;

    public BitmapWorkerTask (Activity activity, ImageView imageView) {
        _activity = activity;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);
        _scaleImage = false;
    }

    public BitmapWorkerTask (Activity activity, ImageView imageView, int reqWidth, int reqHeight)
    {
        _activity = activity;
        imageViewReference = new WeakReference<>(imageView);
        _reqHeight = reqHeight; _reqWidth = reqWidth;
        _scaleImage = true;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground (Integer... params)
    {
        _data = params[0];
        return decodeBitmap (_data);
    }

    protected abstract Bitmap decodeBitmap(int data);

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute (Bitmap bitmap)
    {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
