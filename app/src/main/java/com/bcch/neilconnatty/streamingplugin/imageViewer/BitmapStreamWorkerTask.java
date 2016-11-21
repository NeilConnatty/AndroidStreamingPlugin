package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bcch.neilconnatty.streamingplugin.activities.MainActivity;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by neilconnatty on 2016-10-19.
 */

public class BitmapStreamWorkerTask extends AsyncTask<InputStream, Void, Bitmap>
{
    private final WeakReference<ImageView> imageViewReference;

    private int _reqWidth;
    private int _reqHeight;
    private boolean _scaleImage;
    private Context _currentContext;
    private  int _position;

    public BitmapStreamWorkerTask (Context context, ImageView imageView, int pos)
    {
        imageViewReference = new WeakReference<>(imageView);
        _scaleImage = false;
        _currentContext = context;
        _position = pos;
    }

    public BitmapStreamWorkerTask (ImageView imageView, int reqWidth, int reqHeight, int pos)
    {
        imageViewReference = new WeakReference<>(imageView);
        _scaleImage = true;
        _reqHeight = reqHeight;
        _reqWidth = reqWidth;
        _position = pos;
    }


    @Override
    protected Bitmap doInBackground (InputStream... params)
    {
        InputStream is = params[0];
        return decodeBitmap(is);
    }

    private Bitmap decodeBitmap (InputStream is)
    {
        if (_scaleImage)
            return BitmapDecoder.decodeSampledBitmapFromStream (is, _reqWidth, _reqHeight);
        else
            return BitmapDecoder.decodeBitmapFromStream(is);
    }

    @Override
    protected void onPostExecute (Bitmap bitmap)
    {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            if (MainActivity.class.isInstance(_currentContext)) {
                ((MainActivity) _currentContext).setBitmapPosition(bitmap, _position);
            }
        }
    }
}
