package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bcch.neilconnatty.streamingplugin.activities.MainActivity;

import java.lang.ref.WeakReference;

/** BitmapWorkertask */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
{
    private Activity _activity;
    private final WeakReference<ImageView> imageViewReference;
    private int data = 0;
    private int _reqWidth;
    private int _reqHeight;
    private boolean _scaleImage;

    public BitmapWorkerTask (Activity activity, ImageView imageView) {
        _activity = activity;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        _scaleImage = false;
    }

    public BitmapWorkerTask (Activity activity, ImageView imageView, int reqWidth, int reqHeight)
    {
        _activity = activity;
        imageViewReference = new WeakReference<ImageView>(imageView);
        _reqHeight = reqHeight; _reqWidth = reqWidth;
        _scaleImage = true;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground (Integer... params) {
        data = params[0];
        if (_scaleImage)
            return BitmapDecoder.decodeSampledBitmapFromResource (_activity.getResources(), data, _reqWidth, _reqHeight);
        else
            return BitmapDecoder.decodeBitmapFromResource(_activity.getResources(), data);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute (Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
