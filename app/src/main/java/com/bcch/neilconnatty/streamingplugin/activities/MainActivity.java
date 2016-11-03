package com.bcch.neilconnatty.streamingplugin.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bcch.neilconnatty.libstreamingplugin.StreamingPlugin;
import com.bcch.neilconnatty.libstreamingplugin.activites.BaseActivity;
import com.bcch.neilconnatty.libstreamingplugin.callbacks.QBSessionCallback;
import com.bcch.neilconnatty.streamingplugin.R;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapResourceWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapStreamWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.BitmapWorkerTask;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ContentRetriever;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ImageDetailFragment;
import com.bcch.neilconnatty.streamingplugin.imageViewer.ZoomAnimator;
import com.bcch.neilconnatty.streamingplugin.messaging.Messenger;
import com.bcch.neilconnatty.streamingplugin.messaging.remoteInput.RemoteInput;
import com.bcch.neilconnatty.streamingplugin.messaging.remoteInput.RemoteInputCallbackListener;
import com.bcch.neilconnatty.streamingplugin.timer.TimerCallback;
import com.bcch.neilconnatty.streamingplugin.timer.TimerHelper;
import com.bcch.neilconnatty.streamingplugin.timer.TimerUICallback;
import com.crashlytics.android.Crashlytics;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by neilconnatty on 2016-10-6.
 */

public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImagePagerAdapter mAdapter = null;
    private ViewPager mPager;
    private ImageView mImageView;
    private int _currentPosition = 0;
    private boolean _imageViewOn = false;
    private TextView _timerText;
    private Timer _timer;
    private Bitmap[] bitmaps;
    private Messenger _messenger;

    final private Handler _messageHandler = new Handler();

    private ZoomAnimator _zoomAnimator = null;

    /** A static dataset to back the ViewPager adapter */
    public final static Integer[] imageResIds = new Integer[] {
            R.raw.image_one, R.raw.image_two, R.raw.image_three
    };

    public static ArrayList<QBFile> files = null;


    /** Constructor */
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with (this, new Crashlytics());
        setContentView(R.layout.activity_main);

        setViewReferences();

        _timerText = (TextView) findViewById(R.id.timer);
        _timer = initChronometer(new Handler(), new TimerUICallback(_timerText));

        TextView notificationText = (TextView) findViewById(R.id.notification);
        _messenger = new Messenger(_messageHandler, notificationText, new RemoteInputCallbackListener() {
            @Override
            public void receiveInput(RemoteInput input) {
                switch (input) {
                    case ZOOM_IMAGE:
                        handleZoom();
                        break;
                    case SHOW_IMAGE:
                        handleShowImage();
                        break;
                    case HIDE_IMAGE:
                        handleShowImage();
                        break;
                    case RELOAD_IMAGE:
                        handleReloadImages();
                        break;
                }
            }
        });
        startMessagingService();

        StreamingPlugin plugin = new StreamingPlugin (this, false);
        startStreaming(plugin, new QBSessionCallback() {
            @Override
            public void onSuccess() {
                Log.d (TAG, "streaming started");
            }

            @Override
            public void onError(QBResponseException error) {
            }
        });
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        _timer.cancel();
        _messenger.stopMessenger();
    }


    /*********** Public Methods **********/

    public void loadBitmap (int resId, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        BitmapWorkerTask task = new BitmapResourceWorkerTask(this, imageView);
        task.execute(resId);
    }

    public void loadBitmap (QBFile file, ImageView imageView, int pos)
    {
        imageView.setImageResource(R.drawable.image_placeholder);
        final BitmapStreamWorkerTask task = new BitmapStreamWorkerTask(this, imageView, pos);
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

    public void setBitmapPosition (Bitmap bitmap, int pos)
    {
        bitmaps[pos] = bitmap;
    }

    public Bitmap getBitmapAtPosition (int pos)
    {
        return bitmaps[pos];
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "KEYCODE_DPAD_CENTER");
                handleZoom();
                return true;

            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "KEYCODE_MENU");
                handleReloadImages();
                return true;

            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "KEYCODE_BACK");
                handleShowImage();
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

    private void startMessagingService ()
    {
        if (_messenger.initializeMessenger() == 1) {
            Log.d(TAG, "Messaging service started");
        } else {
            Log.e(TAG, "error starting messaging service");
        }
    }

    private Timer initChronometer (Handler handler, TimerCallback callback)
    {
        return new TimerHelper().createTimer(handler, callback, 0, 1000);
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
                    initImageAdapterHelper(qbFiles);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "Error retrieving files from server: " + e.toString());
                }
            });
        } else {
            initImageAdapterHelper(files);
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

    private void initImageAdapterHelper (ArrayList<QBFile> qbFiles)
    {
        files = qbFiles;
        bitmaps = new Bitmap[files.size()];
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), files.size());
        mImageView = (ImageView) findViewById(R.id.expanded_image);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setVisibility(View.VISIBLE);
        createPagerListener(mPager);
    }

    private void handleZoom ()
    {
        if (!_imageViewOn) return;
        if (_zoomAnimator == null) {
            _zoomAnimator = new ZoomAnimator(this);
            // if there is a current bitmap saved, zoom image with it. Else download new bitmap
            if (bitmaps[_currentPosition] != null) {
                _zoomAnimator.zoomImage(mPager, mImageView, bitmaps[_currentPosition]);
            } else {
                final ZoomAnimator animator = _zoomAnimator;
                ContentRetriever.downloadFile(files.get(_currentPosition), new QBEntityCallback<InputStream>() {
                    @Override
                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                        animator.zoomImage(mPager, mImageView, inputStream, _currentPosition);
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


    private void handleShowImage() {
        if (mAdapter == null) {
            initImageAdapter();
            _imageViewOn = true;
        } else {
            handleHideImage();
        }
    }

    private void handleReloadImages ()
    {
        retrieveFilesFromServer(new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                files.clear();
                files = qbFiles;
                bitmaps = null;
                bitmaps = new Bitmap[files.size()];
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
