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

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

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

    private static final int MAX_ITERATIONS = 1000;
    private static final float FRACTION_BY_POPULATION = 0.5f;

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

    public ColorNode[] getSortedQuantizedColors() {
        ColorNode[] sortedNodes = Arrays.copyOf(quantColors, quantColors.length);

        Arrays.sort(sortedNodes, new Comparator<MedianCutQuantizer.ColorNode>() {
            @Override
            public int compare(MedianCutQuantizer.ColorNode colorNode,
                    MedianCutQuantizer.ColorNode colorNode2) {
                return colorNode2.getCount() - colorNode.getCount();
            }
        });

        listColorNodes(sortedNodes);

        return sortedNodes;
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
            final int populationMax = Math.round(Kmax * FRACTION_BY_POPULATION);

            ColorBox initialBox = new ColorBox(0, K - 1);
            PriorityQueue<ColorBox> pq1 = new PriorityQueue<ColorBox>(11,
                    new Comparator<ColorBox>() {
                        @Override
                        public int compare(ColorBox lhs, ColorBox rhs) {
                            return rhs.colorCount() - lhs.colorCount();
                        }
                    });

            pq1.offer(initialBox);
            iterateBoxs(pq1, populationMax);

            PriorityQueue<ColorBox> pq2 = new PriorityQueue<ColorBox>(11,
                    new Comparator<ColorBox>() {
                        @Override
                        public int compare(ColorBox lhs, ColorBox rhs) {
                            return (rhs.colorCount() * rhs.volume()) - (lhs.colorCount() * lhs.volume());
                        }
                    });

            while (pq1.peek() != null) {
                pq2.offer(pq1.poll());
            }
            iterateBoxs(pq2, Kmax - pq2.size());

            rCols = averageColors(pq2);
        }
        return rCols;
    }

    private void iterateBoxs(PriorityQueue<ColorBox> boxes, int targetColors) {
        int nColors = 1;
        int nIters = 0;

        while (nIters < MAX_ITERATIONS) {
            ColorBox box = boxes.poll();
            if (box.colorCount() == 0) {
                boxes.offer(box);
                nIters++;
                continue;
            }

            ColorBox box2 = box.splitBox();

            // Lets re-offer the box
            boxes.offer(box);

            // If we have a second box, offer it too
            if (box2 != null) {
                boxes.offer(box2);
                nColors++;
            }

            if (nColors >= targetColors) {
                return;
            }

            nIters++;
        }
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

    private ColorNode[] averageColors(PriorityQueue<ColorBox> colorBoxes) {
        int n = colorBoxes.size();
        ColorNode[] avgColors = new ColorNode[n];

        int i = 0;
        while (colorBoxes.peek() != null) {
            avgColors[i++] = colorBoxes.poll().getAverageColor();
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
        int count = 0;    // number of pixels represented by thos color box
        int volume = 0;

        int rmin, rmax;    // range of contained colors in red dimension
        int gmin, gmax;    // range of contained colors in green dimension
        int bmin, bmax;    // range of contained colors in blue dimension

        ColorBox(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
            this.trim();
        }

        int colorCount() {
            return upper - lower;
        }

        int volume() {
            return ((rmax - rmin + 1) * (gmax - gmin + 1) * (bmax - bmin + 1));
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
            volume = ((rmax - rmin + 1) * (gmax - gmin + 1) * (bmax - bmin + 1));
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
                ColorBox newBox = new ColorBox(med + 1, upper);
                this.upper = med;
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
            s = s + " count=" + count;
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
