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
            return movie.getSortTitle().compareTo(movie2.getSortTitle());
        }
    };

    private static final String[] TITLE_PREFIXES = { "The ", "An " };

    // tmdbId
    Long _id;

    String traktId;
    String title;
    String sortTitle;
    String overview;

    String posterUrl;
    String fanartUrl;

    boolean inWatchlist;
    boolean inCollection;
    boolean watched;

    int plays;
    int year;
    long releasedTime;

    int ratingPercent;
    int ratingVotes;

    public PhilmMovie() {}

    public PhilmMovie(Movie traktEntity) {
        setFromMovie(traktEntity);
    }

    public static String getTraktId(Movie rawMovie) {
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

        _id = Long.parseLong(movie.tmdbId);

        traktId = getTraktId(movie);
        title = movie.title;
        sortTitle = getSortTitle(title);

        if (!TextUtils.isEmpty(movie.overview)) {
            overview = movie.overview;
        }

        year = unbox(year, movie.year);
        inCollection = unbox(inCollection, movie.inCollection);
        inWatchlist = unbox(inWatchlist, movie.inWatchlist);
        watched = unbox(inWatchlist, movie.watched);
        plays = unbox(plays, movie.plays);
        releasedTime = unbox(releasedTime, movie.released);

        Ratings ratings = movie.ratings;
        if (ratings != null) {
            ratingPercent = unbox(ratingPercent, ratings.percentage);
            ratingVotes = unbox(ratingVotes, ratings.votes);
        }

        Images images = movie.images;
        if (images != null) {
            fanartUrl = images.fanart;
            posterUrl = images.poster;
        }
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
        plays = watched ? 1 : 0;
    }

    public long getDbId() {
        return _id;
    }

    public String getTraktId() {
        return traktId;
    }

    public boolean inCollection() {
        return inCollection;
    }

    public void setInCollection(boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean inWatchlist() {
        return inWatchlist;
    }

    public void setInWatched(boolean inWatchlist) {
        this.inWatchlist = inWatchlist;
    }

    public String getTitle() {
        return title;
    }

    public String getSortTitle() {
        return sortTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getFanartUrl() {
        return fanartUrl;
    }

    public long getReleasedTime() {
        return releasedTime;
    }

    public int getPlays() {
        return plays;
    }

    public int getYear() {
        return year;
    }

    public int getRatingPercent() {
        return ratingPercent;
    }

    public int getRatingVotes() {
        return ratingVotes;
    }

    public String getOverview() {
        return overview;
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

    private static boolean unbox(boolean currentValue, Boolean newValue) {
        return newValue != null ? newValue : currentValue;
    }

    private static int unbox(int currentValue, Integer newValue) {
        return newValue != null ? newValue : currentValue;
    }

    private static long unbox(long currentValue, Date newValue) {
        return newValue != null ? newValue.getTime() : currentValue;
    }
}
