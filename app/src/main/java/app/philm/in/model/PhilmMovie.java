package app.philm.in.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.Ratings;

import android.text.TextUtils;

import java.util.Comparator;
import java.util.Date;

public class PhilmMovie {

    public static final Comparator<PhilmMovie> COMPARATOR = new Comparator<PhilmMovie>() {
        @Override
        public int compare(PhilmMovie movie, PhilmMovie movie2) {
            return movie.getSortTitle().compareTo(movie.getSortTitle());
        }
    };

    private static final String[] TITLE_PREFIXES = { "The ", "An " };

    private String mId;

    private String mTitle;
    private String mSortTitle;
    private String mOverview;

    private String mPosterUrl;
    private String mFanartUrl;

    private boolean mInWatchlist;
    private boolean mInCollection;
    private boolean mWatched;

    private int mPlays;
    private int mYear;
    private long mReleasedTime;

    private int mRatingPercent;
    private int mRatingVotes;

    public PhilmMovie(Movie traktEntity) {
        setFromMovie(traktEntity);
    }

    public static String getId(Movie rawMovie) {
        if (!TextUtils.isEmpty(rawMovie.imdb_id)) {
            return rawMovie.imdb_id;
        } else if (!TextUtils.isEmpty(rawMovie.tmdbId)) {
            return rawMovie.tmdbId;
        }
        return null;
    }

    private static String getSortTitle(String title) {
        for (int i = 0, z = TITLE_PREFIXES.length; i < z; i++) {
            final String prefix = TITLE_PREFIXES[i];
            if (title.startsWith(prefix)) {
                return title.substring(prefix.length());
            }
        }
        return title;
    }

    public void setFromMovie(Movie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        mId = getId(movie);
        mTitle = movie.title;
        mSortTitle = getSortTitle(mTitle);
        mOverview = movie.overview;

        mYear = unbox(movie.year);
        mInCollection = unbox(movie.inCollection);
        mInWatchlist = unbox(movie.inWatchlist);
        mWatched = unbox(movie.watched);
        mPlays = unbox(movie.plays);
        mReleasedTime = unbox(movie.released);

        Ratings ratings = movie.ratings;
        if (ratings != null) {
            mRatingPercent = unbox(ratings.percentage);
            mRatingVotes = unbox(ratings.votes);
        }

        Images images = movie.images;
        if (images != null) {
            mFanartUrl = images.fanart;
            mPosterUrl = images.poster;
        }
    }

    public boolean isWatched() {
        return mWatched;
    }

    public void setWatched(boolean watched) {
        mWatched = watched;
        mPlays = watched ? 1 : 0;
    }

    public String getId() {
        return mId;
    }

    public boolean inCollection() {
        return mInCollection;
    }

    public void setInCollection(boolean inCollection) {
        mInCollection = inCollection;
    }

    public boolean inWatchlist() {
        return mInWatchlist;
    }

    public void setInWatched(boolean inWatchlist) {
        mInWatchlist = inWatchlist;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSortTitle() {
        return mSortTitle;
    }

    public String getPosterUrl() {
        return mPosterUrl;
    }

    public String getFanartUrl() {
        return mFanartUrl;
    }

    public long getReleasedTime() {
        return mReleasedTime;
    }

    public int getPlays() {
        return mPlays;
    }

    public int getYear() {
        return mYear;
    }

    public int getRatingPercent() {
        return mRatingPercent;
    }

    public int getRatingVotes() {
        return mRatingVotes;
    }

    public String getOverview() {
        return mOverview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return Objects.equal(getTitle(), ((PhilmMovie) o).getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTitle());
    }

    private static boolean unbox(Boolean value) {
        return value != null ? value : false;
    }

    private static int unbox(Integer value) {
        return value != null ? value : 0;
    }

    private static long unbox(Long value) {
        return value != null ? value : 0l;
    }

    private static long unbox(Date value) {
        return value != null ? value.getTime() : 0l;
    }
}
