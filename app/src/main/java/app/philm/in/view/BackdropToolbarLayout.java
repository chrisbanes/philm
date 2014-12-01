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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import app.philm.in.R;

import static app.philm.in.util.AnimationUtils.interpolate;

public class BackdropToolbarLayout extends FrameLayout {

    private static final float DEFAULT_MIN_TEXT_SIZE = 32f;

    private static final Rect TEMP_RECT = new Rect();

    private Toolbar mToolbar;
    private View mDummyView;

    private float mScrollOffset;

    private final Rect mToolbarContentBounds;
    private final Rect mCurrentTextBounds;

    private final float mMinTextSize;

    private float mInitialTitleMargin;
    private float mRequestedInitialTitleTextSize;

    private float mInitialTitleTextSize;
    private float mRequestedFinalTitleTextSize;
    private float mFinalTitleTextSize;

    private float mInitialTop;
    private float mFinalTop;

    private String mTitle;

    private String mTitleToDraw;

    private float mTextLeft;
    private float mTextTop;

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

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);

        mMinTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MIN_TEXT_SIZE,
                getResources().getDisplayMetrics());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackdropToolbarLayout);

        mInitialTitleMargin = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialMargin, 0);
        mRequestedInitialTitleTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialTextSize, 0);
        mRequestedFinalTitleTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleFinalTextSize, 0);
        mTextPaint.setColor(a.getColor(
                R.styleable.BackdropToolbarLayout_android_textColor, Color.WHITE));

        a.recycle();

        mCurrentTextBounds = new Rect();
        mToolbarContentBounds = new Rect();

        setWillNotDraw(false);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (child instanceof Toolbar) {
            mToolbar = (Toolbar) child;
            mDummyView = new View(getContext());
            mToolbar.addView(mDummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    public void setScrollOffset(float offset) {
        if (offset != mScrollOffset) {
            mScrollOffset = offset;
            calculateOffsets();
        }
    }

    private void calculateOffsets() {
        final float offset = mScrollOffset;

        mTextTop = interpolate(mInitialTop, mFinalTop, offset);
        mTextLeft = interpolate(mInitialTitleMargin, mToolbarContentBounds.left, offset);
        setInterpolatedTextSize(interpolate(mInitialTitleTextSize, mFinalTitleTextSize, offset));

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void calculateTextBounds() {
        mInitialTitleTextSize = getSingleLineTextSize(mTitle, mTextPaint,
                getWidth() - (mInitialTitleMargin * 2f), 0f,
                mRequestedInitialTitleTextSize, 0.5f, getResources().getDisplayMetrics());

        if (mInitialTitleTextSize < mMinTextSize) {
            mInitialTitleTextSize = mMinTextSize;
            mTextPaint.setTextSize(mInitialTitleTextSize);

            mTitleToDraw = TextUtils.ellipsize(mTitle, mTextPaint,
                    getWidth() - (mInitialTitleMargin * 2f),
                    TextUtils.TruncateAt.END).toString();
        } else {
            mTitleToDraw = mTitle;
            mTextPaint.setTextSize(mInitialTitleTextSize);
        }
        mTextPaint.getTextBounds(mTitleToDraw, 0, mTitleToDraw.length(), TEMP_RECT);
        mInitialTop = getHeight() - TEMP_RECT.height() - mInitialTitleMargin;

        mFinalTitleTextSize = getSingleLineTextSize(mTitleToDraw, mTextPaint,
                mToolbarContentBounds.width(),
                0f, mRequestedFinalTitleTextSize, 0.5f, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(mFinalTitleTextSize);
        mTextPaint.getTextBounds(mTitleToDraw, 0, mTitleToDraw.length(), TEMP_RECT);
        mFinalTop = mToolbarContentBounds.centerY() - (TEMP_RECT.height() / 2f);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mTitleToDraw != null) {
            canvas.save();
            float x = -mCurrentTextBounds.left + mTextLeft;
            float y = -mCurrentTextBounds.top + mTextTop;
            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }
            canvas.drawText(mTitleToDraw, x, y, mTextPaint);
            canvas.restore();
        }
    }

    private void setInterpolatedTextSize(float textSize) {
        final int pxSize = Math.round(textSize);

        if (pxSize == mFinalTitleTextSize || pxSize == mInitialTitleTextSize || pxSize % 16 == 0) {
            mTextPaint.setTextSize(textSize);
            mScale = 1f;
        } else {
            mScale = textSize / mTextPaint.getTextSize();
        }

        if (mTitleToDraw != null) {
            mTextPaint.getTextBounds(mTitleToDraw, 0, mTitleToDraw.length(), mCurrentTextBounds);
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
