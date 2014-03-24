package app.philm.in.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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

    private Drawable mDefaultInsetBackground;
    private Drawable mInsetBackground;

    private Rect mInsets;
    private Rect mTempRect = new Rect();
    private OnInsetsCallback mOnInsetsCallback;

    private int mTopAlpha = 255;
    private int mBottomAlpha = 0;

    public InsetFrameLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public InsetFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public InsetFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DrawInsetsFrameLayout, defStyle, 0);
        if (a == null) {
            return;
        }
        mDefaultInsetBackground = mInsetBackground
                = a.getDrawable(R.styleable.DrawInsetsFrameLayout_insetBackground);
        a.recycle();

        setWillNotDraw(true);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mInsets = new Rect(insets);

        setWillNotDraw(mInsetBackground == null);
        ViewCompat.postInvalidateOnAnimation(this);

        if (mOnInsetsCallback != null) {
            mOnInsetsCallback.onInsetsChanged(insets);
        }
        return true; // consume insets
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        final int width = getWidth();
        final int height = getHeight();
        if (mInsets != null && mInsetBackground != null) {
            // Top
            mTempRect.set(0, 0, width, mInsets.top);
            mInsetBackground.setBounds(mTempRect);
            mInsetBackground.setAlpha(mTopAlpha);
            mInsetBackground.draw(canvas);

            // Bottom
            mTempRect.set(0, height - mInsets.bottom, width, height);
            mInsetBackground.setBounds(mTempRect);
            mInsetBackground.setAlpha(mBottomAlpha);
            mInsetBackground.draw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mInsetBackground != null) {
            mInsetBackground.setCallback(this);
        }
    }

    public void resetInsetBackground() {
        setInsetBackground(mDefaultInsetBackground);
    }

    public void setInsetBackgroundColor(int color) {
        setInsetBackground(new ColorDrawable(color));
    }

    private void setInsetBackground(Drawable background) {
        if (mInsetBackground != null) {
            mInsetBackground.setCallback(null);
        }

        mInsetBackground = background;

        if (mInsetBackground != null && getWindowToken() != null) {
            mInsetBackground.setCallback(this);
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInsetBackground != null) {
            mInsetBackground.setCallback(null);
        }
    }

    public void setTopInsetAlpha(int alpha) {
        mTopAlpha = alpha;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setBottomInsetAlpha(int alpha) {
        mBottomAlpha = alpha;
        ViewCompat.postInvalidateOnAnimation(this);
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