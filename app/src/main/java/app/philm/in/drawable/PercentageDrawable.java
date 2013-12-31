package app.philm.in.drawable;

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
    private static final float FOREGROUND_CIRCLE_RADIUS_RATIO = 0.7f / 2f;
    private static final float TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.725f;

    private final Paint mBackgroundCirclePaint;
    private final Paint mForegroundCirclePaint;
    private final Paint mArcPaint;
    private final Paint mTextPaint;

    private final Rect mTextBounds;
    private final RectF mBounds;

    private ValueAnimator mAnimator;

    private int mCurrentValue;
    private int mTargetValue;

    private String mText;

    public PercentageDrawable(Resources resources) {
        mBounds = new RectF();
        mTextBounds = new Rect();

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(0xFF99CC00);

        mBackgroundCirclePaint = new Paint();
        mBackgroundCirclePaint.setAntiAlias(true);
        mBackgroundCirclePaint.setColor(0xFFDDDDDD);

        mForegroundCirclePaint = new Paint();
        mForegroundCirclePaint.setAntiAlias(true);
        mForegroundCirclePaint.setColor(0xFF669900);

        mTextPaint = new Paint();
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(0xFF99CC00);
    }

    @Override
    public void draw(Canvas canvas) {
        mBounds.set(getBounds());

        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(),
                mBounds.height() * BACKGROUND_CIRCLE_RADIUS_RATIO, mBackgroundCirclePaint);

        final float arcAngle = (mCurrentValue / 100f) * 360f;
        canvas.drawArc(mBounds, -90f, arcAngle, true, mArcPaint);

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
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mArcPaint.setColorFilter(colorFilter);
        mBackgroundCirclePaint.setColorFilter(colorFilter);
        mForegroundCirclePaint.setColorFilter(colorFilter);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateTextSize(bounds);
    }

    private void updateTextSize(final Rect bounds) {
        final int targetWidth = Math.round(bounds.width() * TEXT_SIZE_BOUNDS_RATIO);
        final int targetHeight = Math.round(bounds.height() * TEXT_SIZE_BOUNDS_RATIO);

        float textSize = 4f;
        do {
            mTextPaint.setTextSize(textSize);
            mTextPaint.getTextBounds("100", 0, 3, mTextBounds);
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
        mCurrentValue = 0;
        mTargetValue = percentage;

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(1250);
        mAnimator.setInterpolator(INTERPOLATOR);
        mAnimator.setIntValues(mCurrentValue, mTargetValue);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentValue = (Integer) valueAnimator.getAnimatedValue();
                invalidateSelf();
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
}
