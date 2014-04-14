package app.philm.in.lib.util;

import com.google.common.base.Preconditions;

public class IntUtils {

    public static int anchor(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int weightedAverage(int... values) {
        Preconditions.checkArgument(values.length % 2 == 0, "values must have a multiples of 2");

        int sum = 0;
        int sumWeight = 0;

        for (int i = 0; i < values.length; i += 2) {
            int value = values[i];
            int weight = values[i + 1];

            sum += (value * weight);
            sumWeight += weight;
        }

        return sum / sumWeight;
    }

    public static int[] toArray(int... array) {
        return array;
    }

}
