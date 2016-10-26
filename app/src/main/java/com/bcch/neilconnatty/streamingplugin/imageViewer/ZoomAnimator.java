package com.bcch.neilconnatty.streamingplugin.imageViewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bcch.neilconnatty.streamingplugin.R;

import java.io.InputStream;

/**
 * Created by neilconnatty on 2016-10-13.
 */

public class ZoomAnimator
{
    private Animator _currentAnimator;
    private int _shortAnimationDuration;
    private Activity _currentActivity;

    private final Rect startBounds = new Rect();
    private final Rect finalBounds = new Rect();
    private final Point globalOffset = new Point();

    private float _startScaleFinal;

    /** Constructor */
    public ZoomAnimator (Activity currentActivity)
    {
        _shortAnimationDuration = currentActivity.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        _currentActivity = currentActivity;
    }


    /********** Public Methods **********/

    public void zoomImage (final View sourceView, final ImageView targetView, int imageResId)
    {
        // if there's an animation in progress, cancel it and
        // proceed with this one
        if (_currentAnimator != null) _currentAnimator.cancel();

        BitmapWorkerTask task = new BitmapResourceWorkerTask (_currentActivity, targetView);
        task.execute(imageResId);

        zoomImageHelper(sourceView, targetView);
    }

    public void zoomImage (final View sourceView, final ImageView targetView, Bitmap bitmap)
    {
        if (_currentAnimator != null) _currentAnimator.cancel();
        targetView.setImageBitmap(bitmap);
        zoomImageHelper(sourceView, targetView);
    }

    public void zoomImage (final View sourceView, final ImageView targetView, InputStream is, int pos)
    {
        if (_currentAnimator != null) _currentAnimator.cancel();

        BitmapStreamWorkerTask task = new BitmapStreamWorkerTask(_currentActivity, targetView, pos);
        task.execute(is);

        zoomImageHelper(sourceView, targetView);
    }

    public void shrinkImage (final View sourceView, final ImageView targetView)
    {
        if (_currentAnimator != null) {
            _currentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(targetView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(targetView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(targetView,
                                View.SCALE_X, _startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(targetView,
                                View.SCALE_Y, _startScaleFinal));
        set.setDuration(_shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sourceView.setVisibility(View.VISIBLE);
                targetView.setVisibility(View.GONE);
                _currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                sourceView.setVisibility(View.VISIBLE);
                targetView.setVisibility(View.GONE);
                _currentAnimator = null;
            }
        });
        set.start();
        _currentAnimator = set;
    }


    /********** Private Methods **********/

    private void zoomImageHelper(View sourceView, ImageView targetView)
    {
        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        sourceView.getGlobalVisibleRect(startBounds);
        _currentActivity.findViewById(R.id.activity_main)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        sourceView.setVisibility(View.INVISIBLE);
        targetView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        targetView.setPivotX(0f);
        targetView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(targetView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(targetView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(targetView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(targetView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(_shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                _currentAnimator = null;
            }
        });
        set.start();
        _currentAnimator = set;

        _startScaleFinal = startScale;
    }
}
