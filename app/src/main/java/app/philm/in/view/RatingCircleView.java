package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.drawable.PercentageDrawable;
import app.philm.in.lib.util.TypefaceManager;

public class RatingCircleView extends ImageView {

    private PercentageDrawable mDrawable;
    private String mRatePrompt;

    @Inject TypefaceManager mTypefaceManager;

    public RatingCircleView(Context context) {
        this(context, null);
    }

    public RatingCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingCircleView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        PhilmApplication.from(context).inject(this);

        mDrawable = new PercentageDrawable();
        setImageDrawable(mDrawable);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingCircleView);

        mDrawable.setBackgroundCircleColor(
                a.getColor(R.styleable.RatingCircleView_backgroundCircleColor, 0));
        mDrawable.setForegroundCircleColor(
                a.getColor(R.styleable.RatingCircleView_foregroundCircleColor, 0));
        mDrawable.setArcColor(a.getColor(R.styleable.RatingCircleView_arcColor, 0));
        mDrawable.setTextColor(a.getColor(R.styleable.RatingCircleView_textColor, 0));
        mRatePrompt = a.getString(R.styleable.RatingCircleView_ratePrompt);

        Typeface typeface = FontTextView.getFont(mTypefaceManager,
                a.getInt(R.styleable.RatingCircleView_font, 0));
        if (typeface != null) {
            mDrawable.setTypeface(typeface);
        }

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(Math.min(heightSize, widthSize), Math.min(heightSize, widthSize));
    }

    /**
     * @param rating between 0-10.
     */
    public void showRating(int rating) {
        mDrawable.showRating(rating);
    }

    public void showRatePrompt() {
        mDrawable.showPrompt(mRatePrompt);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            if (visibility != VISIBLE) {
                mDrawable.stop();
            }
        }
    }

    public PercentageDrawable getPercentageDrawable() {
        return mDrawable;
    }

    @Override
    protected void onDetachedFromWindow() {
        mDrawable.stop();
        super.onDetachedFromWindow();
    }
}
