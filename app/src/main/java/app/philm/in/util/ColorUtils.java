package app.philm.in.util;

import android.graphics.Color;

public class ColorUtils {

    public static int darken(final int color, float fraction) {
        int red = Math.round(Color.red(color) * (1.0f - fraction));
        int green = Math.round(Color.green(color) * (1.0f - fraction));
        int blue = Math.round(Color.blue(color) * (1.0f - fraction));

        red = IntUtils.anchor(red, 0, 255);
        green = IntUtils.anchor(green, 0, 255);
        blue = IntUtils.anchor(blue, 0, 255);

        return Color.rgb(red, green, blue);
    }

}
