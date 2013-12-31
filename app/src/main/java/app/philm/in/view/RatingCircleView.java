package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import app.philm.in.R;
import app.philm.in.drawable.PercentageDrawable;

public class RatingCircleView extends ImageView {

    private PercentageDrawable mDrawable;
    private String mRatePrompt;

    public RatingCircleView(Context context) {
        this(context, null);
    }

    public RatingCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingCircleView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(Math.min(heightSize, widthSize), Math.min(heightSize, widthSize));
    }

    /**
     * @param rating between 0 & 100.
     */
    public void showRating(int rating) {
        mDrawable.showRating(rating);
    }

    public void showRatePrompt() {
        mDrawable.showRate(mRatePrompt);
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

    @Override
    protected void onDetachedFromWindow() {
        mDrawable.stop();
        super.onDetachedFromWindow();
    }
}
