/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.util;

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
