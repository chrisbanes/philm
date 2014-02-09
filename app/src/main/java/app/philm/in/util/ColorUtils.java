package app.philm.in.util;

import android.graphics.Color;

public class ColorUtils {

    public static int darken(final int color, float fraction) {
        return multiplyColor(color, 1f - fraction);
    }

    public static int lighten(final int color, float fraction) {
        return multiplyColor(color, fraction);
    }

    private static int multiplyColor(final int color, float fraction) {
        int red = Math.round(Color.red(color) * fraction);
        int green = Math.round(Color.green(color) * fraction);
        int blue = Math.round(Color.blue(color) * fraction);

        red = IntUtils.anchor(red, 0, 255);
        green = IntUtils.anchor(green, 0, 255);
        blue = IntUtils.anchor(blue, 0, 255);

        return Color.rgb(red, green, blue);
    }

    /**
     * @return luma value according to to YIQ color space.
     */
    public static final int calculateYiqLuma(int color) {
        return (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.green(color)) / 1000;
    }

}
