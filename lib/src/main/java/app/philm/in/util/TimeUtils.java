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

public class TimeUtils {

    public static boolean isAfterThreshold(final long time, final long threshold) {
        return isInFuture(time - threshold);
    }

    public static boolean isBeforeThreshold(final long time, final long threshold) {
        return isInPast(time - threshold);
    }

    public static boolean isPastThreshold(final long time, final long threshold) {
        return isInPast(time + threshold);
    }

    public static boolean isInPast(final long time) {
        return time <= System.currentTimeMillis();
    }

    public static boolean isInFuture(final long time) {
        return time > System.currentTimeMillis();
    }

}
