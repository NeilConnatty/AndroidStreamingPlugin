package com.bcch.neilconnatty.streamingplugin.activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapDecoder;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ImageDetailFragment;
import com.crashlytics.android.Crashlytics;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import java.lang.ref.WeakReference;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;


    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

    /** Constructor */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        setViewReferences();

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageResIds.length);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        plugin.StartStreamingPlugin ();
    }


    /*********** Public Methods **********/

    public void loadBitmap(int resId, ImageView imageView) {
        imageView.setImageResource(R.drawable.image_placeholder);
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(resId);
    }


    /*********** Private/Protected Methods **********/

    @Override
    protected void setViewReferences()
    {
        opponentView = null;
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }


    /*********** Local Classes **********/

    /** ImagePagerAdapter */
    public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(position);
        }
    }

    /** BitmapWorkertask */
    public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            return BitmapDecoder.decodeSampledBitmapFromResource (getResources(), data, 100, 100);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
