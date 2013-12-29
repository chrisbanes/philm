package app.philm.in.util;

public class TimeUtils {

    public static boolean isAfterThreshold(final long time, final long threshold) {
        return System.currentTimeMillis() - time > threshold;
    }

    public static boolean isBeforeThreshold(final long time, final long threshold) {
        return !isAfterThreshold(time, threshold);
    }

}
