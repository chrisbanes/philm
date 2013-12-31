package app.philm.in.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class PercentageDrawable extends Drawable {

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private static final float BACKGROUND_CIRCLE_RADIUS_RATIO = 1f / 2f;
    private static final float FOREGROUND_CIRCLE_RADIUS_RATIO = 0.8f / 2f;
    private static final float TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.725f;

    private final Paint mBackgroundCirclePaint;
    private final Paint mForegroundCirclePaint;
    private final Paint mArcPaint;
    private final Paint mTextPaint;

    private final Rect mTextBounds;
    private final RectF mBounds;

    private ValueAnimator mAnimator;

    private float mCurrentValue;
    private float mTargetValue;

    private String mText;

    private boolean mReverseMode;

    public PercentageDrawable() {
        mBounds = new RectF();
        mTextBounds = new Rect();

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);

        mBackgroundCirclePaint = new Paint();
        mBackgroundCirclePaint.setAntiAlias(true);

        mForegroundCirclePaint = new Paint();
        mForegroundCirclePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setSubpixelText(true);
        mTextPaint.setAntiAlias(true);
    }

    public void setTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        updateTextSize();
    }

    public void setBackgroundCircleColor(int color) {
        mBackgroundCirclePaint.setColor(color);
    }

    public void setForegroundCircleColor(int color) {
        mForegroundCirclePaint.setColor(color);
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        mBounds.set(getBounds());

        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(),
                mBounds.height() * BACKGROUND_CIRCLE_RADIUS_RATIO, mBackgroundCirclePaint);

        final float arcAngle = mCurrentValue * 360f;
        if (mReverseMode) {
            canvas.drawArc(mBounds, arcAngle - 90f, 360f - arcAngle, true, mArcPaint);
        } else {
            canvas.drawArc(mBounds, -90f, arcAngle, true, mArcPaint);
        }

        canvas.drawCircle(mBounds.centerX(),
                mBounds.centerY(),
                mBounds.height() * FOREGROUND_CIRCLE_RADIUS_RATIO,
                mForegroundCirclePaint);

        if (mText != null) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            canvas.drawText(mText,
                    mBounds.centerX() - (mTextBounds.width() / 2f),
                    mBounds.centerY() + (mTextBounds.height() / 2f),
                    mTextPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mArcPaint.setAlpha(alpha);
        mBackgroundCirclePaint.setAlpha(alpha);
        mForegroundCirclePaint.setAlpha(alpha);
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mArcPaint.setColorFilter(colorFilter);
        mBackgroundCirclePaint.setColorFilter(colorFilter);
        mForegroundCirclePaint.setColorFilter(colorFilter);
        mTextPaint.setColorFilter(colorFilter);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mText != null) {
            updateTextSize();
        }
    }

    private void updateTextSize() {
        Rect bounds = getBounds();
        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        final int targetWidth = Math.round(bounds.width() * TEXT_SIZE_BOUNDS_RATIO);
        final int targetHeight = Math.round(bounds.height() * TEXT_SIZE_BOUNDS_RATIO);

        float textSize = 4f;
        do {
            mTextPaint.setTextSize(textSize);
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            textSize += 2f;
        } while (mTextBounds.width() < targetWidth && mTextBounds.height() < targetHeight);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public void showRating(int percentage) {
        if (isRunning()) {
            stop();
        }

        mText = String.valueOf(percentage);
        updateTextSize();

        mTargetValue = percentage / 100f;

        mAnimator = createAnimator(0f, mTargetValue);
        mAnimator.start();
    }

    public void showRate(String promptText) {
        if (isRunning()) {
            stop();
        }

        mText = promptText;
        updateTextSize();

        mAnimator = createAnimator(0f, 1f);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mReverseMode = !mReverseMode;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mReverseMode = false;
            }
        });
        mAnimator.start();
    }

    public void stop() {
        if (mAnimator != null) {
            mAnimator.end();
        }
    }

    public boolean isRunning() {
        return mAnimator != null ? mAnimator.isRunning() : false;
    }

    private ValueAnimator createAnimator(float startValue, float endValue) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(1250);
        animator.setInterpolator(INTERPOLATOR);
        animator.setFloatValues(startValue, endValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentValue = (Float) valueAnimator.getAnimatedValue();
                invalidateSelf();
            }
        });
        return animator;
    }
}
