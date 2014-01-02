package app.philm.in.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import app.philm.in.util.ColorUtils;

public class PercentageDrawable extends Drawable {

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private static final float BACKGROUND_CIRCLE_RADIUS_RATIO = 1f / 2f;
    private static final float FOREGROUND_CIRCLE_RADIUS_RATIO = 0.8f / 2f;

    private static final float SMALL_TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.5f;
    private static final float TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.7f;

    private static final float PRESSED_DARKEN_RATIO = 0.15f;

    private final Paint mBackgroundCirclePaint;
    private final Paint mForegroundCirclePaint;
    private final Paint mArcPaint;
    private final Paint mTextPaint;

    private int mForegroundCircleColor;

    private final Rect mTextBounds;
    private final RectF mBounds;

    private ValueAnimator mAnimator;

    private float mCurrentValue;
    private float mTargetValue;

    private String mText;

    private boolean mAntiClockwiseMode;

    private boolean mEnabled;
    private boolean mPressed;

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
        mForegroundCircleColor = color;
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
        if (mAntiClockwiseMode) {
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
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean pressed = false;
        boolean enabled = false;

        for (int i = 0, z = state.length ; i < z ; i++) {
            switch (state[i]) {
                case android.R.attr.state_pressed:
                    pressed = true;
                    break;
                case android.R.attr.state_enabled:
                    enabled = true;
                    break;
            }
        }

        boolean causedInvalidate = false;
        if (setPressed(pressed)) {
            causedInvalidate = true;
        }
        if (setEnabled(enabled)) {
            causedInvalidate = true;
        }
        return causedInvalidate;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mText != null) {
            updateTextSize();
        }
    }

    private boolean setPressed(boolean pressed) {
        if (pressed != mPressed) {
            mPressed = pressed;
            if (pressed) {
                mForegroundCirclePaint.setColor(
                        ColorUtils.darken(mForegroundCircleColor, PRESSED_DARKEN_RATIO));
            } else {
                mForegroundCirclePaint.setColor(mForegroundCircleColor);
            }
            invalidateSelf();
            return true;
        }
        return false;
    }

    private boolean setEnabled(boolean enabled) {
        if (enabled != mEnabled) {
            mEnabled = enabled;
            if (enabled) {
                setAlpha(255);
            } else {
                setAlpha(Math.round(255 * 0.3f));
                stop();
            }
            invalidateSelf();
            return true;
        }
        return false;
    }

    private void updateTextSize() {
        Rect bounds = getBounds();
        if (mText == null || bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        final float boundsRatio = mText.length() <= 2
                ? SMALL_TEXT_SIZE_BOUNDS_RATIO
                : TEXT_SIZE_BOUNDS_RATIO;

        final int targetWidth = Math.round(bounds.width() * boundsRatio);
        final int targetHeight = Math.round(bounds.height() * boundsRatio);

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

    public void showRating(int rating) {
        if (isRunning()) {
            stop();
        }

        mText = String.valueOf(rating);
        updateTextSize();

        mTargetValue = rating / 10f;

        if (shouldAnimate()) {
            mAnimator = createAnimator(0f, mTargetValue);
            mAnimator.start();
        }
    }

    public void showPrompt(String promptText) {
        if (isRunning()) {
            stop();
        }

        mText = promptText;
        updateTextSize();

        if (shouldAnimate()) {
            mAnimator = createAnimator(0f, 1f);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    mAntiClockwiseMode = !mAntiClockwiseMode;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAntiClockwiseMode = false;
                }
            });
            mAnimator.start();
        }
    }

    public void stop() {
        if (mAnimator != null) {
            mAnimator.end();
        }
    }

    private boolean shouldAnimate() {
        return mEnabled;
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
