package com.bcch.neilconnatty.streamingplugin.activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
    private int _currentPosition;


    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

    /** Constructor */
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        setViewReferences();
        initImageAdapter();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        plugin.StartStreamingPlugin ();
    }


    /*********** Public Methods **********/

    public void loadBitmap (int resId, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(resId);
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (mPager.getVisibility() == View.VISIBLE) mPager.setVisibility(View.INVISIBLE);
                else mPager.setVisibility(View.VISIBLE);
                return true;

            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "KEYCODE_MENU");
                return true;

            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "KEYCODE_BACK");
                return true;
        }

        return false;
    }


    /*********** Private/Protected Methods **********/

    @Override
    protected void setViewReferences ()
    {
        opponentView = null;
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }

    private void initImageAdapter ()
    {
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageResIds.length);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setVisibility(View.VISIBLE);
        _currentPosition = 0;
        createPagerListener(mPager);
    }

    private void createPagerListener (ViewPager pager)
    {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener () {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                _currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /*********** Local Classes **********/

    /** ImagePagerAdapter */
    public static class ImagePagerAdapter extends FragmentStatePagerAdapter
    {
        private final int mSize;

        ImagePagerAdapter (FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount () {
            return mSize;
        }

        @Override
        public Fragment getItem (int position) {
            return ImageDetailFragment.newInstance(position);
        }
    }

    /** BitmapWorkertask */
    public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        BitmapWorkerTask (ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground (Integer... params) {
            data = params[0];
            return BitmapDecoder.decodeSampledBitmapFromResource (getResources(), data, 100, 100);
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
}
