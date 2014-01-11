package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.Ratings;
import com.jakewharton.trakt.enumerations.Rating;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.philm.in.trakt.TraktUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;

public class PhilmMovie {

    public static final int NOT_SET = 0;

    private static final int ID_TYPE_TMDB = 1;
    private static final int ID_TYPE_IMDB = 2;

    public static final Comparator<PhilmMovie> COMPARATOR = new Comparator<PhilmMovie>() {
        @Override
        public int compare(PhilmMovie movie, PhilmMovie movie2) {
            return movie.getSortTitle().compareTo(movie2.getSortTitle());
        }
    };

    private static final String[] TITLE_PREFIXES = { "The ", "An " };

    // tmdbId
    Long _id;
    int idType;

    String traktId;
    String imdbId;
    String tmdbId;
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

    int userRating;
    int userRatingAdvanced;
    int ratingPercent;
    int ratingVotes;

    int runtime;
    String certification;
    String genres;

    long lastFetched;

    transient List<PhilmMovie> related;

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

        tmdbId = movie.tmdbId;
        imdbId = movie.imdb_id;
        traktId = getTraktId(movie);

        if (!TextUtils.isEmpty(imdbId)) {
            _id = new Long(imdbId.hashCode());
            idType = ID_TYPE_IMDB;
        } else if (!TextUtils.isEmpty(tmdbId)) {
            _id = new Long(tmdbId.hashCode());
            idType = ID_TYPE_TMDB;
        } else {
            idType = NOT_SET;
        }

        title = movie.title;
        sortTitle = getSortTitle(title);

        if (!TextUtils.isEmpty(movie.overview)) {
            overview = movie.overview;
        }

        year = unbox(year, movie.year);
        inCollection = unbox(inCollection, movie.inCollection);
        inWatchlist = unbox(inWatchlist, movie.inWatchlist);
        watched = unbox(watched, movie.watched);
        plays = unbox(plays, movie.plays);
        releasedTime = unbox(releasedTime, movie.released);

        Ratings ratings = movie.ratings;
        if (ratings != null) {
            ratingPercent = unbox(ratingPercent, ratings.percentage);
            ratingVotes = unbox(ratingVotes, ratings.votes);
        }

        userRating = unbox(userRating, movie.rating);
        userRatingAdvanced = unbox(userRatingAdvanced, movie.rating_advanced);

        Images images = movie.images;
        if (images != null) {
            fanartUrl = images.fanart;
            posterUrl = images.poster;
        }

        if (movie.genres != null) {
            genres = getFormatStringList(movie.genres);
        }

        runtime = unbox(runtime, movie.runtime);
        if (!TextUtils.isEmpty(movie.certification)) {
            certification = movie.certification;
        }

        lastFetched = System.currentTimeMillis();
    }

    public boolean isWatched() {
        return watched || plays > 0;
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

    public int getUserRating() {
        return userRating;
    }

    public int getUserRatingAdvanced() {
        return userRatingAdvanced;
    }

    public void setUserRatingAdvanced(Rating rating) {
        userRatingAdvanced = unbox(userRatingAdvanced, rating);
    }

    public String getOverview() {
        return overview;
    }

    public long getLastFetchedTime() {
        return lastFetched;
    }

    public List<PhilmMovie> getRelated() {
        return related;
    }

    public void setRelated(List<PhilmMovie> related) {
        this.related = related;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getCertification() {
        return certification;
    }

    public String getGenres() {
        return genres;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PhilmMovie that = (PhilmMovie) o;

        if (year != that.year) {
            return false;
        }
        if (traktId != null ? !traktId.equals(that.traktId) : that.traktId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = traktId != null ? traktId.hashCode() : 0;
        result = 31 * result + year;
        return result;
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

    private static int unbox(int currentValue, Rating rating) {
        if (rating != null) {
            return TraktUtils.mapRatingToInt(rating);
        }
        return currentValue;
    }

    private static String getFormatStringList(List<String> list) {
        if (!PhilmCollections.isEmpty(list)) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, z = list.size() ; i < z ; i++) {
                sb.append(list.get(i));
                if (i < z - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return null;
    }

}
