package app.philm.in.drawable;

import android.animation.ValueAnimator;
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

import app.philm.in.lib.util.ColorUtils;

public class PercentageDrawable extends Drawable {

    private static final int MODE_RATING = 1;
    private static final int MODE_PROMPT = 2;

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private static final float BACKGROUND_CIRCLE_RADIUS_RATIO = 1f / 2f;
    private static final float FOREGROUND_CIRCLE_RADIUS_RATIO = 0.87f / 2f;

    private static final float SMALL_TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.55f;
    private static final float TEXT_SIZE_BOUNDS_RATIO = FOREGROUND_CIRCLE_RADIUS_RATIO * 2f * 0.75f;

    private static final float PRESSED_DARKEN_RATIO = 0.15f;

    private final Paint mBackgroundArcPaint;
    private final Paint mForegroundCirclePaint;
    private final Paint mArcPaint;
    private final Paint mTextPaint;

    private int mForegroundCircleColor;

    private final Rect mTextBounds;
    private final RectF mBounds;

    private ValueAnimator mAnimator;

    private int mUserRating;

    private float mCurrentValue;
    private float mTargetValue;

    private String mText;
    private float mTextWidth, mTextHeight;

    private boolean mEnabled;
    private boolean mPressed;

    private int mMode;

    public PercentageDrawable() {
        mBounds = new RectF();
        mTextBounds = new Rect();

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);

        mBackgroundArcPaint = new Paint();
        mBackgroundArcPaint.setAntiAlias(true);

        mForegroundCirclePaint = new Paint();
        mForegroundCirclePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setSubpixelText(true);
        mTextPaint.setAntiAlias(true);
    }

    public void setTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        updateTextSize();
        invalidateSelf();
    }

    public void setBackgroundCircleColor(int color) {
        mBackgroundArcPaint.setColor(color);
        invalidateSelf();
    }

    public void setForegroundCircleColor(int color) {
        mForegroundCircleColor = color;
        mForegroundCirclePaint.setColor(color);
        invalidateSelf();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidateSelf();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidateSelf();
    }

    public int getBackgroundCircleColor() {
        return mBackgroundArcPaint.getColor();
    }

    public int getForegroundCircleColor() {
        return mForegroundCirclePaint.getColor();
    }

    public int getTextColor() {
        return mTextPaint.getColor();
    }

    public int getArcColor() {
        return mArcPaint.getColor();
    }

    @Override
    public void draw(Canvas canvas) {
        mBounds.set(getBounds());

        if (mMode == MODE_RATING) {
            float arcAngle = mCurrentValue * 360f;
            final float arcStart = -90f;
            canvas.drawArc(mBounds, arcStart + arcAngle, 360f - arcAngle, true,
                    mBackgroundArcPaint);
            canvas.drawArc(mBounds, arcStart, arcAngle, true, mArcPaint);
        } else if (mMode == MODE_PROMPT) {
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(),
                    mBounds.height() * BACKGROUND_CIRCLE_RADIUS_RATIO, mBackgroundArcPaint);
        }

        canvas.drawCircle(mBounds.centerX(),
                mBounds.centerY(),
                mBounds.height() * FOREGROUND_CIRCLE_RADIUS_RATIO,
                mForegroundCirclePaint);

        if (mText != null) {
            canvas.drawText(mText,
                    mBounds.centerX() - (mTextWidth / 2f),
                    mBounds.centerY() + (mTextHeight / 2f),
                    mTextPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mArcPaint.setAlpha(alpha);
        mBackgroundArcPaint.setAlpha(alpha);
        mForegroundCirclePaint.setAlpha(alpha);
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mArcPaint.setColorFilter(colorFilter);
        mBackgroundArcPaint.setColorFilter(colorFilter);
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
                        ColorUtils.changeBrightness(mForegroundCircleColor, PRESSED_DARKEN_RATIO));
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

        mTextWidth = mTextPaint.measureText(mText);
        mTextHeight = mTextBounds.height();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public void showRating(int rating) {
        if (mMode == MODE_RATING && mUserRating == rating) {
            return;
        }

        if (isRunning()) {
            stop();
        }

        mUserRating = rating;
        mMode = MODE_RATING;

        mText = String.valueOf(rating);
        updateTextSize();
        mTargetValue = rating / 10f;
        invalidateSelf();

        if (shouldAnimate()) {
            mAnimator = createAnimator(mCurrentValue, mTargetValue);
            mAnimator.start();
        }
    }

    public void showPrompt(String promptText) {
        if (mMode == MODE_PROMPT) {
            return;
        }

        if (isRunning()) {
            stop();
        }

        mMode = MODE_PROMPT;
        mText = promptText;
        updateTextSize();
        invalidateSelf();
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

    private ValueAnimator createAnimator(float... values) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(1250);
        animator.setInterpolator(INTERPOLATOR);
        animator.setFloatValues(values);
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
