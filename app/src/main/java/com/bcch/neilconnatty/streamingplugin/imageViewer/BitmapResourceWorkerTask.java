package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by neilconnatty on 2016-10-19.
 */

public class BitmapResourceWorkerTask extends BitmapWorkerTask
{
    public BitmapResourceWorkerTask (Activity activity, ImageView imageView) {
        super(activity, imageView);
    }

    public BitmapResourceWorkerTask (Activity activity, ImageView imageView, int reqWidth, int reqHeight) {
        super(activity, imageView, reqWidth, reqHeight);
    }

    @Override
    protected Bitmap decodeBitmap (int data)
    {
        if (_scaleImage)
            return BitmapDecoder.decodeSampledBitmapFromResource (_activity.getResources(), data, _reqWidth, _reqHeight);
        else
            return BitmapDecoder.decodeBitmapFromResource(_activity.getResources(), data);
    }
}
