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
import android.graphics.Paint;
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

public class CollapsingTitleLayout extends FrameLayout {

    private static final float DEFAULT_MIN_TEXT_SIZE = 12f; // 12dp

    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT;
    static {
        DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
        if (DEBUG_DRAW_PAINT != null) {
            DEBUG_DRAW_PAINT.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
        }
    }

    private static final Rect TEMP_RECT = new Rect();

    private Toolbar mToolbar;
    private View mDummyView;

    private float mScrollOffset;

    private final Rect mToolbarContentBounds;
    private final Rect mTextPaintBounds;
    private final Rect mDrawnTextBounds;

    private float mMinTextSize;

    private float mExpandedMargin;
    private float mRequestedExpandedTitleTextSize;
    private float mExpandedTitleTextSize;
    private float mRequestedCollapsedTitleTextSize;
    private float mCollapsedTitleTextSize;

    private float mExpandedTop;
    private float mCollapsedTop;

    private String mTitle;

    private String mTitleToDraw;

    private float mTextLeft;
    private float mTextRight;
    private float mTextTop;

    private float mScale;

    private final TextPaint mTextPaint;

    public CollapsingTitleLayout(Context context) {
        this(context, null);
    }

    public CollapsingTitleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsingTitleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);

        mMinTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MIN_TEXT_SIZE,
                getResources().getDisplayMetrics());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsingTitleLayout);

        mExpandedMargin = a.getDimensionPixelSize(
                R.styleable.CollapsingTitleLayout_expandedMargin, 0);
        mRequestedExpandedTitleTextSize = a.getDimensionPixelSize(
                R.styleable.CollapsingTitleLayout_expandedTextSize, 0);
        mRequestedCollapsedTitleTextSize = a.getDimensionPixelSize(
                R.styleable.CollapsingTitleLayout_collapsedTextSize, 0);
        mTextPaint.setColor(a.getColor(
                R.styleable.CollapsingTitleLayout_android_textColor, Color.WHITE));

        final int defaultMinTextSize = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MIN_TEXT_SIZE,
                        getResources().getDisplayMetrics());
        mMinTextSize = a.getDimensionPixelSize(R.styleable.CollapsingTitleLayout_minTextSize,
                defaultMinTextSize);

        a.recycle();

        mTextPaintBounds = new Rect();
        mDrawnTextBounds = new Rect();
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

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     *
     * A value of {@code 0.0} indicates that the layout is fully expanded.
     * A value of {@code 1.0} indicates that the layout is fully collapsed.
     */
    public void setScrollOffset(float offset) {
        if (offset != mScrollOffset) {
            mScrollOffset = offset;
            calculateOffsets();
        }
    }

    private void calculateOffsets() {
        final float offset = mScrollOffset;

        mTextLeft = interpolate(mExpandedMargin, mToolbarContentBounds.left, offset);
        mTextTop = interpolate(mExpandedTop, mCollapsedTop, offset);
        mTextRight = interpolate(getWidth() - mExpandedMargin, mToolbarContentBounds.right, offset);

        setInterpolatedTextSize(interpolate(mExpandedTitleTextSize,
                mCollapsedTitleTextSize, offset));

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void calculateTextBounds() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        // First, let's calculate the expanded text size so that it fit within the bounds
        // We make sure this value is at least our minimum text size
        mExpandedTitleTextSize = Math.max(mMinTextSize,
                getSingleLineTextSize(mTitle, mTextPaint, getWidth() - (mExpandedMargin * 2f), 0f,
                        mRequestedExpandedTitleTextSize, 0.5f, metrics));
        mTextPaint.setTextSize(mExpandedTitleTextSize);
        mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), TEMP_RECT);
        mExpandedTop = getHeight() - TEMP_RECT.height() - mExpandedMargin;

        // We then calculate the collapsed text size, using the same logic
        mCollapsedTitleTextSize = Math.max(mMinTextSize,
                getSingleLineTextSize(mTitle, mTextPaint, mToolbarContentBounds.width(), 0f,
                        mRequestedCollapsedTitleTextSize, 0.5f, metrics));
        mTextPaint.setTextSize(mCollapsedTitleTextSize);
        mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), TEMP_RECT);
        mCollapsedTop = mToolbarContentBounds.centerY() - (TEMP_RECT.height() / 2f);
    }

    @Override
    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();

        final int toolbarHeight = mToolbar.getHeight();
        canvas.clipRect(0, 0, canvas.getWidth(),
                interpolate(canvas.getHeight(), toolbarHeight, mScrollOffset));

        // Now call super and let it draw the background, etc
        super.draw(canvas);

        if (mTitleToDraw != null) {
            float x = -mDrawnTextBounds.left + mTextLeft;
            float y = -mDrawnTextBounds.top + mTextTop;

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a Magneta rect in the text bounds
                canvas.drawRect(mTextLeft, mTextTop, mTextRight,
                        mTextTop + mDrawnTextBounds.height(),
                        DEBUG_DRAW_PAINT);
            }

            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }

            canvas.drawText(mTitleToDraw, x, y, mTextPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private void setInterpolatedTextSize(final float textSize) {
        if (mTitle == null) return;

        if (isCloseToDecimal(textSize) || isClose(textSize, mCollapsedTitleTextSize)
                || isClose(textSize, mExpandedTitleTextSize) || mTitleToDraw == null) {
            // If the text size is 'close' to being a decimal, then we use this as a sync-point.
            // We disable our manual scaling and set the paint's text size.
            mTextPaint.setTextSize(textSize);
            mScale = 1f;

            // We also use this as an opportunity to ellipsize the string
            final CharSequence title = TextUtils.ellipsize(mTitle, mTextPaint,
                    (mTextRight - mTextLeft),
                    TextUtils.TruncateAt.END);
            if (title != mTitleToDraw) {
                // If the title has changed, turn it into a string
                mTitleToDraw = title.toString();
            }

            // As we've changed the text size (and possibly the text) we'll re-measure the text
            mTextPaint.getTextBounds(mTitleToDraw, 0, mTitleToDraw.length(), mTextPaintBounds);
            mDrawnTextBounds.set(mTextPaintBounds);
        } else {
            // We're not close to a decimal so use our canvas scaling method
            mScale = textSize / mTextPaint.getTextSize();

            // Because we're scaling using canvas, we need to update the drawn text bounds too
            mDrawnTextBounds.set(mTextPaintBounds);
            mDrawnTextBounds.left *= mScale;
            mDrawnTextBounds.top *= mScale;
            mDrawnTextBounds.right *= mScale;
            mDrawnTextBounds.bottom *= mScale;
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mToolbarContentBounds.left = mDummyView.getLeft();
        mToolbarContentBounds.top = mDummyView.getTop();
        mToolbarContentBounds.right = mDummyView.getRight();
        mToolbarContentBounds.bottom = mDummyView.getBottom();

        if (changed && mTitle != null) {
            // If we've changed and we have a title, re-calculate everything!
            calculateTextBounds();
            calculateOffsets();
        }
    }

    /**
     * Set the title to display
     *
     * @param title
     */
    public void setTitle(String title) {
        if (title == null || !title.equals(mTitle)) {
            mTitle = title;

            if (getHeight() > 0) {
                // If we've already been laid out, calculate everything now otherwise we'll wait
                // until a layout
                calculateTextBounds();
                calculateOffsets();
            }
        }
    }

    /**
     * Recursive binary search to find the best size for the text
     *
     * Adapted from https://github.com/grantland/android-autofittextview
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

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.01.
     */
    private static boolean isCloseToDecimal(float value) {
        final float absValue = Math.abs(value);
        return Math.abs(absValue - Math.round(absValue)) < 0.01f;
    }

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.01.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.01f;
    }

    /**
     * Interpolate between {@code startValue} and {@code endValue}, using {@code progress}.
     */
    private static float interpolate(float startValue, float endValue, float progress) {
        return startValue + ((endValue - startValue) * progress);
    }
}
