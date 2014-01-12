package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Ratings;
import com.jakewharton.trakt.enumerations.Rating;
import com.uwetrottmann.tmdb.entities.Configuration;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.Genre;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.philm.in.trakt.TraktUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;

public class PhilmMovie {

    public static final int NOT_SET = 0;

    public static final int TYPE_TMDB = 1;
    public static final int TYPE_IMDB = 2;
    public static final int TYPE_TRAKT = 3;

    private static final Calendar CALENDAR = Calendar.getInstance();

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

    String imdbId;
    Integer tmdbId;
    String title;
    String sortTitle;
    String overview;

    String posterUrl;
    int posterType;
    String fanartUrl;
    int fanartType;

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

    String localizedCountryCode;

    transient List<PhilmMovie> related;

    public PhilmMovie() {}

    private static String getSortTitle(String title) {
        for (int i = 0, z = TITLE_PREFIXES.length; i < z; i++) {
            final String prefix = TITLE_PREFIXES[i];
            if (title.startsWith(prefix)) {
                return title.substring(prefix.length());
            }
        }
        return title;
    }

    public void setFromMovie(com.jakewharton.trakt.entities.Movie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        if (!TextUtils.isEmpty(movie.tmdbId)) {
            tmdbId = Integer.parseInt(movie.tmdbId);
        }
        imdbId = movie.imdb_id;

        if (_id == null || idType == NOT_SET) {
            if (!TextUtils.isEmpty(imdbId)) {
                _id = new Long(imdbId.hashCode());
                idType = TYPE_IMDB;
            } else if (tmdbId != null) {
                _id = new Long(tmdbId);
                idType = TYPE_TMDB;
            } else {
                idType = NOT_SET;
            }
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
            fanartType = TYPE_TRAKT;
            posterType = TYPE_TRAKT;
        }

        if (movie.genres != null) {
            genres = getTraktGenreFormatStringList(movie.genres);
        }

        runtime = unbox(runtime, movie.runtime);
        if (!TextUtils.isEmpty(movie.certification)) {
            certification = movie.certification;
        }

        lastFetched = System.currentTimeMillis();
    }

    public void setFromMovie(com.uwetrottmann.tmdb.entities.Movie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        tmdbId = movie.id;
        if (!TextUtils.isEmpty(movie.imdb_id)) {
            imdbId = movie.imdb_id;
        }

        if (_id == null || idType == NOT_SET) {
            if (!TextUtils.isEmpty(imdbId)) {
                _id = new Long(imdbId.hashCode());
                idType = TYPE_IMDB;
            } else if (tmdbId != null) {
                _id = new Long(tmdbId);
                idType = TYPE_TMDB;
            } else {
                idType = NOT_SET;
            }
        }

        title = movie.title;
        sortTitle = getSortTitle(title);

        if (!TextUtils.isEmpty(movie.overview)) {
            overview = movie.overview;
        }

        releasedTime = unbox(releasedTime, movie.release_date);

        if (year == 0 && releasedTime > 0) {
            CALENDAR.setTimeInMillis(releasedTime);
            year = CALENDAR.get(Calendar.YEAR);
        }

        ratingPercent = unbox(ratingPercent, movie.vote_average);
        ratingVotes = unbox(ratingVotes, movie.vote_count);

        if (!TextUtils.isEmpty(movie.backdrop_path)) {
            fanartUrl = movie.backdrop_path;
            fanartType = TYPE_TMDB;
        }
        if (!TextUtils.isEmpty(movie.poster_path)) {
            posterUrl = movie.poster_path;
            posterType = TYPE_TMDB;
        }

        if (movie.genres != null) {
            genres = getTmdbGenreFormatStringList(movie.genres);
        }

        runtime = unbox(runtime, movie.runtime);

        lastFetched = System.currentTimeMillis();
    }

    public void updateFrom(CountryRelease countryRelease) {
        Preconditions.checkNotNull(countryRelease, "countryRelease cannot be null");

        if (!TextUtils.isEmpty(countryRelease.certification)) {
            certification = countryRelease.certification;
        }
        if (countryRelease.release_date != null) {
            releasedTime = countryRelease.release_date.getTime();
        }
        if (!TextUtils.isEmpty(countryRelease.iso_3166_1)) {
            localizedCountryCode = countryRelease.iso_3166_1;
        }
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

    public String getImdbId() {
        return imdbId;
    }

    public Integer getTmdbId() {
        return tmdbId;
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

    public String getLocalizedCountryCode() {
        return localizedCountryCode;
    }

    public int getFanartType() {
        return fanartType;
    }

    public int getPosterType() {
        return posterType;
    }

    public String getTraktId() {
        if (!TextUtils.isEmpty(imdbId)) {
            return imdbId;
        } else if (tmdbId != null) {
            return String.valueOf(tmdbId);
        }
        // TODO return the slugs
        return null;
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

        if (imdbId != null ? !imdbId.equals(that.imdbId) : that.imdbId != null) {
            return false;
        }
        if (tmdbId != null ? !tmdbId.equals(that.tmdbId) : that.tmdbId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = imdbId != null ? imdbId.hashCode() : 0;
        result = 31 * result + (tmdbId != null ? tmdbId.hashCode() : 0);
        return result;
    }

    private static boolean unbox(boolean currentValue, Boolean newValue) {
        return newValue != null ? newValue : currentValue;
    }

    private static int unbox(int currentValue, Integer newValue) {
        return newValue != null ? newValue : currentValue;
    }

    private static int unbox(int currentValue, Double newValue) {
        return newValue != null ? ((int) (newValue * 10)) : currentValue;
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

    private static String getTmdbGenreFormatStringList(List<Genre> list) {
        if (!PhilmCollections.isEmpty(list)) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, z = list.size() ; i < z ; i++) {
                sb.append(list.get(i).name);
                if (i < z - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return null;
    }

    private static String getTraktGenreFormatStringList(List<String> list) {
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
