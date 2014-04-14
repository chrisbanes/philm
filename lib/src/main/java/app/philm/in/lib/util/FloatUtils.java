package app.philm.in.lib.util;

import com.google.common.base.Preconditions;

public class FloatUtils {

    public static float anchor(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float weightedAverage(float... values) {
        Preconditions.checkArgument(values.length % 2 == 0, "values must have a multiples of 2");

        float sum = 0;
        float sumWeight = 0;

        for (int i = 0; i < values.length; i += 2) {
            float value = values[i];
            float weight = values[i + 1];

            sum += (value * weight);
            sumWeight += weight;
        }

        return sum / sumWeight;
    }

}
