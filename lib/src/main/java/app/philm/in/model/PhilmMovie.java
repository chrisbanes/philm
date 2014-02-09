package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Ratings;
import com.jakewharton.trakt.enumerations.Rating;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.Credits;
import com.uwetrottmann.tmdb.entities.Genre;
import com.uwetrottmann.tmdb.entities.ReleasesResult;
import com.uwetrottmann.tmdb.entities.SpokenLanguage;
import com.uwetrottmann.tmdb.entities.Trailer;
import com.uwetrottmann.tmdb.entities.Trailers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.philm.in.trakt.TraktUtils;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;

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

    // TMDb
    int tmdbRatingPercent;
    int tmdbRatingVotes;
    // Trakt
    int ratingPercent;
    int ratingVotes;

    int runtime;
    String certification;
    String genres;

    String mainLanguageTitle;

    long lastFetched;

    String localizedCountryCode;

    boolean loadedFromTrakt;
    boolean loadedFromTmdb;

    transient List<PhilmMovie> related;
    transient List<PhilmCast> cast;
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
            if (TextUtils.isEmpty(fanartUrl)) {
                fanartUrl = images.fanart;
                fanartType = TYPE_TRAKT;
            }
            if (TextUtils.isEmpty(posterUrl)) {
                posterUrl = images.poster;
                posterType = TYPE_TRAKT;
            }
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

        releasedTime = unbox(releasedTime, movie.release_date);

        if (year == 0 && releasedTime > 0) {
            CALENDAR.setTimeInMillis(releasedTime);
            year = CALENDAR.get(Calendar.YEAR);
        }

        tmdbRatingPercent = unbox(tmdbRatingPercent, movie.vote_average);
        tmdbRatingVotes = unbox(tmdbRatingVotes, movie.vote_count);

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

        if (!PhilmCollections.isEmpty(movie.spoken_languages)) {
            SpokenLanguage mainLanguage = movie.spoken_languages.get(0);
            if (mainLanguage != null) {
                mainLanguageTitle = mainLanguage.name;
            }
        }

        runtime = unbox(runtime, movie.runtime);

        if (movie.credits != null) {
            updateWithCast(movie.credits);
        }

        if (movie.trailers != null) {
            updateWithTrailers(movie.trailers);
        }

        lastFetched = System.currentTimeMillis();
    }

    public void updateWithCast(final Credits credits) {
        Preconditions.checkNotNull(credits, "credits cannot be null");

        if (!PhilmCollections.isEmpty(credits.cast)) {
            Collections.sort(credits.cast, new Comparator<Credits.CastMember>() {
                @Override
                public int compare(Credits.CastMember castMember, Credits.CastMember castMember2) {
                    return castMember.order - castMember2.order;
                }
            });

            final ArrayList<PhilmCast> castList = new ArrayList<PhilmCast>();

            for (Credits.CastMember castMember : credits.cast) {
                final PhilmCast philmCastMember = new PhilmCast();
                philmCastMember.setFromCast(castMember);
                castList.add(philmCastMember);
            }

            setCast(castList);
        }
    }

    public void updateWithTrailers(final Trailers trailers) {
        Preconditions.checkNotNull(trailers, "trailers cannot be null");

        final ArrayList<PhilmTrailer> philmTrailers = new ArrayList<PhilmTrailer>();

        if (!PhilmCollections.isEmpty(trailers.youtube)) {
            for (Trailer trailer : trailers.youtube) {
                final PhilmTrailer philmTrailer = new PhilmTrailer();
                philmTrailer.setFromTmdb(PhilmTrailer.Source.YOUTUBE, trailer);
                philmTrailers.add(philmTrailer);
            }
        }

        setTrailers(philmTrailers);
    }

    public void updateWithReleases(final ReleasesResult releasesResult, final String countryCode) {
        Preconditions.checkNotNull(releasesResult, "releasesResult cannot be null");

        if (!PhilmCollections.isEmpty(releasesResult.countries)) {
            CountryRelease countryRelease = null;
            CountryRelease usRelease = null;

            for (CountryRelease release : releasesResult.countries) {
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
                }
                if (countryRelease.release_date != null) {
                    releasedTime = countryRelease.release_date.getTime();
                }
                if (!TextUtils.isEmpty(countryRelease.iso_3166_1)) {
                    localizedCountryCode = countryRelease.iso_3166_1;
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

    public int getTraktRatingPercent() {
        return ratingPercent;
    }

    public int getTraktRatingVotes() {
        return ratingVotes;
    }

    public int getTmdbRatingPercent() {
        return tmdbRatingPercent;
    }

    public int getTmdbRatingVotes() {
        return tmdbRatingVotes;
    }

    public int getAverageRatingPercent() {
        if (ratingPercent > 0 && tmdbRatingPercent > 0) {
            return weightAverage(ratingPercent, ratingVotes, tmdbRatingPercent, tmdbRatingVotes);
        } else {
            return Math.max(ratingPercent, tmdbRatingPercent);
        }
    }

    public int getAverageRatingVotes() {
        return tmdbRatingVotes + ratingVotes;
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

    public String getReleaseCountryCode() {
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

    public String getMainLanguageTitle() {
        return mainLanguageTitle;
    }

    public void setLoadedFromTmdb(boolean loadedFromTmdb) {
        this.loadedFromTmdb = loadedFromTmdb;
    }

    public void setLoadedFromTrakt(boolean loadedFromTrakt) {
        this.loadedFromTrakt = loadedFromTrakt;
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

    private static int weightAverage(int... values) {
        Preconditions.checkArgument(values.length % 2 == 0, "values must have a multiples of 2");

        int sum = 0;
        int sumWeight = 0;

        for (int i = 0; i < values.length; i += 2) {
            int value = values[i];
            int weight = values[i + 1];

            sum += (value * weight);
            sumWeight += weight;
        }

        return sum / sumWeight;
    }

}
