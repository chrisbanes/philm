package app.philm.in.util;

import com.google.common.base.Preconditions;

import java.net.URLEncoder;

import app.philm.in.model.BasePhilmCast;
import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmMovie;

public class ImageHelper {

    private static final int[] TRAKT_POSTER_SIZES = { 138, 300 };
    private static final int[] TRAKT_BACKDROP_SIZES = { 218, 940 };

    private String mTmdbBaseUrl;
    private int[] mTmdbPosterSizes;
    private int[] mTmdbBackdropSizes;
    private int[] mTmdbProfileSizes;

    public void setTmdbBackdropSizes(int[] tmdbBackdropSizes) {
        mTmdbBackdropSizes = tmdbBackdropSizes;
    }

    public void setTmdbPosterSizes(int[] tmdbPosterSizes) {
        mTmdbPosterSizes = tmdbPosterSizes;
    }

    public void setTmdbBaseUrl(String baseUrl) {
        mTmdbBaseUrl = baseUrl;
    }

    public void setTmdbProfileSizes(int[] tmdbProfileSizes) {
        mTmdbProfileSizes = tmdbProfileSizes;
    }

    public String getPosterUrl(final PhilmMovie movie, final int width) {
        final String imageUrl = movie.getPosterUrl();
        Preconditions.checkNotNull(imageUrl, "movie must have poster url");

        switch (movie.getPosterSourceType()) {
            case PhilmMovie.TYPE_TMDB:
                return buildTmdbPosterUrl(imageUrl, width);
            default:
            case PhilmMovie.TYPE_TRAKT:
                return buildTraktUrl(imageUrl, selectSize(width, TRAKT_POSTER_SIZES));
        }
    }

    public String getFanartUrl(final PhilmMovie movie, final int width) {
        final String imageUrl = movie.getBackdropUrl();
        Preconditions.checkNotNull(imageUrl, "movie must have backdrop url");

        switch (movie.getBackdropSourceType()) {
            case PhilmMovie.TYPE_TMDB:
                return buildTmdbBackdropUrl(imageUrl, width);
            default:
            case PhilmMovie.TYPE_TRAKT:
                return buildTraktUrl(imageUrl, selectSize(width, TRAKT_BACKDROP_SIZES));
        }
    }

    public String getProfileUrl(final BasePhilmCast cast, final int width) {
        final String imageUrl = cast.getPictureUrl();
        Preconditions.checkNotNull(imageUrl, "movie must have picture url");

        switch (cast.getPictureType()) {
            case PhilmMovie.TYPE_TMDB:
                return buildTmdbBackdropUrl(imageUrl, width);
            default:
            case PhilmMovie.TYPE_TRAKT:
                return buildTraktUrl(imageUrl, selectSize(width, TRAKT_BACKDROP_SIZES));
        }
    }

    public String getResizedUrl(String url, int width, int height, String format) {
        StringBuffer sb = new StringBuffer("https://images1-focus-opensocial.googleusercontent.com/gadgets/proxy");
        sb.append("?container=focus");
        sb.append("&resize_w=").append(width);
        sb.append("&resize_h=").append(height);
        sb.append("&url=").append(URLEncoder.encode(url));
        sb.append("&refresh=31536000");
        return sb.toString();
    }

    private String buildTmdbPosterUrl(String imageUrl, int width) {
        if (mTmdbBaseUrl != null && mTmdbPosterSizes != null) {
            return buildTmdbUrl(mTmdbBaseUrl, imageUrl, selectSize(width, mTmdbPosterSizes));
        } else {
            return null;
        }
    }

    private String buildTmdbBackdropUrl(String imageUrl, int width) {
        if (mTmdbBaseUrl != null && mTmdbBackdropSizes != null) {
            return buildTmdbUrl(mTmdbBaseUrl, imageUrl, selectSize(width, mTmdbBackdropSizes));
        } else {
            return null;
        }
    }

    private String buildTmdbProfileUrl(String imageUrl, int width) {
        if (mTmdbBaseUrl != null && mTmdbProfileSizes != null) {
            return buildTmdbUrl(mTmdbBaseUrl, imageUrl, selectSize(width, mTmdbProfileSizes));
        } else {
            return null;
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
        if (width == Integer.MAX_VALUE) {
            url.append("original");
        } else {
            url.append('w').append(width);
        }
        url.append(imagePath);
        return url.toString();
    }

}
