/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import app.philm.in.R;

/**
 * A layout that draws something in the insets passed to {@link #fitSystemWindows(Rect)}, i.e. the
 * area above UI chrome (status and navigation bars, overlay action bars).
 */
public class InsetFrameLayout extends FrameLayout {

    private OnInsetsCallback mOnInsetsCallback;

    private Rect mInsets;

    public InsetFrameLayout(Context context) {
        super(context);
    }

    public InsetFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InsetFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mInsets = new Rect(insets);
        ViewCompat.postInvalidateOnAnimation(this);

        if (mOnInsetsCallback != null) {
            mOnInsetsCallback.onInsetsChanged(mInsets);
        }
        return true; // consume insets
    }

    /**
     * Allows the calling container to specify a callback for custom processing when insets change
     * (i.e. when {@link #fitSystemWindows(Rect)} is called. This is useful for setting padding on
     * UI elements based on UI chrome insets (e.g. a Google Map or a ListView). When using with
     * ListView or GridView, remember to set clipToPadding to false.
     */
    public void setOnInsetsCallback(OnInsetsCallback onInsetsCallback) {
        mOnInsetsCallback = onInsetsCallback;
    }

    public static interface OnInsetsCallback {
        public void onInsetsChanged(Rect insets);
    }
}