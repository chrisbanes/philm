package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Ratings;
import com.jakewharton.trakt.enumerations.Rating;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.Genre;
import com.uwetrottmann.tmdb.entities.Releases;
import com.uwetrottmann.tmdb.entities.SpokenLanguage;
import com.uwetrottmann.tmdb.entities.Trailer;
import com.uwetrottmann.tmdb.entities.Trailers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.trakt.TraktUtils;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.IntUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;

import static app.philm.in.util.TimeUtils.isPastThreshold;

public class PhilmMovie implements PhilmModel {

    public static final int NOT_SET = 0;

    private static final Calendar CALENDAR = Calendar.getInstance();

    public static final Comparator<PhilmMovie> COMPARATOR_SORT_TITLE
            = new Comparator<PhilmMovie>() {
        @Override
        public int compare(PhilmMovie movie, PhilmMovie movie2) {
            return movie.getSortTitle().compareTo(movie2.getSortTitle());
        }
    };

    public static final Comparator<ListItem<PhilmMovie>> COMPARATOR_LIST_ITEM_DATE_ASC
            = new ListItemReleaseDateComparator(true);

    private static class ListItemReleaseDateComparator implements Comparator<ListItem<PhilmMovie>> {

        private final boolean ascending;

        ListItemReleaseDateComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(ListItem<PhilmMovie> item1, ListItem<PhilmMovie> item2) {
            if (item1.getType() == ListItem.TYPE_SECTION) {
                return -1;
            } else if (item2.getType() == ListItem.TYPE_SECTION) {
                return 1;
            } else {
                final long time1 = item1.getItem().getReleasedTime();
                final long time2 = item2.getItem().getReleasedTime();
                if (time1 < time2) {
                    return ascending ? -1 : 1;
                } else if (time1 > time2) {
                    return ascending ? 1 : -1;
                }
            }

            return 0;
        }
    }

    private static final String[] TITLE_PREFIXES = {"The ", "An "};

    // tmdbId
    Long _id;
    int idType;

    String imdbId;
    Integer tmdbId;
    String title;
    String sortTitle;
    String overview;

    String posterUrl;
    int posterSourceType;
    String backdropUrl;
    int backdropSourceType;

    boolean inWatchlist;
    boolean inCollection;
    boolean watched;

    int plays;
    int year;

    long releasedTime;
    String releasedCountryCode;
    int releasedSourceType;

    int userRating;
    int userRatingAdvanced;

    int tmdbRatingPercent;
    int tmdbRatingVotes;
    int traktRatingPercent;
    int traktRatingVotes;

    int runtime;
    int runtimeSourceType;

    String certification;
    int certificationSourceType;

    String genres;
    int genresSourceType;

    String mainLanguageTitle;

    transient long lastFullFetchFromTraktStarted;
    transient long lastFullFetchFromTmdbStarted;
    long lastFullFetchFromTraktCompleted;
    long lastFullFetchFromTmdbCompleted;

    boolean loadedFromTrakt;
    boolean loadedFromTmdb;

    transient List<PhilmMovie> related;
    transient List<PhilmCast> cast;
    transient List<PhilmCrew> crew;
    transient List<PhilmTrailer> trailers;
    transient List<CountryRelease> releases;

    transient ColorScheme colorScheme;

    public PhilmMovie() {
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

    public void setFromMovie(com.jakewharton.trakt.entities.Movie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        loadedFromTrakt = true;

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
        if (!TextUtils.isEmpty(title)) {
            sortTitle = getSortTitle(title);
        }

        if (!TextUtils.isEmpty(movie.overview)) {
            overview = movie.overview;
        }

        year = unbox(year, movie.year);
        inCollection = unbox(inCollection, movie.inCollection);
        inWatchlist = unbox(inWatchlist, movie.inWatchlist);
        watched = unbox(watched, movie.watched);
        plays = unbox(plays, movie.plays);

        if (releasedSourceType != TYPE_TMDB) {
            releasedTime = unbox(releasedTime, movie.released);
        }

        Ratings ratings = movie.ratings;
        if (ratings != null) {
            traktRatingPercent = unbox(traktRatingPercent, ratings.percentage);
            traktRatingVotes = unbox(traktRatingVotes, ratings.votes);
        }

        userRating = unbox(userRating, movie.rating);
        userRatingAdvanced = unbox(userRatingAdvanced, movie.rating_advanced);

        Images images = movie.images;
        if (images != null) {
            // Prefer images from tmdb over trakt
            if (backdropSourceType != TYPE_TMDB && !TextUtils.isEmpty(images.fanart)) {
                backdropUrl = images.fanart;
                backdropSourceType = TYPE_TRAKT;
            }
            if (posterSourceType != TYPE_TMDB && !TextUtils.isEmpty(images.poster)) {
                posterUrl = images.poster;
                posterSourceType = TYPE_TRAKT;
            }
        }

        if (genresSourceType != TYPE_TMDB && movie.genres != null) {
            genres = getTraktGenreFormatStringList(movie.genres);
            genresSourceType = TYPE_TRAKT;
        }

        if (runtimeSourceType != TYPE_TMDB) {
            runtime = unbox(runtime, movie.runtime);
            runtimeSourceType = TYPE_TRAKT;
        }

        if (certificationSourceType != TYPE_TMDB && !TextUtils.isEmpty(movie.certification)) {
            certification = movie.certification;
            certificationSourceType = TYPE_TRAKT;
        }
    }

    public void setFromMovie(com.uwetrottmann.tmdb.entities.Movie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        loadedFromTmdb = true;

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
        if (!TextUtils.isEmpty(title)) {
            sortTitle = getSortTitle(title);
        }

        if (!TextUtils.isEmpty(movie.overview)) {
            overview = movie.overview;
        }

        // Only update from here if we do not have a country code
        if (movie.release_date != null && releasedCountryCode == null) {
            releasedTime = unbox(releasedTime, movie.release_date);
            releasedSourceType = TYPE_TMDB;
        }

        if (year == 0 && releasedTime > 0) {
            CALENDAR.setTimeInMillis(releasedTime);
            year = CALENDAR.get(Calendar.YEAR);
        }

        tmdbRatingPercent = unbox(tmdbRatingPercent, movie.vote_average);
        tmdbRatingVotes = unbox(tmdbRatingVotes, movie.vote_count);

        if (!TextUtils.isEmpty(movie.backdrop_path)) {
            backdropUrl = movie.backdrop_path;
            backdropSourceType = TYPE_TMDB;
        }
        if (!TextUtils.isEmpty(movie.poster_path)) {
            posterUrl = movie.poster_path;
            posterSourceType = TYPE_TMDB;
        }

        if (movie.genres != null) {
            genres = getTmdbGenreFormatStringList(movie.genres);
            genresSourceType = TYPE_TMDB;
        }

        if (!PhilmCollections.isEmpty(movie.spoken_languages)) {
            SpokenLanguage mainLanguage = movie.spoken_languages.get(0);
            if (mainLanguage != null) {
                mainLanguageTitle = mainLanguage.name;
            }
        }

        if (movie.runtime != null) {
            runtime = unbox(runtime, movie.runtime);
            runtimeSourceType = TYPE_TMDB;
        }

        if (movie.trailers != null) {
            updateWithTrailers(movie.trailers);
        }
    }

    public void updateWithTrailers(final Trailers trailers) {
        Preconditions.checkNotNull(trailers, "trailers cannot be null");

        if (!PhilmCollections.isEmpty(trailers.youtube)) {
            final ArrayList<PhilmTrailer> philmTrailers = new ArrayList<>();

            for (Trailer trailer : trailers.youtube) {
                final PhilmTrailer philmTrailer = new PhilmTrailer();
                philmTrailer.setFromTmdb(PhilmTrailer.Source.YOUTUBE, trailer);
                philmTrailers.add(philmTrailer);
            }

            setTrailers(philmTrailers);
        }
    }

    public void updateWithReleases(final Releases releases, final String countryCode) {
        Preconditions.checkNotNull(releases, "releases cannot be null");

        if (!PhilmCollections.isEmpty(releases.countries)) {
            CountryRelease countryRelease = null;
            CountryRelease usRelease = null;

            for (CountryRelease release : releases.countries) {
                if (countryCode != null && countryCode.equalsIgnoreCase(release.iso_3166_1)) {
                    countryRelease = release;
                    break;
                } else if (CountryProvider.US_TWO_LETTER_CODE
                        .equalsIgnoreCase(release.iso_3166_1)) {
                    usRelease = release;
                }
            }

            if (countryRelease == null) {
                countryRelease = usRelease;
            }

            if (countryRelease != null) {
                if (!TextUtils.isEmpty(countryRelease.certification)) {
                    certification = countryRelease.certification;
                    certificationSourceType = TYPE_TMDB;
                }
                if (countryRelease.release_date != null) {
                    releasedTime = countryRelease.release_date.getTime();
                    releasedCountryCode = countryRelease.iso_3166_1;
                    releasedSourceType = TYPE_TMDB;
                }
            }
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

    @Override
    public String getName() {
        return getTitle();
    }

    public String getSortTitle() {
        return sortTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
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

    public int getTraktRatingPercent() {
        return traktRatingPercent;
    }

    public int getTraktRatingVotes() {
        return traktRatingVotes;
    }

    public int getTmdbRatingPercent() {
        return tmdbRatingPercent;
    }

    public int getTmdbRatingVotes() {
        return tmdbRatingVotes;
    }

    public int getAverageRatingPercent() {
        if (traktRatingPercent > 0 && tmdbRatingPercent > 0) {
            return IntUtils.weightedAverage(
                    traktRatingPercent, traktRatingVotes,
                    tmdbRatingPercent, tmdbRatingVotes);
        } else {
            return Math.max(traktRatingPercent, tmdbRatingPercent);
        }
    }

    public int getAverageRatingVotes() {
        return tmdbRatingVotes + traktRatingVotes;
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

    public String getReleasedCountryCode() {
        return releasedCountryCode;
    }

    public int getBackdropSourceType() {
        return backdropSourceType;
    }

    public int getPosterSourceType() {
        return posterSourceType;
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

    public String getMainLanguageTitle() {
        return mainLanguageTitle;
    }

    private boolean needFullFetch() {
        return PhilmCollections.isEmpty(trailers)
                || PhilmCollections.isEmpty(cast)
                || PhilmCollections.isEmpty(crew)
                || PhilmCollections.isEmpty(related);
    }

    public boolean needFullFetchFromTmdb() {
        return (needFullFetch() || isPastThreshold(lastFullFetchFromTmdbCompleted,
                    Constants.STALE_MOVIE_DETAIL_THRESHOLD)) &&
                isPastThreshold(lastFullFetchFromTmdbStarted,
                    Constants.FULL_MOVIE_DETAIL_ATTEMPT_THRESHOLD);
    }

    public boolean needFullFetchFromTrakt() {
        return isPastThreshold(lastFullFetchFromTraktStarted,
                Constants.FULL_MOVIE_DETAIL_ATTEMPT_THRESHOLD)
                && isPastThreshold(lastFullFetchFromTraktCompleted,
                Constants.STALE_MOVIE_DETAIL_THRESHOLD);
    }

    public void markFullFetchStarted(final int type) {
        switch (type) {
            case TYPE_TMDB:
                lastFullFetchFromTmdbStarted = System.currentTimeMillis();
                break;
            case TYPE_TRAKT:
                lastFullFetchFromTraktStarted = System.currentTimeMillis();
                break;
        }
    }

    public void markFullFetchCompleted(final int type) {
        switch (type) {
            case TYPE_TMDB:
                lastFullFetchFromTmdbCompleted = System.currentTimeMillis();
                break;
            case TYPE_TRAKT:
                lastFullFetchFromTraktCompleted = System.currentTimeMillis();
                break;
        }
    }

    public boolean isLoadedFromTmdb() {
        return loadedFromTmdb;
    }

    public boolean isLoadedFromTrakt() {
        return loadedFromTrakt;
    }

    public List<PhilmCast> getCast() {
        return cast;
    }

    public void setCast(List<PhilmCast> cast) {
        this.cast = cast;
    }

    public List<PhilmCrew> getCrew() {
        return crew;
    }

    public void setCrew(List<PhilmCrew> crew) {
        this.crew = crew;
    }

    public List<PhilmTrailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<PhilmTrailer> trailers) {
        this.trailers = trailers;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
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
            for (int i = 0, z = list.size(); i < z; i++) {
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
            for (int i = 0, z = list.size(); i < z; i++) {
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
