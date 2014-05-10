package app.philm.in.model;

import app.philm.in.util.TextUtils;

public abstract class PhilmModel {

    public static final int TYPE_TMDB = 1;
    public static final int TYPE_IMDB = 2;
    public static final int TYPE_TRAKT = 3;

    static String select(String tmdbVersion, String traktVersion) {
        if (!TextUtils.isEmpty(tmdbVersion)) {
            return tmdbVersion;
        }
        return traktVersion;
    }

    static int select(int tmdbVersion, int traktVersion) {
        if (tmdbVersion > 0) {
            return tmdbVersion;
        }
        return traktVersion;
    }

    static long select(long tmdbVersion, long traktVersion) {
        if (tmdbVersion > 0) {
            return tmdbVersion;
        }
        return traktVersion;
    }

}
