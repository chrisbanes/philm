package app.philm.in;

import android.text.format.DateUtils;

public class Constants {

    public static final String TRAKT_API_KEY = "8221f3109b8d71c2fed89dee7bad5850";

    public static final String TRAKT_ACCOUNT_TYPE = "app.philm.in.account";
    public static final String TRAKT_AUTHTOKEN_PASSWORD_TYPE = "password";

    public static final long FUTURE_SOON_THRESHOLD = 30 * DateUtils.DAY_IN_MILLIS;

    public static final long STALE_MOVIE_DETAIL_THRESHOLD = 2 * DateUtils.DAY_IN_MILLIS;

    public static final int FILTER_HIGHLY_RATED = 70;

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean DEBUG_NETWORK = DEBUG;

}
