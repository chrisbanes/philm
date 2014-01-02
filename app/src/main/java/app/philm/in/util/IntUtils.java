package app.philm.in.util;

public class IntUtils {

    public static int anchor(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

}
