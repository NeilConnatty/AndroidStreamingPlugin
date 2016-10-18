package com.bcch.neilconnatty.streamingplugin.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ImageDetailFragment;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ZoomAnimator;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.GooglePlayServicesHelper;
import com.bcch.neilconnatty.streamingplugin.pushNotifications.constants.GcmConsts;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by neilconnatty on 2016-10-6.
 */

public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity _activity;

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private ImageView mImageView;
    private int _currentPosition;
    private String gcmToken = null;

    private ZoomAnimator _zoomAnimator = null;

    private List<String> receivedPushes;
    private ArrayAdapter<String> adapter;
    private GooglePlayServicesHelper googlePlayServicesHelper;


    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

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

        _activity = this;

        receivedPushes = new ArrayList<>();
        googlePlayServicesHelper = new GooglePlayServicesHelper();

        setViewReferences();
        initImageAdapter();
        initMessagesUI();

        /*
        if (checkPlayServices()) {
            while (gcmToken == null) {
                gcmToken = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "GCMToken:" + gcmToken);
            }
        }
        */

        String message = getIntent().getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
        if (message != null) {
            retrieveMessage(message);
        }
        registerReceiver();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        startStreaming (plugin);
    }


    /*********** Public Methods **********/

    public void loadBitmap (int resId, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        BitmapWorkerTask task = new BitmapWorkerTask(this, imageView);
        task.execute(resId);
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
                handleZoom ();
                return true;
        }
        return false;
    }


    /*********** Private/Protected Methods **********/

    private void startStreaming (final StreamingPlugin plugin)
    {
        plugin.initApp();
        plugin.StartStreamingPlugin(new QBSessionCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "successfully created session, starting subscription to push notifications");
                if (googlePlayServicesHelper.checkPlayServicesAvailable(_activity)) {
                    googlePlayServicesHelper.registerForGcm(GcmConsts.GCM_SENDER_ID);
                }
                /*
                String registrationID = FirebaseInstanceId.getInstance().getId();
                PushNotificationSubscriber.subscribe(registrationID, _activity, new QBSessionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "successfully subscribed to notifications");
                    }

                    @Override
                    public void onError(QBResponseException error) {
                        Log.e(TAG, "error subscribing to notifications");
                    }
                });
                */
            }

            @Override
            public void onError(QBResponseException error) {
                Log.e(TAG, "error creating new session, attempting again");
                startStreaming(plugin);
            }
        });
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 1).show();
            } else {
                Log.e(TAG, "This device is not supported.");
                //finish();
            }
            Log.e(TAG, "checkPlayServices() returned false");
            return false;
        }
        Log.d(TAG, "checkPlayService() returned true");
        return true;
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
            _zoomAnimator.zoomImage(mPager, mImageView, imageResIds[_currentPosition]);
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
