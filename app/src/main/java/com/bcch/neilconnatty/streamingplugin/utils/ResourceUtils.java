package com.bcch.neilconnatty.streamingplugin.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.bcch.neilconnatty.streamingplugin.App;

/**
 * Created by neilconnatty on 2016-10-18.
 * Extended from Quickblox sample projects at https://github.com/QuickBlox/quickblox-android-sdk/
 */

public class ResourceUtils
{
    public static String getString(@StringRes int stringId) {
        return App.getInstance().getString(stringId);
    }

    public static Drawable getDrawable(@DrawableRes int drawableId) {
        return App.getInstance().getResources().getDrawable(drawableId);
    }

    public static int getColor(@ColorRes int colorId) {
        return App.getInstance().getResources().getColor(colorId);
    }

    public static int getDimen(@DimenRes int dimenId) {
        return (int) App.getInstance().getResources().getDimension(dimenId);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
