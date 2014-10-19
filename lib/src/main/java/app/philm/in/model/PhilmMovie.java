/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Images;
import com.jakewharton.trakt.entities.Ratings;
import com.jakewharton.trakt.enumerations.Rating;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.Genre;
import com.uwetrottmann.tmdb.entities.Image;
import com.uwetrottmann.tmdb.entities.Releases;
import com.uwetrottmann.tmdb.entities.SpokenLanguage;
import com.uwetrottmann.tmdb.entities.Video;
import com.uwetrottmann.tmdb.entities.Videos;

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

public class PhilmMovie extends PhilmModel {

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

    String traktTitle;
    String tmdbTitle;

    String traktSortTitle;
    String tmdbSortTitle;

    String traktOverview;
    String tmdbOverview;

    String traktTagline;
    String tmdbTagline;

    String traktPosterUrl;
    String tmdbPosterUrl;

    String traktBackdropUrl;
    String tmdbBackdropUrl;

    boolean traktInWatchlist;
    boolean traktInCollection;
    boolean traktWatched;

    int traktPlays;

    int traktYear;
    int tmdbYear;

    boolean tmdbIsAdult;

    int tmdbBudget;

    long traktReleasedTime;
    String traktReleasedCountryCode;
    long tmdbReleasedTime;
    String tmdbReleasedCountryCode;

    int traktUserRating;
    int traktUserRatingAdvanced;

    int tmdbRatingPercent;
    int tmdbRatingVotes;
    int traktRatingPercent;
    int traktRatingVotes;

    int traktRuntime;
    int tmdbRuntime;

    String traktCertification;
    String tmdbCertification;

    String traktGenres;
    String tmdbGenres;

    String traktMainLanguage;
    String tmdbMainLanguage;

    transient long lastFullFetchFromTraktStarted;
    transient long lastFullFetchFromTmdbStarted;
    long lastFullFetchFromTraktCompleted;
    long lastFullFetchFromTmdbCompleted;

    boolean loadedFromTrakt;
    boolean loadedFromTmdb;

    transient List<PhilmMovie> related;
    transient List<PhilmMovieCredit> cast;
    transient List<PhilmMovieCredit> crew;
    transient List<PhilmMovieVideo> trailers;
    transient List<CountryRelease> releases;
    transient List<BackdropImage> mBackdropImages;

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

        tmdbId = movie.tmdbId;
        imdbId = movie.imdb_id;

        if (_id == null || idType == NOT_SET) {
            if (!TextUtils.isEmpty(imdbId)) {
                _id = new Long(imdbId.hashCode());
                idType = TYPE_IMDB;
            } else if (tmdbId != 0) {
                _id = new Long(tmdbId);
                idType = TYPE_TMDB;
            } else {
                idType = NOT_SET;
            }
        }

        traktTitle = movie.title;

        if (!TextUtils.isEmpty(traktTitle)) {
            traktSortTitle = getSortTitle(traktTitle);
        }

        if (!TextUtils.isEmpty(movie.overview)) {
            traktOverview = movie.overview;
        }

        if (!TextUtils.isEmpty(movie.tagline)) {
            traktTagline = movie.tagline;
        }

        traktYear = unbox(traktYear, movie.year);
        traktInCollection = unbox(traktInCollection, movie.inCollection);
        traktInWatchlist = unbox(traktInWatchlist, movie.inWatchlist);
        traktWatched = unbox(traktWatched, movie.watched);
        traktPlays = unbox(traktPlays, movie.plays);
        traktReleasedTime = unbox(traktReleasedTime, movie.released);
        traktRuntime = unbox(traktRuntime, movie.runtime);

        Ratings ratings = movie.ratings;
        if (ratings != null) {
            traktRatingPercent = unbox(traktRatingPercent, ratings.percentage);
            traktRatingVotes = unbox(traktRatingVotes, ratings.votes);
        }

        traktUserRating = unbox(traktUserRating, movie.rating);
        traktUserRatingAdvanced = unbox(traktUserRatingAdvanced, movie.rating_advanced);

        Images images = movie.images;
        if (images != null) {
            if (!TextUtils.isEmpty(images.fanart)) {
                traktBackdropUrl = images.fanart;
            }
            if (!TextUtils.isEmpty(images.poster)) {
                traktPosterUrl = images.poster;
            }
        }

        if (movie.genres != null) {
            traktGenres = getTraktGenreFormatStringList(movie.genres);
        }

        if (!TextUtils.isEmpty(movie.certification)) {
            traktCertification = movie.certification;
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

        if (!TextUtils.isEmpty(movie.title)) {
            tmdbTitle = movie.title;
            tmdbSortTitle = getSortTitle(movie.title);
        }

        if (!TextUtils.isEmpty(movie.overview)) {
            tmdbOverview = movie.overview;
        }

        if (!TextUtils.isEmpty(movie.tagline)) {
            tmdbTagline = movie.tagline;
        }

        // Only update from here if we do not have a country code
        if (movie.release_date != null && tmdbReleasedCountryCode == null) {
            tmdbReleasedTime = unbox(tmdbReleasedTime, movie.release_date);
        }

        if (tmdbYear == 0 && tmdbReleasedTime != 0) {
            CALENDAR.setTimeInMillis(tmdbReleasedTime);
            tmdbYear = CALENDAR.get(Calendar.YEAR);
        }

        tmdbIsAdult = unbox(tmdbIsAdult, movie.adult);

        tmdbBudget = unbox(tmdbBudget, movie.budget);

        tmdbRatingPercent = unbox(tmdbRatingPercent, movie.vote_average);
        tmdbRatingVotes = unbox(tmdbRatingVotes, movie.vote_count);

        if (!TextUtils.isEmpty(movie.backdrop_path)) {
            tmdbBackdropUrl = movie.backdrop_path;
        }
        if (!TextUtils.isEmpty(movie.poster_path)) {
            tmdbPosterUrl = movie.poster_path;
        }

        if (movie.genres != null) {
            tmdbGenres = getTmdbGenreFormatStringList(movie.genres);
        }

        if (!PhilmCollections.isEmpty(movie.spoken_languages)) {
            SpokenLanguage mainLang = movie.spoken_languages.get(0);
            if (mainLang != null) {
                tmdbMainLanguage = mainLang.name;
            }
        }

        tmdbRuntime = unbox(tmdbRuntime, movie.runtime);

        if (movie.videos != null) {
            updateWithVideos(movie.videos);
        }
    }

    public void updateWithVideos(final Videos videos) {
        Preconditions.checkNotNull(videos, "videos cannot be null");

        if (!PhilmCollections.isEmpty(videos.results)) {
            final ArrayList<PhilmMovieVideo> philmMovieVideos = new ArrayList<>();

            for (Video video : videos.results) {
                if (PhilmMovieVideo.isValid(video)) {
                    final PhilmMovieVideo philmMovieVideo = new PhilmMovieVideo();
                    philmMovieVideo.setFromTmdb(video);
                    philmMovieVideos.add(philmMovieVideo);
                }
            }

            setTrailers(philmMovieVideos);
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
                    tmdbCertification = countryRelease.certification;
                }
                if (countryRelease.release_date != null) {
                    tmdbReleasedTime = countryRelease.release_date.getTime();
                    tmdbReleasedCountryCode = countryRelease.iso_3166_1;

                    if (tmdbYear == 0 && tmdbReleasedTime != 0) {
                        CALENDAR.setTimeInMillis(tmdbReleasedTime);
                        tmdbYear = CALENDAR.get(Calendar.YEAR);
                    }
                }
            }
        }
    }

    public boolean isWatched() {
        return traktWatched || traktPlays > 0;
    }

    public void setWatched(boolean watched) {
        traktWatched = watched;
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
        return traktInCollection;
    }

    public void setInCollection(boolean inCollection) {
        traktInCollection = inCollection;
    }

    public boolean inWatchlist() {
        return traktInWatchlist;
    }

    public void setInWatched(boolean inWatchlist) {
        this.traktInWatchlist = inWatchlist;
    }

    public String getTitle() {
        return select(tmdbTitle, traktTitle);
    }

    public String getTagline() {
        return select(tmdbTagline, traktTagline);
    }

    public String getSortTitle() {
        return select(tmdbSortTitle, traktSortTitle);
    }

    public String getTraktPosterUrl() {
        return traktPosterUrl;
    }

    public String getTraktBackdropUrl() {
        return traktBackdropUrl;
    }

    public String getTmdbPosterUrl() {
        return tmdbPosterUrl;
    }

    public String getTmdbBackdropUrl() {
        return tmdbBackdropUrl;
    }

    public boolean hasPosterUrl() {
        return !TextUtils.isEmpty(tmdbPosterUrl) || !TextUtils.isEmpty(traktPosterUrl);
    }

    public boolean hasBackdropUrl() {
        return !TextUtils.isEmpty(tmdbBackdropUrl) || !TextUtils.isEmpty(traktBackdropUrl);
    }

    public long getReleasedTime() {
        return select(tmdbReleasedTime, traktReleasedTime);
    }

    public int getPlays() {
        return traktPlays;
    }

    public int getYear() {
        return select(tmdbYear, traktYear);
    }

    public int getBudget() {
        return tmdbBudget;
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
        return traktUserRating;
    }

    public int getUserRatingAdvanced() {
        return traktUserRatingAdvanced;
    }

    public void setUserRatingAdvanced(Rating rating) {
        traktUserRatingAdvanced = unbox(traktUserRatingAdvanced, rating);
    }

    public String getOverview() {
        return select(tmdbOverview, traktOverview);
    }

    public List<PhilmMovie> getRelated() {
        return related;
    }

    public void setRelated(List<PhilmMovie> related) {
        this.related = related;
    }

    public int getRuntime() {
        return select(tmdbRuntime, traktRuntime);
    }

    public String getCertification() {
        return select(tmdbCertification, traktCertification);
    }

    public String getGenres() {
        return select(tmdbGenres, traktGenres);
    }

    public String getReleasedCountryCode() {
        return select(tmdbReleasedCountryCode, traktReleasedCountryCode);
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

    public boolean isAdult() {
        return tmdbIsAdult;
    }

    public String getMainLanguageTitle() {
        return select(tmdbMainLanguage, traktMainLanguage);
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

    public List<PhilmMovieCredit> getCast() {
        return cast;
    }

    public void setCast(List<PhilmMovieCredit> cast) {
        this.cast = cast;
    }

    public List<PhilmMovieCredit> getCrew() {
        return crew;
    }

    public void setCrew(List<PhilmMovieCredit> crew) {
        this.crew = crew;
    }

    public List<PhilmMovieVideo> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<PhilmMovieVideo> trailers) {
        this.trailers = trailers;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public List<BackdropImage> getBackdropImages() {
        return mBackdropImages;
    }

    public void setBackdropImages(List<BackdropImage> backdropImages) {
        this.mBackdropImages = backdropImages;
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

        if (imdbId != null && that.imdbId != null) {
            return imdbId.equals(that.imdbId);
        }
        if (tmdbId != null && that.tmdbId != null) {
            return tmdbId.equals(that.tmdbId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(imdbId, tmdbId);
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("tmdbId", tmdbId)
                .add("imdbId", imdbId)
                .add("title", getTitle())
                .add("year", getYear())
                .add("runtime", getRuntime())
                .toString();
    }

    public static class BackdropImage {
        public final String url;
        public final int sourceType;

        public BackdropImage(String url, int sourceType) {
            this.url = Preconditions.checkNotNull(url, "url cannot be null");
            this.sourceType = sourceType;
        }

        public BackdropImage(Image image) {
            this.url = image.file_path;
            this.sourceType = TYPE_TMDB;
        }
    }
}
