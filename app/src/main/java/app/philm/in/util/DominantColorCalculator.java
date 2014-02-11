package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

import app.philm.in.model.ColorScheme;

public class DominantColorCalculator {

    private static final String LOG_TAG = DominantColorCalculator.class.getSimpleName();

    private static final int NUM_COLORS = 8;

    private static final float PRIMARY_MIN_COLORFULNESS = 0.20f;
    private static final int PRIMARY_TEXT_MIN_CONTRAST = 135;

    private static final int SECONDARY_MIN_CONTRAST_PRIMARY = 40;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 35;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 140;

    private final MedianCutQuantizer.ColorNode[] mPalette;

    private ColorScheme mColorScheme;

    public DominantColorCalculator(Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "bitmap cannot be null");

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[] rgbPixels = new int[width * height];
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);

        final MedianCutQuantizer mcq = new MedianCutQuantizer(rgbPixels, NUM_COLORS);
        mPalette = mcq.getSortedQuantizedColors();

        try {
            findColors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ColorScheme getColorScheme() {
        return mColorScheme;
    }

    private void findColors() {
        final int primaryAccentColor = findPrimaryAccentColor();
        final int secondaryAccentColor = findSecondaryAccentColor(primaryAccentColor);
        final int tertiaryAccentColor = findTertiaryAccentColor(
                primaryAccentColor, secondaryAccentColor);
        final int primaryTextColor = findPrimaryTextColor(primaryAccentColor);

        final int secondaryTextColor = ColorUtils.calculateYiqLuma(primaryAccentColor) >= 128
                ? Color.BLACK
                : Color.WHITE;

        mColorScheme = new ColorScheme(primaryAccentColor, secondaryAccentColor,
                tertiaryAccentColor, primaryTextColor, secondaryTextColor);
    }

    private int findPrimaryAccentColor() {
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateColorfulness(mPalette[i]) >= PRIMARY_MIN_COLORFULNESS) {
                return mPalette[i].getRgb();
            }
        }
        return mPalette[0].getRgb();
    }

    private int findSecondaryAccentColor(final int primary) {
        // We just return the most frequent color which isn't the primary accent
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], primary) >= SECONDARY_MIN_CONTRAST_PRIMARY) {
                return mPalette[i].getRgb();
            }
        }

        Log.d(LOG_TAG, "Calculating secondary accent from: #" + Integer.toHexString(primary));

        return ColorUtils.changeBrightness(primary, 0.4f);
    }

    private int findTertiaryAccentColor(final int primary, final int secondary) {
        ArrayList<MedianCutQuantizer.ColorNode> possibles
                = new ArrayList<MedianCutQuantizer.ColorNode>();

        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], primary) >= TERTIARY_MIN_CONTRAST_PRIMARY &&
                    calculateContrast(mPalette[i], secondary) >= TERTIARY_MIN_CONTRAST_SECONDARY) {
                possibles.add(mPalette[i]);
            }
        }

        MedianCutQuantizer.ColorNode max = null;

        if (!PhilmCollections.isEmpty(possibles)) {
            for (MedianCutQuantizer.ColorNode node : possibles) {
                if (max == null || calculateColorfulness(node) > calculateColorfulness(max)) {
                    max = node;
                }
            }
        }

        if (max != null) {
            return max.getRgb();
        }

        Log.d(LOG_TAG, "Calculating tertiary accent from: #" + Integer.toHexString(secondary));

        return ColorUtils.changeBrightness(secondary, 0.5f);
    }

    private final int findPrimaryTextColor(final int primary) {
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], primary) >= PRIMARY_TEXT_MIN_CONTRAST) {
                return mPalette[i].getRgb();
            }
        }
        return ColorUtils.calculateYiqLuma(primary) >= 128
                ? Color.BLACK
                : Color.WHITE;
    }

    private static final int calculateContrast(MedianCutQuantizer.ColorNode color1, int color2) {
        return Math.abs(ColorUtils.calculateYiqLuma(color1.getRgb())
                - ColorUtils.calculateYiqLuma(color2));
    }

    private static final float calculateColorfulness(MedianCutQuantizer.ColorNode node) {
        float[] hsv = node.getHsv();
        return hsv[1] * hsv[2];
    }

}
