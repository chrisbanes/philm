package app.philm.in.util;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import android.content.res.Resources;
import android.text.TextUtils;

import app.philm.in.R;

/**
 * Created by chris on 16/12/2013.
 */
public class TraktImageHelper {

    public static final int TYPE_SMALL = 0;
    public static final int TYPE_LARGE = 1;
    public static final int TYPE_UNCOMPRESSED = 2;

    private final Resources mResources;

    public TraktImageHelper(Resources resources) {
        mResources = Preconditions.checkNotNull(resources, "resources cannot be null");
    }

    public String getPosterUrl(Movie movie, final int type) {
        final String rawPosterUrl = movie.images.poster;

        if (type != TYPE_UNCOMPRESSED && !TextUtils.isEmpty(rawPosterUrl)) {
            final int lastDot = rawPosterUrl.lastIndexOf('.');

            if (lastDot != 0) {
                StringBuilder url = new StringBuilder(rawPosterUrl.substring(0, lastDot));
                switch (type) {
                    case TYPE_LARGE:
                        url.append("-300");
                        break;
                    case TYPE_SMALL:
                        url.append("-138");
                        break;
                }
                url.append(rawPosterUrl.substring(lastDot));
                return url.toString();
            }
        }

        return rawPosterUrl;
    }

    public String getPosterUrl(Movie movie) {
        return getPosterUrl(movie, mResources.getInteger(R.integer.trakt_image_size));
    }


}
