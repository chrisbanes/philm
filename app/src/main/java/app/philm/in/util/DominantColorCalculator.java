package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

import app.philm.in.model.ColorScheme;

public class DominantColorCalculator {

    private static final int NUM_COLORS = 10;

    private static final float PRIMARY_MIN_SATURATION = 0.35f;
    private static final float PRIMARY_MIN_VALUE = 0.07f;
    private static final int PRIMARY_TEXT_MIN_CONTRAST = 135;

    private static final int SECONDARY_MIN_CONTRAST_PRIMARY = 40;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 25;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 70;

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
        MedianCutQuantizer.ColorNode primaryNode = findPrimaryAccentColor();
        int primaryAccentColor = primaryNode.getRgb();

        MedianCutQuantizer.ColorNode secondaryNode = findSecondaryAccentColor(primaryNode);
        int secondaryAccentColor;
        if (secondaryNode != null) {
            secondaryAccentColor = secondaryNode.getRgb();
        }

        MedianCutQuantizer.ColorNode tertiaryNode = findTertiaryAccentColor(primaryNode, secondaryNode);
        int tertiaryAccentColor;
        if (tertiaryNode != null) {
            tertiaryAccentColor = tertiaryNode.getRgb();
        } else {
            tertiaryAccentColor = ColorUtils.calculateYiqLuma(secondaryAccentColor) >= 128
                    ? ColorUtils.darken(secondaryAccentColor, 0.3f)
                    : ColorUtils.lighten(secondaryAccentColor, 0.3f);
        }

        MedianCutQuantizer.ColorNode primaryTextNode = findPrimaryTextColor(primaryNode);
        int primaryTextColor;
        if (primaryTextNode != null) {
            primaryTextColor = primaryTextNode.getRgb();
        } else {
            primaryTextColor = ColorUtils.calculateYiqLuma(primaryAccentColor) >= 128
                    ? Color.BLACK
                    : Color.WHITE;
        }

        int secondaryTextColor = ColorUtils.calculateYiqLuma(primaryAccentColor) >= 128
                ? Color.BLACK
                : Color.WHITE;

        mColorScheme = new ColorScheme(primaryAccentColor, secondaryAccentColor,
                tertiaryAccentColor, primaryTextColor, secondaryTextColor);
    }

    private MedianCutQuantizer.ColorNode findPrimaryAccentColor() {
        for (int i = 0; i < mPalette.length; i++) {
            MedianCutQuantizer.ColorNode iNode = mPalette[i];
                final float[] hsv = iNode.getHsv();
                if (hsv[1] >= PRIMARY_MIN_SATURATION && hsv[2] >= PRIMARY_MIN_VALUE) {
                    return iNode;
                }
        }
        return mPalette[0];
    }

    private MedianCutQuantizer.ColorNode findSecondaryAccentColor(
            MedianCutQuantizer.ColorNode primary) {
        // We just return the most frequent color which isn't the primary accent
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], primary) >= SECONDARY_MIN_CONTRAST_PRIMARY) {
                return mPalette[i];
            }
        }
        return null;
    }

    private MedianCutQuantizer.ColorNode findTertiaryAccentColor(
            MedianCutQuantizer.ColorNode primary,
            MedianCutQuantizer.ColorNode secondary) {

        ArrayList<MedianCutQuantizer.ColorNode> possibles = new ArrayList<MedianCutQuantizer.ColorNode>();

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

        return max;
    }

    private final MedianCutQuantizer.ColorNode findPrimaryTextColor(
            MedianCutQuantizer.ColorNode primary) {
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], primary) >= PRIMARY_TEXT_MIN_CONTRAST) {
                return mPalette[i];
            }
        }
        return null;
    }

    private static final int calculateContrast(MedianCutQuantizer.ColorNode color1,
            MedianCutQuantizer.ColorNode color2) {
        return Math.abs(ColorUtils.calculateYiqLuma(color1.getRgb())
                - ColorUtils.calculateYiqLuma(color2.getRgb()));
    }

    private static final float calculateColorfulness(MedianCutQuantizer.ColorNode node) {
        float[] hsv = node.getHsv();
        return hsv[1] * hsv[2];
    }

}
