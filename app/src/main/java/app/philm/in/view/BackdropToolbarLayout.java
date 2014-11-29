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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import app.philm.in.R;

import static app.philm.in.util.AnimationUtils.interpolate;

public class BackdropToolbarLayout extends FrameLayout {

    private static final Rect TEMP_RECT = new Rect();

    private Toolbar mToolbar;
    private View mDummyView;
    private BackdropImageView mBackdropImageView;

    private float mScrollOffset;

    private final Rect mToolbarContentBounds;
    private final Rect mCurrentTextBounds;

    private float mInitialTitleMargin;
    private float mRequestedInitialTitleTextSize;
    private float mTitleInitialTextSize;
    private float mRequestedFinalTitleTextSize;
    private float mTitleFinalTextSize;

    private float mInitialTop;
    private float mFinalTop;

    private String mTitle;

    private float mTextLeft;
    private float mTextTop;
    private float mTextSize;

    private float mScale;

    private final TextPaint mTextPaint;

    public BackdropToolbarLayout(Context context) {
        this(context, null);
    }

    public BackdropToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackdropToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_backdrop_toolbar, this, true);

        mBackdropImageView = (BackdropImageView) findViewById(R.id.imageview_fanart);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDummyView = mToolbar.findViewById(R.id.dummy);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackdropToolbarLayout);

        mInitialTitleMargin = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialMargin, 0);
        mRequestedInitialTitleTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialTextSize, 0);
        mRequestedFinalTitleTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleFinalTextSize, 0);

        a.recycle();

        mCurrentTextBounds = new Rect();
        mToolbarContentBounds = new Rect();

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    public void setScrollOffset(float offset) {
        if (offset != mScrollOffset) {
            mScrollOffset = offset;
            calculateOffsets();
        }
    }

    private void calculateOffsets() {
        final float offset = mScrollOffset;
        final int offsetPx = (int) ((getHeight() - mToolbar.getHeight()) * offset);
        mBackdropImageView.setScrollOffset(-offsetPx);

        mTextTop = interpolate(mInitialTop, mFinalTop, offset);
        mTextLeft = interpolate(mInitialTitleMargin, mToolbarContentBounds.left, offset);
        setInterpolatedTextSize(interpolate(mTitleInitialTextSize, mTitleFinalTextSize, offset));

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void calculateTextBounds() {
        mTitleInitialTextSize = getSingleLineTextSize(mTitle, mTextPaint,
                getWidth() - (mInitialTitleMargin * 2f), 0f,
                mRequestedInitialTitleTextSize, 0.5f, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(mTitleInitialTextSize);
        mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), TEMP_RECT);
        mInitialTop = getHeight() - TEMP_RECT.height() - mInitialTitleMargin;

        mTitleFinalTextSize = getSingleLineTextSize(mTitle, mTextPaint,
                mToolbarContentBounds.width(),
                0f, mRequestedFinalTitleTextSize, 0.5f, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(mTitleFinalTextSize);
        mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), TEMP_RECT);
        mFinalTop = mToolbarContentBounds.centerY() - (TEMP_RECT.height() / 2f);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mTitle != null) {
            canvas.save();
            float x = -mCurrentTextBounds.left + mTextLeft;
            float y = -mCurrentTextBounds.top + mTextTop;
            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }
            canvas.drawText(mTitle, x, y, mTextPaint);
            canvas.restore();
        }
    }

    private void setInterpolatedTextSize(float textSize) {
        final int pxSize = Math.round(textSize);

        if (pxSize == mTitleFinalTextSize || pxSize == mTitleInitialTextSize || pxSize % 16 == 0) {
            mTextSize = textSize;
            mTextPaint.setTextSize(mTextSize);
            mScale = 1f;
        } else {
            mScale = textSize / mTextSize;
        }

        if (mTitle != null) {
            mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), mCurrentTextBounds);
            mCurrentTextBounds.left *= mScale;
            mCurrentTextBounds.top *= mScale;
            mCurrentTextBounds.right *= mScale;
            mCurrentTextBounds.bottom *= mScale;
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            mToolbarContentBounds.left = mDummyView.getLeft();
            mToolbarContentBounds.top = mDummyView.getTop();
            mToolbarContentBounds.right = mDummyView.getRight();
            mToolbarContentBounds.bottom = mDummyView.getBottom();

            if (mTitle != null) {
                calculateTextBounds();
                calculateOffsets();
            }
        }
    }

    public void setScrimColor(int scrimColor) {
        mBackdropImageView.setScrimColor(scrimColor);
    }

    public BackdropImageView getImageView() {
        return mBackdropImageView;
    }

    public void setTitle(String title) {
        if (title == null || !title.equals(mTitle)) {
            mTitle = title;

            if (getHeight() > 0) {
                calculateTextBounds();
                calculateOffsets();
            }
        }
    }

    /**
     * Recursive binary search to find the best size for the text
     */
    private static float getSingleLineTextSize(String text, TextPaint paint, float targetWidth,
            float low, float high, float precision, DisplayMetrics metrics) {
        final float mid = (low + high) / 2.0f;

        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, metrics));
        final float maxLineWidth = paint.measureText(text);

        if ((high - low) < precision) {
            return low;
        } else if (maxLineWidth > targetWidth) {
            return getSingleLineTextSize(text, paint, targetWidth, low, mid, precision, metrics);
        } else if (maxLineWidth < targetWidth) {
            return getSingleLineTextSize(text, paint, targetWidth, mid, high, precision, metrics);
        } else {
            return mid;
        }
    }
}
