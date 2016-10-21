package com.bcch.neilconnatty.streamingplugin.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.libstreamingplugin.callbacks.QBSessionCallback;
import com.bcch.neilconnatty.streamingplugin.App;
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapResourceWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapStreamWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ContentRetriever;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ImageDetailFragment;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ZoomAnimator;
import com.crashlytics.android.Crashlytics;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by neilconnatty on 2016-10-6.
 */

public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Bitmap _currentBitmap = null;

    private ImagePagerAdapter mAdapter = null;
    private ViewPager mPager;
    private ImageView mImageView;
    private int _currentPosition = 0;
    private boolean _imageViewOn = false;

    private ZoomAnimator _zoomAnimator = null;

    private List<String> receivedPushes;
    private ArrayAdapter<String> adapter;
    //private GooglePlayServicesHelper googlePlayServicesHelper;


    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

    public static ArrayList<QBFile> files = null;

    /* Was used for push notifications--doesn't work on ODG R-7
    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + GcmConsts.ACTION_NEW_GCM_EVENT + " with data: " + message);
            retrieveMessage(message);
        }
    };
    */

    /** Constructor */
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        receivedPushes = new ArrayList<>();
        //googlePlayServicesHelper = new GooglePlayServicesHelper();

        setViewReferences();
        initMessagesUI();

        /* Was used for push notifications--doesn't work on ODG R-7
        String message = getIntent().getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
        if (message != null) {
            retrieveMessage(message);
        }
        registerReceiver();
        */

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        startStreaming(plugin, new QBSessionCallback() {
            @Override
            public void onSuccess() {
                Log.d (TAG, "streaming started");
                /* Was used for push notifications--doesn't work on ODG R-7
                if (checkPlayServices()) {
                    googlePlayServicesHelper.registerForGcm(GcmConsts.GCM_SENDER_ID);
                }
                */
            }

            @Override
            public void onError(QBResponseException error) {
            }
        });
    }


    /*********** Public Methods **********/

    public void loadBitmap (int resId, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        BitmapWorkerTask task = new BitmapResourceWorkerTask(this, imageView);
        task.execute(resId);
    }

    public void loadBitmap (QBFile file, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        final BitmapStreamWorkerTask task = new BitmapStreamWorkerTask(this, imageView);
        ContentRetriever.downloadFile (file, new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle bundle) {
                task.execute(inputStream);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "Error downloading file: " + e.toString());
            }
        });
    }

    public void setCurrentBitmap (Bitmap bitmap)
    {
        _currentBitmap = bitmap;
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (!_imageViewOn) return true;
                handleZoom();
                return true;

            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "KEYCODE_MENU");
                handleReloadImages();
                return true;

            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "KEYCODE_BACK");
                if (mAdapter == null) {
                    initImageAdapter();
                    _imageViewOn = true;
                } else {
                    handleHideImage();
                }
                return true;
        }
        return false;
    }


    /*********** Private/Protected Methods **********/

    private void startStreaming (final StreamingPlugin plugin, final QBSessionCallback callback)
    {
        plugin.initApp();
        plugin.StartStreamingPlugin(new QBSessionCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "successfully created session");
                callback.onSuccess();
            }

            @Override
            public void onError(QBResponseException error) {
                Log.e(TAG, "error creating new session, attempting again " + error.toString());
                startStreaming(plugin, callback);
            }
        });
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *//* Was used for push notifications--doesn't work on ODG R-7
    private boolean checkPlayServices() {
        return googlePlayServicesHelper.checkPlayServicesAvailable(this);
    }
    */

    private void initMessagesUI ()
    {
        ListView incomingMessagesListView = (ListView) findViewById(R.id.list_messages);
        adapter = new ArrayAdapter<>(this, R.layout.list_item_message, R.id.item_message, receivedPushes);
        incomingMessagesListView.setAdapter(adapter);
        incomingMessagesListView.setEmptyView(findViewById(R.id.text_empty_messages));
    }

    @Override
    protected void setViewReferences ()
    {
        opponentView = null;
        localView = (QBRTCSurfaceView) findViewById(R.id.localView);
    }

    private void initImageAdapter ()
    {
        if (files == null) {
            retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
                @Override
                public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                    files = qbFiles;
                    initImageAdapterHelper();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "Error retrieving files from server: " + e.toString());
                }
            });
        } else {
            initImageAdapterHelper();
        }
    }

    private void retrieveFilesFromServer (final QBEntityCallback<ArrayList<QBFile>> callback)
    {
        ContentRetriever.retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                callback.onSuccess(qbFiles, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    private void initImageAdapterHelper ()
    {
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), files.size());
        mImageView = (ImageView) findViewById(R.id.expanded_image);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setVisibility(View.VISIBLE);
        createPagerListener(mPager);
    }

    /* Was used for push notifications--doesn't work on ODG R-7
    private void registerReceiver ()
    {
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }
    */

    private void retrieveMessage (String message)
    {
        receivedPushes.add(0, message);
        adapter.notifyDataSetChanged();
    }

    private void handleZoom ()
    {
        if (_zoomAnimator == null) {
            _zoomAnimator = new ZoomAnimator(this);
            // if there is a current bitmap saved, zoom image with it. Else download new bitmap
            if (_currentBitmap != null) {
                _zoomAnimator.zoomImage(mPager, mImageView, _currentBitmap);
            } else {
                final ZoomAnimator animator = _zoomAnimator;
                ContentRetriever.downloadFile(files.get(_currentPosition), new QBEntityCallback<InputStream>() {
                    @Override
                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                        animator.zoomImage(mPager, mImageView, inputStream);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "Error downloading file to hand stream: " + e.toString());
                    }
                });
            }
        } else {
            _zoomAnimator.shrinkImage(mPager, mImageView);
            _zoomAnimator = null;
        }
    }

    private void handleHideImage ()
    {
        if (mPager.getVisibility() == View.VISIBLE) {
            mPager.setVisibility(View.INVISIBLE);
            _imageViewOn = false;
        }
        else {
            mPager.setVisibility(View.VISIBLE);
            _imageViewOn = true;
        }
    }

    private void handleReloadImages ()
    {
        retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                files.clear();
                files = qbFiles;
                mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), files.size());
                mPager.setAdapter(mAdapter);
                mPager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e (TAG, "error retrieving files from server: " + e.toString());
            }
        });
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

        pager.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
            @Override
            public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
                // TODO
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

}
