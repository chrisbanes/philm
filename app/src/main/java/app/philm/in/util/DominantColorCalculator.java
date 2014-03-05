package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.model.ColorScheme;

public class DominantColorCalculator {

    private static final String LOG_TAG = DominantColorCalculator.class.getSimpleName();

    private static final int NUM_COLORS = 10;

    private static final float PRIMARY_MIN_COLORFULNESS = 0.20f;
    private static final int PRIMARY_TEXT_MIN_CONTRAST = 135;

    private static final int SECONDARY_MIN_CONTRAST_PRIMARY = 40;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 20;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 140;

    private static final float COUNT_JITTER = 0.2f;

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

        findColors();
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
        // Iterate through the palette and return the first colour which meets the threshold
        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (ColorUtils.calculateColorfulness(color) >= PRIMARY_MIN_COLORFULNESS) {
                return color.getRgb();
            }
        }

        // We have no colour, so just return the most populous colour
        return mPalette[0].getRgb();
    }

    private int findSecondaryAccentColor(final int primary) {
        MedianCutQuantizer.ColorNode node = null;

        // Find the most frequent color which has sufficient contrast from the primary
        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (ColorUtils.calculateContrast(color, primary) >= SECONDARY_MIN_CONTRAST_PRIMARY) {
                node = color;
            }
        }

        if (node != null) {
            // Find colour with similar counts, but exclude the primary colour
            List<MedianCutQuantizer.ColorNode> similarCounts =
                    findSimilarCounts(node.getCount(), primary);

            // Now find the most colourful from those with similar counts
            node = findMostColorful(similarCounts);
        }

        if (node != null) {
            // If we have a colour, return it
            return node.getRgb();
        } else {
            // We couldn't find a colour. In that case use the primary colour, modifying
            // it's brightness
            return ColorUtils.changeBrightness(primary, 0.4f);
        }
    }

    private int findTertiaryAccentColor(final int primary, final int secondary) {
        List<MedianCutQuantizer.ColorNode> colours = new ArrayList<MedianCutQuantizer.ColorNode>();

        // Find all colors which has sufficient contrast from both the primary & secondary
        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (ColorUtils.calculateContrast(color, primary) >= TERTIARY_MIN_CONTRAST_PRIMARY
                    && ColorUtils.calculateContrast(color, secondary) >= TERTIARY_MIN_CONTRAST_SECONDARY) {
                colours.add(color);
            }
        }

        // Now find the most colourful from those found above
        final MedianCutQuantizer.ColorNode mostColorful = findMostColorful(colours);

        if (mostColorful != null) {
            // If we have a colour, return it
            return mostColorful.getRgb();
        } else {
            // We couldn't find a colour. In that case use the primary colour, modifying
            // it's brightness
            return ColorUtils.changeBrightness(secondary, 0.5f);
        }
    }

    private int findPrimaryTextColor(final int primary) {
        // Try and find a colour with sufficient contrast from the primary colour
        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (ColorUtils.calculateContrast(color, primary) >= PRIMARY_TEXT_MIN_CONTRAST) {
                return color.getRgb();
            }
        }

        // We haven't found a colour, so return black/white depending on the primary colour's
        // brightness
        return ColorUtils.calculateYiqLuma(primary) >= 128
                ? Color.BLACK
                : Color.WHITE;
    }


    private List<MedianCutQuantizer.ColorNode> findSimilarCounts(final int count, final int exclude) {
        final List<MedianCutQuantizer.ColorNode> nodes
                = new ArrayList<MedianCutQuantizer.ColorNode>();
        final int jitter = Math.round(count * COUNT_JITTER);

        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (Math.abs(color.getCount() - count) <= jitter && color.getRgb() != exclude) {
                nodes.add(color);
            }
        }

        return nodes;
    }

    private MedianCutQuantizer.ColorNode findMostColorful(
            List<MedianCutQuantizer.ColorNode> nodes) {
        MedianCutQuantizer.ColorNode max = null;

        for (MedianCutQuantizer.ColorNode color : nodes) {
            if (max == null || ColorUtils.calculateColorfulness(color)
                    > ColorUtils.calculateColorfulness(max)) {
                max = color;
            }
        }

        return max;
    }

}
