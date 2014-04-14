package app.philm.in.lib.util;

import com.google.common.base.Preconditions;

import android.animation.ValueAnimator;
import android.view.View;

public class ColorValueAnimator {

    public interface OnColorSetListener {
        public void onUpdateColor(int[] newColors);
    }

    public static ValueAnimator start(
            final View rootView,
            final int[] current,
            final int[] target,
            final int duration,
            final OnColorSetListener listener) {

        Preconditions.checkNotNull(rootView, "rootView cannot be null");
        Preconditions.checkNotNull(current, "current cannot be null");
        Preconditions.checkNotNull(target, "target cannot be null");
        Preconditions.checkArgument(current.length == target.length,
                "current and target must be the same length");

        if (rootView.getDrawingTime() <= 0 ) {
            listener.onUpdateColor(target);
            return null;
        }

        final int[] colors = new int[target.length];

        ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(0f, 1f);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float currentValue = 1f - (Float) valueAnimator.getAnimatedValue();

                for (int i = 0, z = colors.length; i < z ; i++) {
                    colors[i] = ColorUtils.blendColors(
                            current[i],
                            target[i],
                            currentValue);
                }

                listener.onUpdateColor(colors);
            }
        });
        animator.start();

        return animator;
    }

}
