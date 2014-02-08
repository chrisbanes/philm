package app.philm.in.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.Arrays;
import java.util.Comparator;

import app.philm.in.Constants;

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

    public static int[] findDominateColors(Bitmap bitmap, int numColors) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[] rgbPixels = new int[width * height];
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);

        final MedianCutQuantizer mcq = new MedianCutQuantizer(rgbPixels, numColors);

        final MedianCutQuantizer.ColorNode[] colorNodes = mcq.getSortedQuantizedColors();

        final int colors[] = new int[colorNodes.length];
        for (int i = 0; i < colorNodes.length ; i++) {
            colors[i] = colorNodes[i].getRgb();
        }

        return colors;
    }

    public static final int calculateContrast(int color1, int color2) {
        return Math.abs(calculateBrightness(color1) - calculateBrightness(color2));
    }

    private static final int calculateBrightness(int color) {
        return (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.green(color)) / 1000;
    }

    public static final int findNextVisibleColor(int[] colors, int color1) {

        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != color1) {
                if (calculateContrast(colors[i], color1) > 50) {
                    return colors[i];
                }
            }
        }

        return calculateBrightness(color1) > 128 ? Color.BLACK : Color.WHITE;

    }

}
