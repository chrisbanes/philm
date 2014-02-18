package app.philm.in.view;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import app.philm.in.R;

public class ParallaxContentScrollView extends FrameLayout {

    private static final float PARALLAX_FRICTION = 0.5f;

    public interface OnContentViewScrollListener {
        void onContentViewScrolled(float percent);
    }

    private View mHeaderView;
    private View mContentView;

    private FrameLayout mContentViewWrapper;
    private NotifyingScrollView mContentViewScrollView;

    private OnContentViewScrollListener mContentViewScrollListener;

    private int mContentOverlaySize;

    private int mOriginalHeaderViewHeight;
    private int mHeaderViewHeightDiff;

    public ParallaxContentScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ParallaxContentScrollView);

        mContentOverlaySize = a.getDimensionPixelSize(
                R.styleable.ParallaxContentScrollView_contentOverlay, 0);

        a.recycle();
    }

    public void setOnContentViewScrollListener(OnContentViewScrollListener listener) {
        mContentViewScrollListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Preconditions.checkState(getChildCount() == 2, "There must be children exactly");

        // Get the Views
        mHeaderView = getChildAt(0);
        mContentView = getChildAt(1);

        // Now remove so that we can add our ScrollView
        removeView(mContentView);

        mContentViewWrapper = new FrameLayout(getContext());
        mContentViewWrapper.addView(mContentView);

        mContentViewScrollView = new NotifyingScrollView(getContext());
        mContentViewScrollView.setFillViewport(true);
        mContentViewScrollView.addView(mContentViewWrapper,
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mContentViewScrollView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mContentViewScrollView.setOnScrollChangedListener(
                new NotifyingScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                updateOffset(t);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mOriginalHeaderViewHeight <= 0) {
            mOriginalHeaderViewHeight = mHeaderView.getHeight();
        }

        final int targetHeaderHeight = mOriginalHeaderViewHeight + mHeaderViewHeightDiff;
        mHeaderView.layout(left, 0, right, targetHeaderHeight);

        updateContentViewPaddingTop();
        updateOffset(mContentViewScrollView.getScrollY());
    }

    void updateContentViewPaddingTop() {
        final int targetPaddingTop = mHeaderView.getHeight() - mContentOverlaySize;

        if (mContentViewWrapper.getPaddingTop() != targetPaddingTop) {
            mContentViewWrapper.post(new Runnable() {
                @Override
                public void run() {
                    mContentViewWrapper.setPadding(0, targetPaddingTop, 0, 0);
                }
            });
        }
    }

    public void increaseHeaderViewHeight(int diffPx) {
        mHeaderViewHeightDiff = diffPx;
        requestLayout();
    }

    public void setInsets(Rect insets) {
        mContentView.setPadding(insets.left, 0, insets.right, insets.bottom);
        increaseHeaderViewHeight(insets.top);
    }

    void updateOffset(int y) {
        if (y <= mHeaderView.getHeight()) {
            mHeaderView.setVisibility(View.VISIBLE);

            final int newTop = Math.round(-y * PARALLAX_FRICTION);
            mHeaderView.offsetTopAndBottom(newTop - mHeaderView.getTop());

            if (mContentViewScrollListener != null) {
                mContentViewScrollListener.onContentViewScrolled(
                        y / (float) mHeaderView.getHeight());
            }
        } else {
            mHeaderView.setVisibility(View.INVISIBLE);
        }
    }

    public int getScrollViewScrollY() {
        return mContentViewScrollView.getScrollY();
    }

    public void scrollScrollViewTo(final int y) {
        mContentViewScrollView.post(new Runnable() {
            @Override
            public void run() {
                mContentViewScrollView.scrollTo(0, y);
            }
        });
    }
}
