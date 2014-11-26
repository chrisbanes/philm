package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import app.philm.in.R;
import app.philm.in.util.AnimationUtils;

public class BackdropToolbarLayout extends FrameLayout {

    private BackdropImageView mBackdropImageView;
    private AutofitTextView mTitleView;

    private int mTitleInitialLeft;
    private int mTitleInitialRight;
    private int mTitleFinalLeft;

    private int mTitleInitialTextSize;
    private boolean mAutoFit;
    private int mTitleFinalTextSize;

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
        mTitleView = (AutofitTextView) findViewById(R.id.toolbar_title);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackdropToolbarLayout);

        mTitleInitialLeft = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialLeft, 0);
        mTitleInitialRight = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialRight, 0);
        mTitleFinalLeft = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleFinalLeft, 0);
        mTitleInitialTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleInitialTextSize, 0);
        mTitleFinalTextSize = a.getDimensionPixelSize(
                R.styleable.BackdropToolbarLayout_titleFinalTextSize, 0);

        a.recycle();

        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleInitialTextSize);

        LayoutParams lp = (LayoutParams) mTitleView.getLayoutParams();
        if (lp != null) {
            lp.leftMargin = mTitleInitialLeft;
            lp.rightMargin = mTitleInitialRight;
        }
    }

    public void setScrollOffset(float offset) {
        float offsetPx = getHeight() * -offset;

        mBackdropImageView.setScrollOffset((int) offsetPx);
        mTitleView.setTranslationY(offsetPx);
        mTitleView.setTranslationX(offset * (mTitleFinalLeft - mTitleInitialLeft));

        setInterpolatedTextSize(
                AnimationUtils.interpolate(mTitleInitialTextSize, mTitleFinalTextSize, offset));
    }

    private void setInterpolatedTextSize(float textSize) {
        final int pxSize = Math.round(textSize);

        if (pxSize == mTitleFinalTextSize || pxSize == mTitleInitialTextSize || pxSize % 8 == 0) {
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTitleView.setScaleX(1f);
            mTitleView.setScaleY(1f);
        } else {
            float scale = textSize / mTitleView.getTextSize();
            mTitleView.setScaleX(scale);
            mTitleView.setScaleY(scale);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!mAutoFit) {
            mTitleInitialTextSize = (int) mTitleView.getTextSize();
            mTitleView.setSizeToFit(false);
            mAutoFit = true;
        }

        mTitleView.setPivotX(0f);
        mTitleView.setPivotY(mTitleView.getHeight());
    }

    public void setScrimColor(int scrimColor) {
        mBackdropImageView.setScrimColor(scrimColor);
    }

    public BackdropImageView getImageView() {
        return mBackdropImageView;
    }

    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }
}
