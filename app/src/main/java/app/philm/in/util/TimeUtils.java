package app.philm.in.util;

public class TimeUtils {

    public static boolean isAfterThreshold(final long time, final long threshold) {
        return isInFuture(time - threshold);
    }

    public static boolean isBeforeThreshold(final long time, final long threshold) {
        return isInPast(time - threshold);
    }

    public static boolean isInPast(final long time) {
        return time <= System.currentTimeMillis();
    }

    public static boolean isInFuture(final long time) {
        return time > System.currentTimeMillis();
    }

}
