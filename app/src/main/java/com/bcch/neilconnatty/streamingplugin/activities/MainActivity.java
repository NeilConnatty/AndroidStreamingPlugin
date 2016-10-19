package com.bcch.neilconnatty.streamingplugin.activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
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
import com.bcch.neilconnatty.libstreamingplugin.utils.Consts;
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapResourceWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapStreamWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ImageDetailFragment;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ZoomAnimator;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.GooglePlayServicesHelper;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.constants.GcmConsts;
import com.crashlytics.android.Crashlytics;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
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

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private ImageView mImageView;
    private int _currentPosition;

    private ZoomAnimator _zoomAnimator = null;

    private List<String> receivedPushes;
    private ArrayAdapter<String> adapter;
    private GooglePlayServicesHelper googlePlayServicesHelper;


    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

    public static ArrayList<QBFile> files = null;

    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + GcmConsts.ACTION_NEW_GCM_EVENT + " with data: " + message);
            retrieveMessage(message);
        }
    };

    /** Constructor */
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        receivedPushes = new ArrayList<>();
        googlePlayServicesHelper = new GooglePlayServicesHelper();

        setViewReferences();
        initMessagesUI();

        String message = getIntent().getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
        if (message != null) {
            retrieveMessage(message);
        }
        registerReceiver();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        startStreaming(plugin, new QBSessionCallback() {
            @Override
            public void onSuccess() {
                Log.d (TAG, "streaming started, starting gcm and file download");
                if (checkPlayServices()) {
                    googlePlayServicesHelper.registerForGcm(GcmConsts.GCM_SENDER_ID);
                }
                retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
                    @Override
                    public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                        Log.d(TAG, "files successfully retrieved from server");
                        files = qbFiles;
                        initImageAdapter();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e(TAG, "Error retrieving files from server: " + e.toString());
                    }
                });
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
        final BitmapStreamWorkerTask task = new BitmapStreamWorkerTask(imageView);
        downloadFile (file, new QBEntityCallback<InputStream>() {
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

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                handleHideImage();
                return true;

            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "KEYCODE_MENU");
                return true;

            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "KEYCODE_BACK");
                handleZoom();
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
     */
    private boolean checkPlayServices() {
        return googlePlayServicesHelper.checkPlayServicesAvailable(this);
    }

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

    private void retrieveFilesFromServer (final QBEntityCallback<ArrayList<QBFile>> callback)
    {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(10, 1);
        QBContent.getFiles(requestBuilder, new QBEntityCallback<ArrayList<QBFile>>() {
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

    private void initImageAdapter ()
    {
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageResIds.length);
        mImageView = (ImageView) findViewById(R.id.expanded_image);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setVisibility(View.VISIBLE);
        _currentPosition = 0;
        createPagerListener(mPager);
    }

    private void downloadFile (final QBFile file, final QBEntityCallback<InputStream> callback)
    {
        final String fileUid = file.getUid();
        QBContent.downloadFile(fileUid, new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle bundle) {
                callback.onSuccess(inputStream, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int i) {

            }
        });
    }

    private void registerReceiver ()
    {
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }

    private void retrieveMessage (String message)
    {
        receivedPushes.add(0, message);
        adapter.notifyDataSetChanged();
    }

    private void handleZoom ()
    {
        if (_zoomAnimator == null) {
            _zoomAnimator = new ZoomAnimator(this);
            final ZoomAnimator animator = _zoomAnimator;
            downloadFile(files.get(_currentPosition), new QBEntityCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream inputStream, Bundle bundle) {
                    animator.zoomImage(mPager, mImageView, inputStream);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e (TAG, "Error downloading file to hand stream: " + e.toString());
                }
            });
        } else {
            _zoomAnimator.shrinkImage(mPager, mImageView);
            _zoomAnimator = null;
        }
    }

    private void handleHideImage ()
    {
        if (mPager.getVisibility() == View.VISIBLE) mPager.setVisibility(View.INVISIBLE);
        else mPager.setVisibility(View.VISIBLE);
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

}
