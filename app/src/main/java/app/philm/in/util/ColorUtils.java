package app.philm.in.util;

import android.graphics.Color;

public class ColorUtils {

    public static int darken(final int color, float fraction) {
        return blendColors(Color.BLACK, color, fraction);
    }

    public static int lighten(final int color, float fraction) {
        return blendColors(Color.WHITE, color, fraction);
    }

    /**
     * @return luma value according to to YIQ color space.
     */
    public static final int calculateYiqLuma(int color) {
        return Math.round((299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.green(color)) / 1000f);
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    public static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRatio);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRatio);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRatio);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    public static final int changeBrightness(final int color, float fraction) {
        return calculateYiqLuma(color) >= 128
                ? darken(color, fraction)
                : lighten(color, fraction);
    }

}
