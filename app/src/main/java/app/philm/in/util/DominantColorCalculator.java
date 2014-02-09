package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

public class DominantColorCalculator {

    private static final int NUM_COLORS = 10;

    private static final float PRIMARY_MIN_SATURATION = 0.35f;
    private static final float PRIMARY_MIN_VALUE = 0.07f;
    private static final int PRIMARY_MIN_CONTRAST_DIFF = 135;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 25;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 70;

    private final MedianCutQuantizer.ColorNode[] mPalette;

    private int mPrimaryAccentColor;
    private int mSecondaryAccentColor;
    private int mTertiaryAccentColor;

    private int mPrimaryTextColor;
    private int mSecondaryTextColor;

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

    public int getPrimaryAccentColor() {
        return mPrimaryAccentColor;
    }

    public int getSecondaryAccentColor() {
        return mSecondaryAccentColor;
    }

    public int getTertiaryAccentColor() {
        return mTertiaryAccentColor;
    }

    public int getPrimaryTextColor() {
        return mPrimaryTextColor;
    }

    public int getSecondaryTextColor() {
        return mSecondaryTextColor;
    }

    private void findColors() {
        MedianCutQuantizer.ColorNode primaryNode = findPrimaryAccentColor();
        mPrimaryAccentColor = primaryNode.getRgb();

        MedianCutQuantizer.ColorNode secondaryNode = findSecondaryAccentColor(primaryNode);
        mSecondaryAccentColor = secondaryNode.getRgb();

        MedianCutQuantizer.ColorNode tertiaryNode = findTertiaryAccentColor(primaryNode, secondaryNode);
        if (tertiaryNode != null) {
            mTertiaryAccentColor = tertiaryNode.getRgb();
        } else {
            mTertiaryAccentColor = ColorUtils.calculateYiqLuma(mSecondaryAccentColor) >= 128
                    ? ColorUtils.darken(mSecondaryAccentColor, 0.3f)
                    : ColorUtils.lighten(mSecondaryAccentColor, 0.3f);
        }

        MedianCutQuantizer.ColorNode primaryTextNode = findPrimaryTextColor(primaryNode);
        if (primaryTextNode != null) {
            mPrimaryTextColor = primaryTextNode.getRgb();
        } else {
            mPrimaryTextColor = ColorUtils.calculateYiqLuma(mPrimaryAccentColor) >= 128
                    ? Color.BLACK
                    : Color.WHITE;
        }

        mSecondaryTextColor = ColorUtils.calculateYiqLuma(mPrimaryAccentColor) >= 128
                ? Color.BLACK
                : Color.WHITE;
    }

    private MedianCutQuantizer.ColorNode findPrimaryAccentColor() {
        return findAccentColor();
    }

    private MedianCutQuantizer.ColorNode findSecondaryAccentColor(
            MedianCutQuantizer.ColorNode primary) {
        // We just return the most frequent color which isn't the primary accent
        for (int i = 0; i < mPalette.length; i++) {
            if (mPalette[i] != primary) {
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
            if (calculateContrast(mPalette[i], primary) >= PRIMARY_MIN_CONTRAST_DIFF) {
                return mPalette[i];
            }
        }
        return null;
    }

    private MedianCutQuantizer.ColorNode findAccentColor(MedianCutQuantizer.ColorNode... ignored) {
        for (int i = 0; i < mPalette.length; i++) {
            MedianCutQuantizer.ColorNode iNode = mPalette[i];
            if (!contains(ignored, iNode)) {
                final float[] hsv = iNode.getHsv();
                if (hsv[1] >= PRIMARY_MIN_SATURATION && hsv[2] >= PRIMARY_MIN_VALUE) {
                    return iNode;
                }
            }
        }
        return null;
    }

    private static boolean contains(MedianCutQuantizer.ColorNode[] array,
            MedianCutQuantizer.ColorNode searchFor) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == searchFor) {
                return true;
            }
        }
        return false;
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
