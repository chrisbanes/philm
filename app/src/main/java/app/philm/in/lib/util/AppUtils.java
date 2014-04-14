package app.philm.in.lib.util;

import app.philm.in.BuildConfig;

public class AppUtils {

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
    }
}
