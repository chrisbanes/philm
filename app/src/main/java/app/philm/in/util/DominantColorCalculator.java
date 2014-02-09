package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

public class DominantColorCalculator {

    private static final int NUM_COLORS = 12;
    private static final float PRIMARY_MIN_SATURATION = 0.25f;
    private static final float PRIMARY_MIN_VALUE = 0.07f;
    private static final int PRIMARY_MIN_CONTRAST_DIFF = 135;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 25;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 70;

    private final MedianCutQuantizer.ColorNode[] mPalette;

    private MedianCutQuantizer.ColorNode mPrimaryAccentColor;
    private MedianCutQuantizer.ColorNode mSecondaryAccentColor;
    private MedianCutQuantizer.ColorNode mTertiaryAccentColor;

    private MedianCutQuantizer.ColorNode mPrimaryTextColorNode;
    private int mPrimaryTextColorInt;
    private int mSecondaryTextColorInt;

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

    public int getPrimaryAccentColor() {
        return mPrimaryAccentColor.getRgb();
    }

    public int getSecondaryAccentColor() {
        return mSecondaryAccentColor.getRgb();
    }

    public int getTertiaryAccentColor() {
        return mTertiaryAccentColor.getRgb();
    }

    public int getPrimaryTextColor() {
        if (mPrimaryTextColorNode != null) {
            return mPrimaryTextColorNode.getRgb();
        } else {
            return mPrimaryTextColorInt;
        }
    }

    public int getSecondaryTextColor() {
        return mSecondaryTextColorInt;
    }

    private void findColors() {
        mPrimaryAccentColor = findPrimaryAccentColor();
        mSecondaryAccentColor = findSecondaryAccentColor();
        mTertiaryAccentColor = findTertiaryAccentColor();

        mPrimaryTextColorNode = findPrimaryTextColor();
        if (mPrimaryTextColorNode == null) {
            mPrimaryTextColorInt = calculateYiqBrightness(mPrimaryAccentColor) >= 128
                    ? Color.BLACK
                    : Color.WHITE;
        }

        mSecondaryTextColorInt = calculateYiqBrightness(mPrimaryAccentColor) >= 128
                ? Color.BLACK
                : Color.WHITE;
    }

    private MedianCutQuantizer.ColorNode findPrimaryAccentColor() {
        return findAccentColor();
    }

    private MedianCutQuantizer.ColorNode findSecondaryAccentColor() {
        // We just return the most frequent color which isn't the primary accent
        for (int i = 0; i < mPalette.length; i++) {
            if (mPalette[i] != mPrimaryAccentColor) {
                return mPalette[i];
            }
        }
        return null;
    }

    private MedianCutQuantizer.ColorNode findTertiaryAccentColor() {

        ArrayList<MedianCutQuantizer.ColorNode> possibles = new ArrayList<MedianCutQuantizer.ColorNode>();

        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], mPrimaryAccentColor) >= TERTIARY_MIN_CONTRAST_PRIMARY &&
                    calculateContrast(mPalette[i], mSecondaryAccentColor) >= TERTIARY_MIN_CONTRAST_SECONDARY) {
                possibles.add(mPalette[i]);
            }
        }

        MedianCutQuantizer.ColorNode max = null;

        for (MedianCutQuantizer.ColorNode node : possibles) {
            if (max == null || node.getHsv()[1] > max.getHsv()[1]) {
                max = node;
            }
        }

        return max;
    }

    private final MedianCutQuantizer.ColorNode findPrimaryTextColor() {
        for (int i = 0; i < mPalette.length; i++) {
            if (calculateContrast(mPalette[i], mPrimaryAccentColor) >= PRIMARY_MIN_CONTRAST_DIFF) {
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

    private static final float calculateContrast(
            MedianCutQuantizer.ColorNode color1,
            MedianCutQuantizer.ColorNode color2) {
        return Math.abs(calculateYiqBrightness(color1) - calculateYiqBrightness(color2));
    }

    private static final int calculateYiqBrightness(MedianCutQuantizer.ColorNode colorNode) {
        final int color = colorNode.getRgb();
        return (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.green(color)) / 1000;
    }

}
