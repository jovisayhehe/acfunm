/*
 * Copyright (C) 2013 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.acfun.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewConfiguration;

/**
 * Class which describes system bar sizing and other characteristics for the
 * current device configuration.
 * 
 */
public class SystemBarConfig {
    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
    private final boolean mTranslucentStatusBar;
    private final boolean mTranslucentNavBar;
    private final int mStatusBarHeight;
    private final int mActionBarHeight;
    private final boolean mHasNavigationBar;
    private final int mNavigationBarHeight;
    private final int mNavigationBarWidth;
    private final boolean mInPortrait;
    private final float mSmallestWidthDp;

    public SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean traslucentNavBar) {
        Resources res = activity.getResources();
        mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        mSmallestWidthDp = getSmallestWidthDp(activity);
        mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
        mActionBarHeight = getActionBarHeight(activity);
        mNavigationBarHeight = getNavigationBarHeight(activity);
        mNavigationBarWidth = getNavigationBarWidth(activity);
        mHasNavigationBar = (mNavigationBarHeight > 0);
        mTranslucentStatusBar = translucentStatusBar;
        mTranslucentNavBar = traslucentNavBar;
    }

    @TargetApi(14)
    private int getActionBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            result = context.getResources().getDimensionPixelSize(tv.resourceId);
        }
        return result;
    }

    @TargetApi(14)
    private int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (!ViewConfiguration.get(context).hasPermanentMenuKey()) {
                String key;
                if (mInPortrait) {
                    key = NAV_BAR_HEIGHT_RES_NAME;
                } else {
                    key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                }
                return getInternalDimensionSize(res, key);
            }
        }
        return result;
    }

    @TargetApi(14)
    private int getNavigationBarWidth(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (!ViewConfiguration.get(context).hasPermanentMenuKey()) { return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME); }
        }
        return result;
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @SuppressLint("NewApi")
    private float getSmallestWidthDp(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        } else {
            // TODO this is not correct, but we don't really care pre-kitkat
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        float widthDp = metrics.widthPixels / metrics.density;
        float heightDp = metrics.heightPixels / metrics.density;
        return Math.min(widthDp, heightDp);
    }

    /**
     * Should a navigation bar appear at the bottom of the screen in the current
     * device configuration? A navigation bar may appear on the right side of
     * the screen in certain configurations.
     * 
     * @return True if navigation should appear at the bottom of the screen,
     *         False otherwise.
     */
    public boolean isNavigationAtBottom() {
        return (mSmallestWidthDp >= 600 || mInPortrait);
    }

    /**
     * Get the height of the system status bar.
     * 
     * @return The height of the status bar (in pixels).
     */
    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    /**
     * Get the height of the action bar.
     * 
     * @return The height of the action bar (in pixels).
     */
    public int getActionBarHeight() {
        return mActionBarHeight;
    }

    /**
     * Does this device have a system navigation bar?
     * 
     * @return True if this device uses soft key navigation, False otherwise.
     */
    public boolean hasNavigtionBar() {
        return mHasNavigationBar;
    }

    /**
     * Get the height of the system navigation bar.
     * 
     * @return The height of the navigation bar (in pixels). If the device does
     *         not have soft navigation keys, this will always return 0.
     */
    public int getNavigationBarHeight() {
        return mNavigationBarHeight;
    }

    /**
     * Get the width of the system navigation bar when it is placed vertically
     * on the screen.
     * 
     * @return The width of the navigation bar (in pixels). If the device does
     *         not have soft navigation keys, this will always return 0.
     */
    public int getNavigationBarWidth() {
        return mNavigationBarWidth;
    }

    /**
     * Get the layout inset for any system UI that appears at the top of the
     * screen.
     * 
     * @param withActionBar
     *            True to include the height of the action bar, False otherwise.
     * @return The layout inset (in pixels).
     */
    public int getPixelInsetTop(boolean withActionBar) {
        return (mTranslucentStatusBar ? mStatusBarHeight : 0) + (withActionBar ? mActionBarHeight : 0);
    }

    /**
     * Get the layout inset for any system UI that appears at the bottom of the
     * screen.
     * 
     * @return The layout inset (in pixels).
     */
    public int getPixelInsetBottom() {
        if (mTranslucentNavBar && isNavigationAtBottom()) {
            return mNavigationBarHeight;
        } else {
            return 0;
        }
    }

    /**
     * Get the layout inset for any system UI that appears at the right of the
     * screen.
     * 
     * @return The layout inset (in pixels).
     */
    public int getPixelInsetRight() {
        if (mTranslucentNavBar && !isNavigationAtBottom()) {
            return mNavigationBarWidth;
        } else {
            return 0;
        }
    }
}
