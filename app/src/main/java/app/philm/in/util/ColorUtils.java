package app.philm.in.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseIntArray;

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

    public static int[] findDominateColors(Bitmap bitmap, int detectColors) {
        Log.d("ColorUtils", "findDominateColors. Bitmap: " + bitmap.getWidth()
                + "x" + bitmap.getHeight());

        final int pixelCount = bitmap.getWidth() * bitmap.getHeight();

        final int[] rgbPixels = new int[pixelCount];
        bitmap.getPixels(rgbPixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        Log.d("ColorUtils", "Unpacked RGB Pixels");

        final byte[] originalBgrPixels = new byte[pixelCount * 3];
        for (int i = 0; i < rgbPixels.length; i++) {
            originalBgrPixels[i++] = (byte) Color.blue(rgbPixels[i]);
            originalBgrPixels[i++] = (byte) Color.green(rgbPixels[i]);
            originalBgrPixels[i++] = (byte) Color.red(rgbPixels[i]);
        }

        Log.d("ColorUtils", "Converted pixels to BGR");

        final NeuQuant nq = new NeuQuant(originalBgrPixels, originalBgrPixels.length, 20);
        final byte[] colorMap = nq.process();

        Log.d("ColorUtils", "Quantized color map");

        final SparseIntArray sparseIntArray = new SparseIntArray(pixelCount);

        // map image rgbPixels to new palette
        for (int i = 0; i < pixelCount; i++) {
            final int index = nq.map(
                    originalBgrPixels[i++] & 0xff,
                    originalBgrPixels[i++] & 0xff,
                    originalBgrPixels[i++] & 0xff
            );

            final int rgbColor = bgrToRgb(colorMap[index]);
            sparseIntArray.put(rgbColor, sparseIntArray.get(rgbColor) + 1);
        }

        Log.d("ColorUtils", "Mapped quantized rgbPixels");

        final int[] dominantColors = new int[detectColors];
        for (int i = 0; i < detectColors ; i++) {
            final int maxIndex = maxValueIndex(sparseIntArray);
            dominantColors[i] = sparseIntArray.valueAt(maxIndex);

            // Now remove the current max
            sparseIntArray.removeAt(maxIndex);
        }

        return dominantColors;
    }

    private static int maxValueIndex(SparseIntArray array) {
        int maxIndex = -1;
        int max = -1;

        for (int i = 0; i < array.size(); i++) {
            if (array.valueAt(i) > max) {
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    private static int bgrToRgb(int color) {
        return Color.rgb(Color.blue(color), Color.green(color), Color.red(color));
    }

}
