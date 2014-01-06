package app.philm.in.trakt;

import com.google.common.base.Preconditions;

import android.content.res.Resources;
import android.text.TextUtils;

import app.philm.in.R;
import app.philm.in.model.PhilmMovie;

public class TraktImageHelper {

    public static final int TYPE_SMALL = 0;
    public static final int TYPE_LARGE = 1;
    public static final int TYPE_UNCOMPRESSED = 2;

    private static final String POSTER_SMALL_SUFFIX = "-138";
    private static final String POSTER_LARGE_SUFFIX = "-300";

    private static final String FANART_SMALL_SUFFIX = "-218";
    private static final String FANART_LARGE_SUFFIX = "-940";

    private final Resources mResources;

    public TraktImageHelper(Resources resources) {
        mResources = Preconditions.checkNotNull(resources, "resources cannot be null");
    }

    public String getPosterUrl(final PhilmMovie movie) {
        return getPosterUrl(movie, mResources.getInteger(R.integer.trakt_poster_image_size));
    }

    public String getPosterUrl(final PhilmMovie movie, final int type) {
        final String rawUrl = movie.getPosterUrl();
        switch (type) {
            case TYPE_LARGE:
                return modifyUrl(rawUrl, POSTER_LARGE_SUFFIX);
            case TYPE_SMALL:
                return modifyUrl(rawUrl, POSTER_SMALL_SUFFIX);
            case TYPE_UNCOMPRESSED:
            default:
                return rawUrl;
        }
    }

    public String getFanartUrl(final PhilmMovie movie) {
        return getFanartUrl(movie, mResources.getInteger(R.integer.trakt_fanart_image_size));
    }

    public String getFanartUrl(final PhilmMovie movie, final int type) {
        final String rawUrl = movie.getFanartUrl();
        switch (type) {
            case TYPE_LARGE:
                return modifyUrl(rawUrl, FANART_LARGE_SUFFIX);
            case TYPE_SMALL:
                return modifyUrl(rawUrl, FANART_SMALL_SUFFIX);
            case TYPE_UNCOMPRESSED:
            default:
                return rawUrl;
        }
    }

    private static String modifyUrl(final String originalUrl, final String suffix) {
        if (!TextUtils.isEmpty(originalUrl)) {
            final int lastDot = originalUrl.lastIndexOf('.');
            if (lastDot != 0) {
                StringBuilder url = new StringBuilder(originalUrl.substring(0, lastDot));
                url.append(suffix);
                url.append(originalUrl.substring(lastDot));
                return url.toString();
            }
        }
        return originalUrl;
    }

}
