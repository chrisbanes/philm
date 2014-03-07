package app.philm.in.util;

/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) 2005-2008 Springer-Verlag Berlin,
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 *
 * Date: 2007/11/10
 */


import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import app.philm.in.Constants;

/*
 * This is an implementation of Heckbert's median-cut color quantization algorithm
 * (Heckbert P., "Color Image Quantization for Frame Buffer Display", ACM Transactions
 * on Computer Graphics (SIGGRAPH), pp. 297-307, 1982).
 * Unlike in the original algorithm, no initial uniform (scalar) quantization is used to
 * for reducing the number of image colors. Instead, all colors contained in the original
 * image are considered in the quantization process. After the set of representative
 * colors has been found, each image color is mapped to the closest representative
 * in RGB color space using the Euclidean distance.
 * The quantization process has two steps: first a ColorQuantizer object is created from
 * a given image using one of the constructor methods provided. Then this ColorQuantizer
 * can be used to quantize the original image or any other image using the same set of
 * representative colors (color table).
 */

public class MedianCutQuantizer {

    private static final String LOG_TAG = MedianCutQuantizer.class.getSimpleName();

    private ColorNode[] imageColors = null;    // original (unique) image colors
    private ColorNode[] quantColors = null;    // quantized colors

    public MedianCutQuantizer(int[] pixels, int Kmax) {
        quantColors = findRepresentativeColors(pixels, Kmax);

        if (Constants.DEBUG) {
            listColorNodes(quantColors);
        }
    }

    public int countQuantizedColors() {
        return quantColors.length;
    }

    public ColorNode[] getQuantizedColors() {
        return quantColors;
    }

    ColorNode[] findRepresentativeColors(int[] pixels, int Kmax) {
        ColorHistogram colorHist = new ColorHistogram(pixels);
        int K = colorHist.getNumberOfColors();
        ColorNode[] rCols = null;

        imageColors = new ColorNode[K];
        for (int i = 0; i < K; i++) {
            int rgb = colorHist.getColor(i);
            int cnt = colorHist.getCount(i);
            imageColors[i] = new ColorNode(rgb, cnt);
        }

        if (K <= Kmax) {
            // image has fewer colors than Kmax
            rCols = imageColors;
        } else {
            ColorBox initialBox = new ColorBox(0, K - 1, 0);
            List<ColorBox> colorSet = new ArrayList<ColorBox>();
            colorSet.add(initialBox);
            int k = 1;
            boolean done = false;
            while (k < Kmax && !done) {
                ColorBox nextBox = findBoxToSplit(colorSet);
                if (nextBox != null) {
                    ColorBox newBox = nextBox.splitBox();
                    colorSet.add(newBox);
                    k = k + 1;
                } else {
                    done = true;
                }
            }
            rCols = averageColors(colorSet);
        }
        return rCols;
    }

    public void quantizeImage(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            ColorNode color = findClosestColor(pixels[i]);
            pixels[i] = Color.rgb(color.red, color.grn, color.blu);
        }
    }

    ColorNode findClosestColor(int rgb) {
        int idx = findClosestColorIndex(rgb);
        return quantColors[idx];
    }

    int findClosestColorIndex(int rgb) {
        int red = Color.red(rgb);
        int grn = Color.green(rgb);
        int blu = Color.blue(rgb);
        int minIdx = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < quantColors.length; i++) {
            ColorNode color = quantColors[i];
            int d2 = color.distance2(red, grn, blu);
            if (d2 < minDistance) {
                minDistance = d2;
                minIdx = i;
            }
        }
        return minIdx;
    }

    private ColorBox findBoxToSplit(List<ColorBox> colorBoxes) {
        ColorBox boxToSplit = null;
        // from the set of splitable color boxes
        // select the one with the minimum level
        int minLevel = Integer.MAX_VALUE;
        for (ColorBox box : colorBoxes) {
            if (box.colorCount() >= 2) {    // box can be split
                if (box.level < minLevel) {
                    boxToSplit = box;
                    minLevel = box.level;
                }
            }
        }
        return boxToSplit;
    }

    private ColorNode[] averageColors(List<ColorBox> colorBoxes) {
        int n = colorBoxes.size();
        ColorNode[] avgColors = new ColorNode[n];
        int i = 0;
        for (ColorBox box : colorBoxes) {
            avgColors[i] = box.getAverageColor();
            i = i + 1;
        }
        return avgColors;
    }

    // -------------- class ColorNode -------------------------------------------

    public static class ColorNode {

        private final int red, grn, blu;
        private final int cnt;

        private float[] hsv;

        ColorNode(int rgb, int cnt) {
            this.red = Color.red(rgb);
            this.grn = Color.green(rgb);
            this.blu = Color.blue(rgb);
            this.cnt = cnt;
        }

        ColorNode(int red, int grn, int blu, int cnt) {
            this.red = red;
            this.grn = grn;
            this.blu = blu;
            this.cnt = cnt;
        }

        public int getRgb() {
            return Color.rgb(red, grn, blu);
        }

        public float[] getHsv() {
            if (hsv == null) {
                hsv = new float[3];
                Color.RGBToHSV(red, grn, blu, hsv);
            }
            return hsv;
        }

        public int getCount() {
            return cnt;
        }

        int distance2(int red, int grn, int blu) {
            // returns the squared distance between (red, grn, blu)
            // and this this color
            int dr = this.red - red;
            int dg = this.grn - grn;
            int db = this.blu - blu;
            return dr * dr + dg * dg + db * db;
        }

        public String toString() {
            return new StringBuilder(getClass().getSimpleName())
                    .append(" #").append(Integer.toHexString(getRgb()))
                    .append(". count: ").append(cnt).toString();
        }
    }

    // -------------- class ColorBox -------------------------------------------

    class ColorBox {

        int lower = 0;    // lower index into 'imageColors'
        int upper = -1; // upper index into 'imageColors'
        int level;        // split level o this color box
        int count = 0;    // number of pixels represented by thos color box
        int rmin, rmax;    // range of contained colors in red dimension
        int gmin, gmax;    // range of contained colors in green dimension
        int bmin, bmax;    // range of contained colors in blue dimension

        ColorBox(int lower, int upper, int level) {
            this.lower = lower;
            this.upper = upper;
            this.level = level;
            this.trim();
        }

        int colorCount() {
            return upper - lower;
        }

        void trim() {
            // recompute the boundaries of this color box
            rmin = 255;
            rmax = 0;
            gmin = 255;
            gmax = 0;
            bmin = 255;
            bmax = 0;
            count = 0;
            for (int i = lower; i <= upper; i++) {
                ColorNode color = imageColors[i];
                count = count + color.cnt;
                int r = color.red;
                int g = color.grn;
                int b = color.blu;
                if (r > rmax) {
                    rmax = r;
                }
                if (r < rmin) {
                    rmin = r;
                }
                if (g > gmax) {
                    gmax = g;
                }
                if (g < gmin) {
                    gmin = g;
                }
                if (b > bmax) {
                    bmax = b;
                }
                if (b < bmin) {
                    bmin = b;
                }
            }
        }

        // Split this color box at the median point along its
        // longest color dimension
        ColorBox splitBox() {
            if (this.colorCount() < 2)    // this box cannot be split
            {
                return null;
            } else {
                // find longest dimension of this box:
                ColorDimension dim = getLongestColorDimension();

                // find median along dim
                int med = findMedian(dim);

                // now split this box at the median return the resulting new
                // box.
                int nextLevel = level + 1;
                ColorBox newBox = new ColorBox(med + 1, upper, nextLevel);
                this.upper = med;
                this.level = nextLevel;
                this.trim();
                return newBox;
            }
        }

        // Find longest dimension of this color box (RED, GREEN, or BLUE)
        ColorDimension getLongestColorDimension() {
            int rLength = rmax - rmin;
            int gLength = gmax - gmin;
            int bLength = bmax - bmin;
            if (bLength >= rLength && bLength >= gLength) {
                return ColorDimension.BLUE;
            } else if (gLength >= rLength && gLength >= bLength) {
                return ColorDimension.GREEN;
            } else {
                return ColorDimension.RED;
            }
        }

        // Find the position of the median in RGB space along
        // the red, green or blue dimension, respectively.
        int findMedian(ColorDimension dim) {
            // sort color in this box along dimension dim:
            Arrays.sort(imageColors, lower, upper + 1, dim.comparator);
            // find the median point:
            int half = count / 2;
            int nPixels, median;
            for (median = lower, nPixels = 0; median < upper; median++) {
                nPixels = nPixels + imageColors[median].cnt;
                if (nPixels >= half) {
                    break;
                }
            }
            return median;
        }

        ColorNode getAverageColor() {
            int rSum = 0;
            int gSum = 0;
            int bSum = 0;
            int n = 0;
            for (int i = lower; i <= upper; i++) {
                ColorNode ci = imageColors[i];
                int cnt = ci.cnt;
                rSum = rSum + cnt * ci.red;
                gSum = gSum + cnt * ci.grn;
                bSum = bSum + cnt * ci.blu;
                n = n + cnt;
            }
            double nd = n;
            int avgRed = (int) (0.5 + rSum / nd);
            int avgGrn = (int) (0.5 + gSum / nd);
            int avgBlu = (int) (0.5 + bSum / nd);
            return new ColorNode(avgRed, avgGrn, avgBlu, n);
        }

        public String toString() {
            String s = this.getClass().getSimpleName();
            s = s + " lower=" + lower + " upper=" + upper;
            s = s + " count=" + count + " level=" + level;
            s = s + " rmin=" + rmin + " rmax=" + rmax;
            s = s + " gmin=" + gmin + " gmax=" + gmax;
            s = s + " bmin=" + bmin + " bmax=" + bmax;
            s = s + " bmin=" + bmin + " bmax=" + bmax;
            return s;
        }
    }

    //	 ---  color dimensions ------------------------

    // The main purpose of this enumeration class is associate
    // the color dimensions with the corresponding comparators.
    enum ColorDimension {
        RED(new redComparator()),
        GREEN(new grnComparator()),
        BLUE(new bluComparator());

        public final Comparator<ColorNode> comparator;

        ColorDimension(Comparator<ColorNode> cmp) {
            this.comparator = cmp;
        }
    }

    // --- color comparators used for sorting colors along different dimensions ---

    static class redComparator implements Comparator<ColorNode> {
        public int compare(ColorNode colA, ColorNode colB) {
            return colA.red - colB.red;
        }
    }

    static class grnComparator implements Comparator<ColorNode> {
        public int compare(ColorNode colA, ColorNode colB) {
            return colA.grn - colB.grn;
        }
    }

    static class bluComparator implements Comparator<ColorNode> {
        public int compare(ColorNode colA, ColorNode colB) {
            return colA.blu - colB.blu;
        }
    }

    //-------- utility methods -----------

    void listColorNodes(ColorNode[] nodes){
        int i = 0;
        for (ColorNode color : nodes) {
            Log.d(LOG_TAG, "Color Node #" + i + " " + color.toString());
            i++;
        }
    }

    static class ColorHistogram {

        int colorArray[] = null;
        int countArray[] = null;

        ColorHistogram(int[] color, int[] count) {
            this.countArray = count;
            this.colorArray = color;
        }

        ColorHistogram(int[] pixelsOrig) {
            int N = pixelsOrig.length;
            int[] pixelsCpy = new int[N];
            for (int i = 0; i < N; i++) {
                // remove possible alpha components
                pixelsCpy[i] = 0xFFFFFF & pixelsOrig[i];
            }
            Arrays.sort(pixelsCpy);

            // count unique colors:
            int k = -1; // current color index
            int curColor = -1;
            for (int i = 0; i < pixelsCpy.length; i++) {
                if (pixelsCpy[i] != curColor) {
                    k++;
                    curColor = pixelsCpy[i];
                }
            }
            int nColors = k + 1;

            // tabulate and count unique colors:
            colorArray = new int[nColors];
            countArray = new int[nColors];
            k = -1;    // current color index
            curColor = -1;
            for (int i = 0; i < pixelsCpy.length; i++) {
                if (pixelsCpy[i] != curColor) {    // new color
                    k++;
                    curColor = pixelsCpy[i];
                    colorArray[k] = curColor;
                    countArray[k] = 1;
                } else {
                    countArray[k]++;
                }
            }
        }

        public int[] getColorArray() {
            return colorArray;
        }

        public int[] getCountArray() {
            return countArray;
        }

        public int getNumberOfColors() {
            if (colorArray == null) {
                return 0;
            } else {
                return colorArray.length;
            }
        }

        public int getColor(int index) {
            return this.colorArray[index];
        }

        public int getCount(int index) {
            return this.countArray[index];
        }
    }


} //class MedianCut
