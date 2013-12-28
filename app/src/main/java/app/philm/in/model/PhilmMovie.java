package app.philm.in.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import android.text.TextUtils;

import java.util.Comparator;

public class PhilmMovie {

    private static final String[] TITLE_PREFIXES = { "The ", "An " };

    public static final Comparator<PhilmMovie> COMPARATOR = new Comparator<PhilmMovie>() {
        @Override
        public int compare(PhilmMovie movie, PhilmMovie movie2) {
            return movie.getSortTitle().compareTo(movie.getSortTitle());
        }
    };

    private Movie traktEntity;
    private String mSortTitle;
    private String mId;

    public PhilmMovie(Movie traktEntity) {
        setMovie(traktEntity);
    }

    public static String getId(Movie rawMovie) {
        if (!TextUtils.isEmpty(rawMovie.imdb_id)) {
            return rawMovie.imdb_id;
        } else if (!TextUtils.isEmpty(rawMovie.tmdbId)) {
            return rawMovie.tmdbId;
        }
        return null;
    }

    public Movie getMovie() {
        return traktEntity;
    }

    public void setMovie(Movie movie) {
        traktEntity = Preconditions.checkNotNull(movie, "movie cannot be null");
    }

    public boolean isWatched() {
        if (traktEntity.watched != null) {
            return traktEntity.watched;
        } else if (traktEntity.plays != null) {
            return traktEntity.plays > 0;
        }
        return false;
    }

    public void setWatched(boolean watched) {
        if (watched != isWatched()) {
            traktEntity.watched = watched;
            traktEntity.plays = watched ? 1 : 0;
        }
    }

    public String getId() {
        if (mId == null) {
            mId = getId(traktEntity);
        }
        return mId;
    }

    public boolean inCollection() {
        if (traktEntity.inCollection != null) {
            return traktEntity.inCollection;
        }
        return false;
    }

    public void setInCollection(boolean inCollection) {
        if (inCollection != inCollection()) {
            traktEntity.inCollection = inCollection;
        }
    }

    public boolean inWatchlist() {
        if (traktEntity.inWatchlist != null) {
            return traktEntity.inWatchlist;
        }
        return false;
    }

    public void setInWatched(boolean inWatchlist) {
        if (inWatchlist != inWatchlist()) {
            traktEntity.inWatchlist = inWatchlist;
        }
    }

    public String getTitle() {
        return traktEntity.title;
    }

    public String getSortTitle() {
        if (mSortTitle == null) {
            mSortTitle = getTitle();
            for (int i = 0, z = TITLE_PREFIXES.length; i < z; i++) {
                final String prefix = TITLE_PREFIXES[i];
                if (mSortTitle.startsWith(prefix)) {
                    mSortTitle = mSortTitle.substring(prefix.length());
                }
            }
        }
        return mSortTitle;
    }

    public int getYear() {
        return traktEntity.year;
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
}
