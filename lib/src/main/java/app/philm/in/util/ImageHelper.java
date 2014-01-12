package app.philm.in.util;

import com.google.common.base.Preconditions;

import app.philm.in.model.PhilmMovie;

public class ImageHelper {

    private static final int[] TRAKT_POSTER_SIZES = { 138, 300 };
    private static final int[] TRAKT_FANART_SIZES = { 218, 940 };

    private String mTmdbBaseUrl;
    private int[] mTmdbPosterSizes;
    private int[] mTmdbFanartSizes;

    public void setTmdbFanartSizes(int[] tmdbFanartSizes) {
        mTmdbFanartSizes = tmdbFanartSizes;
    }

    public void setTmdbPosterSizes(int[] tmdbPosterSizes) {
        mTmdbPosterSizes = tmdbPosterSizes;
    }

    public void setTmdbBaseUrl(String baseUrl) {
        mTmdbBaseUrl = baseUrl;
    }

    public String getPosterUrl(final PhilmMovie movie, final int width) {
        final String posterUrl = movie.getPosterUrl();
        Preconditions.checkNotNull(posterUrl, "movie must have poster url");

        switch (movie.getPosterType()) {
            case PhilmMovie.TYPE_TMDB:
                return buildTmdbUrl(mTmdbBaseUrl, posterUrl, selectSize(width, mTmdbPosterSizes));
            default:
            case PhilmMovie.TYPE_TRAKT:
                return buildTraktUrl(posterUrl, selectSize(width, TRAKT_POSTER_SIZES));
        }
    }

    public String getFanartUrl(final PhilmMovie movie, final int width) {
        final String fanartUrl = movie.getFanartUrl();
        Preconditions.checkNotNull(fanartUrl, "movie must have backdrop url");

        switch (movie.getFanartType()) {
            case PhilmMovie.TYPE_TMDB:
                return buildTmdbUrl(mTmdbBaseUrl, fanartUrl, selectSize(width, mTmdbFanartSizes));
            default:
            case PhilmMovie.TYPE_TRAKT:
                return buildTraktUrl(fanartUrl, selectSize(width, TRAKT_FANART_SIZES));
        }
    }

    private static int selectSize(final int width, final int[] widths) {
        int previousBucketWidth = 0;

        for (int i = 0; i < widths.length; i++) {
            final int currentBucketWidth = widths[i];

            if (width < currentBucketWidth) {
                if (previousBucketWidth != 0) {
                    final int bucketDiff = currentBucketWidth - previousBucketWidth;
                    if (width < previousBucketWidth + (bucketDiff / 2)) {
                        return previousBucketWidth;
                    } else {
                        return currentBucketWidth;
                    }
                } else {
                    return currentBucketWidth;
                }
            } else if (i == widths.length - 1) {
                // If we get here then we're larger than a bucket
                if (width < currentBucketWidth * 2) {
                    return currentBucketWidth;
                }
            }

            previousBucketWidth = currentBucketWidth;
        }
        return Integer.MAX_VALUE;
    }

    private static String buildTraktUrl(final String originalUrl, final int width) {
        if (!TextUtils.isEmpty(originalUrl) && width != Integer.MAX_VALUE) {
            final int lastDot = originalUrl.lastIndexOf('.');
            if (lastDot != 0) {
                StringBuilder url = new StringBuilder(originalUrl.substring(0, lastDot));
                url.append('-').append(width);
                url.append(originalUrl.substring(lastDot));
                return url.toString();
            }
        }
        return originalUrl;
    }

    private static String buildTmdbUrl(String baseUrl, String imagePath, int width) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append('w').append(width);
        url.append(imagePath);
        return url.toString();
    }

}
