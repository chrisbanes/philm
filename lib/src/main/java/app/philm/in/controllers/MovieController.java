package app.philm.in.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.enumerations.Rating;
import com.squareup.otto.Subscribe;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmModel;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.model.PhilmTrailer;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.WatchingMovie;
import app.philm.in.modules.qualifiers.GeneralPurpose;
import app.philm.in.network.NetworkError;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.tasks.AddToTraktCollectionRunnable;
import app.philm.in.tasks.AddToTraktWatchlistRunnable;
import app.philm.in.tasks.BaseMovieRunnable;
import app.philm.in.tasks.CancelCheckinTraktRunnable;
import app.philm.in.tasks.CheckinTraktRunnable;
import app.philm.in.tasks.FetchTmdbConfigurationRunnable;
import app.philm.in.tasks.FetchTmdbDetailMovieRunnable;
import app.philm.in.tasks.FetchTmdbMovieCreditsRunnable;
import app.philm.in.tasks.FetchTmdbMovieTrailersRunnable;
import app.philm.in.tasks.FetchTmdbMoviesReleasesRunnable;
import app.philm.in.tasks.FetchTmdbNowPlayingRunnable;
import app.philm.in.tasks.FetchTmdbPersonCreditsRunnable;
import app.philm.in.tasks.FetchTmdbPersonRunnable;
import app.philm.in.tasks.FetchTmdbPopularRunnable;
import app.philm.in.tasks.FetchTmdbRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTmdbSearchMoviesRunnable;
import app.philm.in.tasks.FetchTmdbSearchPeopleRunnable;
import app.philm.in.tasks.FetchTmdbUpcomingRunnable;
import app.philm.in.tasks.FetchTraktDetailMovieRunnable;
import app.philm.in.tasks.FetchTraktLibraryRunnable;
import app.philm.in.tasks.FetchTraktRecommendationsRunnable;
import app.philm.in.tasks.FetchTraktRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTraktTrendingRunnable;
import app.philm.in.tasks.FetchTraktWatchingRunnable;
import app.philm.in.tasks.FetchTraktWatchlistRunnable;
import app.philm.in.tasks.MarkTraktMovieSeenRunnable;
import app.philm.in.tasks.MarkTraktMovieUnseenRunnable;
import app.philm.in.tasks.RemoveFromTraktCollectionRunnable;
import app.philm.in.tasks.RemoveFromTraktWatchlistRunnable;
import app.philm.in.tasks.SubmitTraktMovieRatingRunnable;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.Injector;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.PhilmPreferences;
import app.philm.in.util.TextUtils;

import static app.philm.in.util.TimeUtils.isAfterThreshold;
import static app.philm.in.util.TimeUtils.isBeforeThreshold;
import static app.philm.in.util.TimeUtils.isInFuture;
import static app.philm.in.util.TimeUtils.isInPast;

@Singleton
public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    private static final String LOG_TAG = MovieController.class.getSimpleName();

    private static final int TMDB_FIRST_PAGE = 1;

    private final MoviesState mMoviesState;
    private final BackgroundExecutor mExecutor;
    private final AsyncDatabaseHelper mDbHelper;
    private final Logger mLogger;
    private final PhilmPreferences mPreferences;
    private final Injector mInjector;

    private boolean mPopulatedLibraryFromDb = false;
    private boolean mPopulatedWatchlistFromDb = false;

    @Inject
    public MovieController(
            MoviesState movieState,
            @GeneralPurpose BackgroundExecutor executor,
            AsyncDatabaseHelper dbHelper,
            Logger logger,
            PhilmPreferences preferences,
            Injector injector) {
        super();
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
        mLogger = Preconditions.checkNotNull(logger, "logger cannot be null");
        mPreferences = Preconditions.checkNotNull(preferences, "preferences cannot be null");
        mInjector = Preconditions.checkNotNull(injector, "injector cannot be null");
    }

    @Subscribe
    public void onLibraryChanged(MoviesState.LibraryChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onTrendingChanged(MoviesState.TrendingChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onPopularChanged(MoviesState.PopularChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onInTheatresChanged(MoviesState.InTheatresChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onUpcomingChanged(MoviesState.UpcomingChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onWatchlistChanged(MoviesState.WatchlistChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onAccountChanged(UserState.AccountChangedEvent event) {
        // Nuke all Movie State...
        mMoviesState.setLibrary(null);
        mMoviesState.setWatchlist(null);
        mMoviesState.setRecommended(null);
        mMoviesState.setSearchResult(null);
        mMoviesState.getImdbIdMovies().clear();
        mMoviesState.getTmdbIdMovies().clear();
        mMoviesState.setWatchingMovie(null);

        // TODO: Clear Database Too

        // If we have a new account, pre-fetch library & watchlist
        if (isLoggedIn()) {
            prefetchLibraryIfNeeded();
            prefetchWatchlistIfNeeded();
        }
    }

    @Subscribe
    public void onSearchResultChanged(MoviesState.SearchResultChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onRecommendedChanged(MoviesState.RecommendedChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onTmdbConfigurationChanged(MoviesState.TmdbConfigurationChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onMovieFlagsChanged(MoviesState.MovieFlagsUpdatedEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            // Refetch the recommended tab if the UI if event came from recommended
            if (MovieQueryType.RECOMMENDED == ui.getMovieQueryType()) {
                fetchRecommended(event.callingId);
            }

            populateUi(ui);
        } else {
            populateUis();
        }
    }

    @Subscribe
    public void onMovieDetailChanged(MoviesState.MovieInformationUpdatedEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            populateUi(ui);
            checkDetailMovieResult(event.callingId, event.item);
        }
    }

    @Subscribe
    public void onPersonCreditsChanged(MoviesState.PersonChangedEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            populateUi(ui);
        } else {
            populateUis();
        }
    }

    @Subscribe
    public void onMovieUserRatingChanged(MoviesState.MovieUserRatingChangedEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            populateUi(ui);
        } else {
            populateUis();
        }
    }

    @Subscribe
    public void onMovieReleasesChanged(MoviesState.MovieReleasesUpdatedEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            populateUi(ui);
        }
    }

    @Subscribe
    public void onMovieWatchingChanged(MoviesState.WatchingMovieUpdatedEvent event) {
        WatchingMovie watching = mMoviesState.getWatchingMovie();
        if (watching != null && watching.movie != null) {
            fetchDetailMovieIfNeeded(0, watching.movie.getImdbId());
        }
        populateUis();
    }

    @Subscribe
    public void onNetworkError(BaseState.OnErrorEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null && null != event.error) {
            ui.showError(event.error);
        }
    }

    @Subscribe
    public void onLoadingProgressVisibilityChanged(BaseState.ShowLoadingProgressEvent event) {
        MovieUi ui = findUi(event.callingId);
        if (ui != null) {
            if (event.secondary) {
                ui.showSecondaryLoadingProgress(event.show);
            } else {
                ui.showLoadingProgress(event.show);
            }
        }
    }

    @Override
    protected void onInited() {
        super.onInited();
        populateStateFromDb();
        mMoviesState.registerForEvents(this);

        if (mMoviesState.getTmdbConfiguration() == null) {
            fetchTmdbConfiguration();
        }

        if (isLoggedIn()) {
            fetchWatchingMovieIfNeeded();
        }
    }

    @Override
    protected void onSuspended() {
        super.onSuspended();
        mMoviesState.unregisterForEvents(this);
    }

    @Override
    protected MovieUiCallbacks createUiCallbacks(final MovieUi ui) {
        return new MovieUiCallbacks() {

            private int mTitleTextColor;
            private boolean mTitleTextColorSet;

            @Override
            public void onTitleChanged() {
                if (mTitleTextColorSet) {
                    updateDisplayTitle(ui.getUiTitle(), mTitleTextColor);
                } else {
                    updateDisplayTitle(ui.getUiTitle());
                }
            }

            @Override
            public void setTitleTextColor(int textColor) {
                mTitleTextColor = textColor;
                mTitleTextColorSet = true;
                onTitleChanged();
            }

            @Override
            public void addFilter(MovieFilter filter) {
                if (mMoviesState.getFilters().add(filter)) {
                    removeMutuallyExclusiveFilters(filter);
                    populateUis();
                }
            }

            @Override
            public void removeFilter(MovieFilter filter) {
                if (mMoviesState.getFilters().remove(filter)) {
                    populateUis();
                }
            }

            @Override
            public void clearFilters() {
                if (!mMoviesState.getFilters().isEmpty()) {
                    mMoviesState.getFilters().clear();
                    populateUis();
                }
            }

            @Override
            public void refresh() {
                switch (ui.getMovieQueryType()) {
                    case TRENDING:
                        fetchTrending(getId(ui));
                        break;
                    case LIBRARY:
                        fetchLibrary(getId(ui));
                        break;
                    case WATCHLIST:
                        fetchWatchlist(getId(ui));
                        break;
                    case DETAIL:
                        fetchDetailMovie(getId(ui), ui.getRequestParameter());
                        break;
                    case POPULAR:
                        fetchPopular(getId(ui));
                        break;
                    case RECOMMENDED:
                        fetchRecommended(getId(ui));
                        break;
                    case UPCOMING:
                        fetchUpcoming(getId(ui));
                        break;
                    case NOW_PLAYING:
                        fetchNowPlaying(getId(ui));
                        break;
                }
            }

            @Override
            public void showMovieDetail(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    if (!TextUtils.isEmpty(movie.getTraktId())) {
                        display.showMovieDetail(movie.getTraktId());
                    }
                    // TODO: Handle the else case
                }
            }

            @Override
            public void showMovieDetail(PhilmPersonCredit credit) {
                Preconditions.checkNotNull(credit, "credit cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showMovieDetail(String.valueOf(credit.getId()));
                }
            }

            @Override
            public void toggleMovieSeen(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                if (movie.isWatched()) {
                    markMoviesUnseen(getId(ui), movie.getTraktId());
                } else {
                    markMoviesSeen(getId(ui), movie.getTraktId());
                }
            }

            @Override
            public void toggleInWatchlist(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                if (movie.inWatchlist()) {
                    removeFromWatchlist(getId(ui), movie.getTraktId());
                } else {
                    addToWatchlist(getId(ui), movie.getTraktId());
                }
            }

            @Override
            public void toggleInCollection(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                if (movie.inCollection()) {
                    removeFromCollection(getId(ui), movie.getTraktId());
                } else {
                    addToCollection(getId(ui), movie.getTraktId());
                }
            }

            @Override
            public void setMoviesInCollection(List<PhilmMovie> movies, boolean inCollection) {
                final ArrayList<String> ids = new ArrayList<>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (inCollection != movie.inCollection()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (inCollection) {
                    addToCollection(getId(ui), ids.toArray(idsArray));
                } else {
                    removeFromCollection(getId(ui), ids.toArray(idsArray));
                }
            }

            @Override
            public void setMoviesInWatchlist(List<PhilmMovie> movies, boolean inWatchlist) {
                final ArrayList<String> ids = new ArrayList<>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (inWatchlist != movie.inWatchlist()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (inWatchlist) {
                    addToWatchlist(getId(ui), ids.toArray(idsArray));
                } else {
                    removeFromWatchlist(getId(ui), ids.toArray(idsArray));
                }
            }

            @Override
            public void setMoviesSeen(List<PhilmMovie> movies, boolean seen) {
                final ArrayList<String> ids = new ArrayList<>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (seen != movie.isWatched()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (seen) {
                    markMoviesSeen(getId(ui), ids.toArray(idsArray));
                } else {
                    markMoviesUnseen(getId(ui), ids.toArray(idsArray));
                }
            }

            @Override
            public void search(String query) {
                switch (ui.getMovieQueryType()) {
                    case SEARCH:
                        fetchSearchResults(getId(ui), query);
                        break;
                    case SEARCH_MOVIES:
                        fetchMovieSearchResults(getId(ui), query);
                        break;
                    case SEARCH_PEOPLE:
                        fetchPeopleSearchResults(getId(ui), query);
                        break;
                }
            }

            @Override
            public void clearSearch() {
                mMoviesState.setSearchResult(null);
            }

            @Override
            public void showRateMovie(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showRateMovieFragment(movie.getTraktId());
                }
            }

            @Override
            public void submitRating(PhilmMovie movie, Rating rating) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                markMovieRating(getId(ui), movie.getTraktId(), rating);
            }

            @Override
            public void onScrolledToBottom() {
                MoviesState.SearchResult searchResult;
                MoviesState.MoviePaginatedResult result;

                switch (ui.getMovieQueryType()) {
                    case POPULAR:
                        result = mMoviesState.getPopular();
                        if (canFetchNextPage(result)) {
                            fetchPopular(getId(ui), result.page + 1);
                        }
                        break;
                    case SEARCH_PEOPLE:
                        searchResult = mMoviesState.getSearchResult();
                        if (searchResult != null && canFetchNextPage(searchResult.people)) {
                            fetchPeopleSearchResults(
                                    getId(ui),
                                    searchResult.query,
                                    searchResult.people.page + 1);
                        }
                        break;
                    case SEARCH_MOVIES:
                        searchResult = mMoviesState.getSearchResult();
                        if (searchResult != null && canFetchNextPage(searchResult.movies)) {
                            fetchMovieSearchResults(
                                    getId(ui),
                                    searchResult.query,
                                    searchResult.movies.page + 1);
                        }
                        break;
                    case UPCOMING:
                        result = mMoviesState.getUpcoming();
                        if (canFetchNextPage(result)) {
                            fetchUpcoming(getId(ui), result.page + 1);
                        }
                        break;
                }
            }

            @Override
            public void showRelatedMovies(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showRelatedMovies(String.valueOf(movie.getTmdbId()));
                }
            }

            @Override
            public void showCastList(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showCastList(String.valueOf(movie.getTmdbId()));
                }
            }

            @Override
            public void showCrewList(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showCrewList(String.valueOf(movie.getTmdbId()));
                }
            }

            @Override
            public void checkin(PhilmMovie movie, String message, boolean shareFacebook,
                    boolean shareTwitter, boolean sharePath, boolean shareTumblr) {
                Preconditions.checkNotNull(movie, "movie cannot be null");
                checkinMovie(getId(ui), movie, message, shareFacebook, shareTwitter, sharePath,
                        shareTumblr);
            }

            @Override
            public void cancelCurrentCheckin() {
                cancelCheckin(getId(ui));
            }

            @Override
            public void requestCheckin(PhilmMovie movie) {
                Preconditions.checkNotNull(movie, "movie cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showCheckin(movie.getTraktId());
                }
            }

            @Override
            public void requestCancelCurrentCheckin() {
                Display display = getDisplay();
                if (display != null) {
                    display.showCancelCheckin();
                }
            }

            @Override
            public void showPersonDetail(PhilmPerson person) {
                Preconditions.checkNotNull(person, "person cannot be null");
                Preconditions.checkNotNull(person.getTmdbId(), "person id cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showPersonDetail(String.valueOf(person.getTmdbId()));
                }
            }

            @Override
            public void showPersonCastCredits(PhilmPerson person) {
                Preconditions.checkNotNull(person, "person cannot be null");
                Preconditions.checkNotNull(person.getTmdbId(), "person id cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showPersonCastCredits(String.valueOf(person.getTmdbId()));
                }
            }

            @Override
            public void showPersonCrewCredits(PhilmPerson person) {
                Preconditions.checkNotNull(person, "person cannot be null");
                Preconditions.checkNotNull(person.getTmdbId(), "person id cannot be null");

                Display display = getDisplay();
                if (display != null) {
                    display.showPersonCrewCredits(String.valueOf(person.getTmdbId()));
                }

            }

            @Override
            public void showMovieSearchResults() {
                Display display = getDisplay();
                if (display != null) {
                    display.showSearchMoviesFragment();
                }
            }

            @Override
            public void showPeopleSearchResults() {
                Display display = getDisplay();
                if (display != null) {
                    display.showSearchPeopleFragment();
                }
            }

            @Override
            public void playTrailer(PhilmTrailer trailer) {
                Preconditions.checkNotNull(trailer, "trailer cannot be null");
                Preconditions.checkNotNull(trailer.getId(), "trailer id cannot be null");

                final Display display = getDisplay();
                if (display != null) {
                    switch (trailer.getSource()) {
                        case YOUTUBE:
                            display.playYoutubeVideo(trailer.getId());
                            break;
                    }
                }
            }

            private boolean canFetchNextPage(MoviesState.PaginatedResult<?> paginatedResult) {
                return paginatedResult != null && paginatedResult.page < paginatedResult.totalPages;
            }
        };
    }

    @Override
    protected void onUiAttached(final MovieUi ui) {
        final MovieQueryType queryType = ui.getMovieQueryType();

        if (queryType.requireLogin() && !isLoggedIn()) {
            return;
        }

        Display display = getDisplay();
        if (display != null && !ui.isModal()) {
            display.showUpNavigation(queryType != null && queryType.showUpNavigation());
        }

        final int callingId = getId(ui);

        switch (queryType) {
            case TRENDING:
                fetchTrendingIfNeeded(callingId);
                break;
            case POPULAR:
                fetchPopularIfNeeded(callingId);
                break;
            case LIBRARY:
                fetchLibraryIfNeeded(callingId);
                break;
            case WATCHLIST:
                fetchWatchlistIfNeeded(callingId);
                break;
            case DETAIL:
                fetchDetailMovieIfNeeded(callingId, ui.getRequestParameter());
                break;
            case NOW_PLAYING:
                fetchNowPlayingIfNeeded(callingId);
                break;
            case UPCOMING:
                fetchUpcomingIfNeeded(callingId);
                break;
            case RECOMMENDED:
                fetchRecommendedIfNeeded(callingId);
                break;
            case RELATED:
                fetchRelatedIfNeeded(callingId, ui.getRequestParameter());
                break;
            case MOVIE_CAST:
                fetchMovieCastIfNeeded(callingId, ui.getRequestParameter());
                break;
            case MOVIE_CREW:
                fetchMovieCrewIfNeeded(callingId, ui.getRequestParameter());
                break;
            case PERSON_DETAIL:
                fetchPersonIfNeeded(callingId, ui.getRequestParameter());
                break;
            case PERSON_CREDITS_CREW:
            case PERSON_CREDITS_CAST:
                fetchPersonCreditsIfNeeded(callingId, ui.getRequestParameter());
                break;
        }
    }

    @Override
    protected void populateUi(final MovieUi ui) {
        if (!isLoggedIn() && ui.getMovieQueryType().requireLogin()) {
            ui.showError(NetworkError.UNAUTHORIZED_TRAKT);
            return;
        }

        if (mMoviesState.getTmdbConfiguration() == null) {
            mLogger.i(LOG_TAG, "TMDB Configuration not downloaded yet.");
            return;
        }

        if (Constants.DEBUG) {
            mLogger.d(LOG_TAG, "populateUi: " + ui.getClass().getSimpleName());
        }

        if (ui instanceof SearchMovieUi) {
            populateSearchMovieUi((SearchMovieUi) ui);
        } else if (ui instanceof MovieListUi) {
            populateMovieListUi((MovieListUi) ui);
        } else if (ui instanceof MovieCreditListUi) {
            populateMovieCreditListUi((MovieCreditListUi) ui);
        } else if (ui instanceof MovieDetailUi) {
            populateDetailUi((MovieDetailUi) ui);
        } else if (ui instanceof MovieRateUi) {
            populateRateUi((MovieRateUi) ui);
        } else if (ui instanceof MovieDiscoverUi) {
            populateMovieDiscoverUi((MovieDiscoverUi) ui);
        } else if (ui instanceof MovieCheckinUi) {
            populateCheckinUi((MovieCheckinUi) ui);
        } else if (ui instanceof CancelCheckinUi) {
            populateCancelCheckinUi((CancelCheckinUi) ui);
        } else if (ui instanceof PersonCreditListUi) {
            populatePersonCreditListUi((PersonCreditListUi) ui);
        } else if (ui instanceof PersonUi) {
            populatePersonUi((PersonUi) ui);
        } else if (ui instanceof MainSearchUi) {
            populateSearchUi((MainSearchUi) ui);
        } else if (ui instanceof SearchPersonUi) {
            populateSearchPersonUi((SearchPersonUi) ui);
        } else if (ui instanceof PersonListUi) {
            populatePersonListUi((PersonListUi) ui);
        }
    }

    private void addToCollection(final int callingId, String... ids) {
        executeTask(new AddToTraktCollectionRunnable(callingId, ids));
    }

    private void addToWatchlist(final int callingId, String... ids) {
        executeTask(new AddToTraktWatchlistRunnable(callingId, ids));
    }

    private void cancelCheckin(int callingId) {
        if (mMoviesState.getWatchingMovie() != null) {
            executeTask(new CancelCheckinTraktRunnable(callingId));
        }
    }

    private void checkinMovie(int callingId, PhilmMovie movie, String message,
            boolean shareFacebook, boolean shareTwitter, boolean sharePath, boolean shareTumblr) {
        Preconditions.checkNotNull(movie, "movie cannot be null");
        executeTask(new CheckinTraktRunnable(callingId, movie.getImdbId(), message, shareFacebook,
                shareTwitter, sharePath, shareTumblr));
    }

    private void checkDetailMovieResult(int callingId, PhilmMovie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");
        fetchDetailMovieIfNeeded(callingId, movie, false);
    }

    private <T> List<ListItem<T>> createListItemList(
            final ListItem.SectionTitle title,
            final List<T> items) {
        Preconditions.checkNotNull(items, "items cannot be null");

        ArrayList<ListItem<T>> listItems = new ArrayList<>(items.size());

        if (title != null) {
            listItems.add(new ListItem<T>(title));
        }

        for (T item : items) {
            listItems.add(new ListItem<>(item));
        }
        return listItems;
    }

    private <T, F extends Filter<T>> List<ListItem<T>> createSectionedListItemList(
            final List<T> items,
            final List<F> sections,
            List<F> sectionProcessingOrder) {
        Preconditions.checkNotNull(items, "items cannot be null");
        Preconditions.checkNotNull(sections, "sections cannot be null");

        if (sectionProcessingOrder != null) {
            Preconditions.checkArgument(sections.size() == sectionProcessingOrder.size(),
                    "sections and sectionProcessingOrder must be the same size");
        } else {
            sectionProcessingOrder = sections;
        }

        final List<ListItem<T>> result = new ArrayList<>(items.size());
        final HashSet<T> movies = new HashSet<>(items);

        Map<F, List<ListItem<T>>> sectionsItemLists = null;

        for (F filter : sectionProcessingOrder) {
            List<ListItem<T>> sectionItems = null;

            for (Iterator<T> i = movies.iterator(); i.hasNext(); ) {
                T movie = i.next();
                if (movie != null && filter.isFiltered(movie)) {
                    if (sectionItems == null) {
                        sectionItems = new ArrayList<>();
                        // Now add Title
                        sectionItems.add(new ListItem<T>(filter.getSectionTitle()));
                    }
                    sectionItems.add(new ListItem<>(movie));
                    i.remove();
                }
            }

            if (!PhilmCollections.isEmpty(sectionItems)) {
                if (sectionsItemLists == null) {
                    sectionsItemLists = new ArrayMap<>();
                }
                filter.sortListItems(sectionItems);
                sectionsItemLists.put(filter, sectionItems);
            }
        }

        if (sectionsItemLists != null) {
            for (F filter : sections) {
                if (sectionsItemLists.containsKey(filter)) {
                    result.addAll(sectionsItemLists.get(filter));
                }
            }
        }

        return result;
    }

    private <R> void executeTask(BaseMovieRunnable<R> task) {
        mInjector.inject(task);
        mExecutor.execute(task);
    }

    private void fetchDetailMovie(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        final PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null) {
            fetchDetailMovieIfNeeded(callingId, movie, true);
        } else {
            // TODO Try and parse id to guess type
            fetchDetailMovieFromTrakt(callingId, id);
        }
    }

    private void fetchDetailMovieFromTmdb(final int callingId, int id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null) {
            movie.markFullFetchStarted(PhilmModel.TYPE_TMDB);
        }

        executeTask(new FetchTmdbDetailMovieRunnable(callingId, id));
    }

    private void fetchDetailMovieFromTrakt(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null) {
            movie.markFullFetchStarted(PhilmModel.TYPE_TRAKT);
        }

        executeTask(new FetchTraktDetailMovieRunnable(callingId, id));
    }

    private void fetchDetailMovieIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie cached = mMoviesState.getMovie(id);
        if (cached == null) {
            fetchDetailMovie(callingId, id);
        } else {
            fetchDetailMovieIfNeeded(callingId, cached, false);
        }
    }

    private void fetchDetailMovieIfNeeded(int callingId, PhilmMovie movie, boolean force) {
        Preconditions.checkNotNull(movie, "movie cannot be null");

        if (isLoggedIn() && (force || movie.needFullFetchFromTrakt())) {
            if (movie.getImdbId() != null) {
                fetchDetailMovieFromTrakt(callingId, movie.getImdbId());
            } else if (movie.getTmdbId() != null) {
                fetchDetailMovieFromTrakt(callingId, String.valueOf(movie.getTmdbId()));
            }
        }

        if (force || movie.needFullFetchFromTmdb()) {
            if (movie.getTmdbId() != null) {
                fetchDetailMovieFromTmdb(callingId, movie.getTmdbId());
            }
        }
    }

    private void fetchRelatedIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null && PhilmCollections.isEmpty(movie.getRelated())) {
            fetchRelatedMovies(callingId, movie);
        }
    }

    private void fetchMovieCastIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null && PhilmCollections.isEmpty(movie.getCast())) {
            fetchMovieCredits(callingId, movie);
        }
    }

    private void fetchMovieCrewIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null && PhilmCollections.isEmpty(movie.getCrew())) {
            fetchMovieCredits(callingId, movie);
        }
    }

    private void fetchPersonIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmPerson person = mMoviesState.getPerson(id);
        if (person == null || !person.hasFetchedCredits()) {
            fetchPerson(callingId, Integer.parseInt(id));
        }
    }

    private void fetchPersonCreditsIfNeeded(final int callingId, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmPerson person = mMoviesState.getPerson(id);
        if (person != null && !person.hasFetchedCredits()) {
            fetchPersonCredits(callingId, person);
        }
    }

    private void fetchLibrary(final int callingId) {
        if (isLoggedIn()) {
            executeTask(new FetchTraktLibraryRunnable(callingId, mMoviesState.getUsername()));
        }
    }

    private void fetchLibraryIfNeeded(final int callingId) {
        if (mPopulatedLibraryFromDb && PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            fetchLibrary(callingId);
        }
    }

    private void fetchMovieReleases(final int callingId, int tmdbId) {
        executeTask(new FetchTmdbMoviesReleasesRunnable(callingId, tmdbId));
    }

    private void fetchNowPlaying(final int callingId) {
        mMoviesState.setNowPlaying(null);
        fetchNowPlaying(callingId, TMDB_FIRST_PAGE);
    }

    private void fetchNowPlaying(final int callingId, final int page) {
        executeTask(new FetchTmdbNowPlayingRunnable(callingId, page));
    }

    private void fetchNowPlayingIfNeeded(final int callingId) {
        MoviesState.MoviePaginatedResult nowPlaying = mMoviesState.getNowPlaying();
        if (nowPlaying == null || PhilmCollections.isEmpty(nowPlaying.items)) {
            fetchNowPlaying(callingId, TMDB_FIRST_PAGE);
        }
    }

    private void fetchPopular(final int callingId) {
        mMoviesState.setPopular(null);
        fetchPopular(callingId, TMDB_FIRST_PAGE);
    }

    private void fetchPopular(final int callingId, final int page) {
        executeTask(new FetchTmdbPopularRunnable(callingId, page));
    }

    private void fetchPopularIfNeeded(final int callingId) {
        MoviesState.MoviePaginatedResult popular = mMoviesState.getPopular();
        if (popular == null || PhilmCollections.isEmpty(popular.items)) {
            fetchPopular(callingId, TMDB_FIRST_PAGE);
        }
    }

    private void fetchRecommended(final int callingId) {
        Preconditions.checkState(isLoggedIn(), "Must be logged in to trakt for recommendations");
        executeTask(new FetchTraktRecommendationsRunnable(callingId));
    }

    private void fetchRecommendedIfNeeded(final int callingId) {
        if (PhilmCollections.isEmpty(mMoviesState.getRecommended())) {
            fetchRecommended(callingId);
        }
    }

    private void fetchRelatedMovies(final int callingId, PhilmMovie movie) {
        if (movie.getTmdbId() != null) {
            executeTask(new FetchTmdbRelatedMoviesRunnable(callingId, movie.getTmdbId()));
        } else if (!TextUtils.isEmpty(movie.getImdbId())) {
            executeTask(new FetchTraktRelatedMoviesRunnable(callingId, movie.getImdbId()));
        }
    }

    private void fetchTrailers(final int callingId, PhilmMovie movie) {
        if (movie.getTmdbId() != null) {
            executeTask(new FetchTmdbMovieTrailersRunnable(callingId, movie.getTmdbId()));
        }
    }

    private void fetchMovieCredits(final int callingId, PhilmMovie movie) {
        if (movie.getTmdbId() != null) {
            executeTask(new FetchTmdbMovieCreditsRunnable(callingId, movie.getTmdbId()));
        }
    }

    private void fetchPersonCredits(final int callingId, PhilmPerson person) {
        executeTask(new FetchTmdbPersonCreditsRunnable(callingId, person.getTmdbId()));
    }

    private void fetchPerson(final int callingId, int id) {
        executeTask(new FetchTmdbPersonRunnable(callingId, id));
    }

    private void fetchSearchResults(final int callingId, String query) {
        mMoviesState.setSearchResult(new MoviesState.SearchResult(query));
        fetchMovieSearchResults(callingId, query, TMDB_FIRST_PAGE);
        fetchPeopleSearchResults(callingId, query, TMDB_FIRST_PAGE);
    }

    private void fetchMovieSearchResults(final int callingId, String query) {
        mMoviesState.setSearchResult(new MoviesState.SearchResult(query));
        fetchMovieSearchResults(callingId, query, TMDB_FIRST_PAGE);
    }

    private void fetchPeopleSearchResults(final int callingId, String query) {
        mMoviesState.setSearchResult(new MoviesState.SearchResult(query));
        fetchPeopleSearchResults(callingId, query, TMDB_FIRST_PAGE);
    }

    private void fetchMovieSearchResults(final int callingId, String query, int page) {
        executeTask(new FetchTmdbSearchMoviesRunnable(callingId, query, page));
    }

    private void fetchPeopleSearchResults(final int callingId, String query, int page) {
        executeTask(new FetchTmdbSearchPeopleRunnable(callingId, query, page));
    }

    private void fetchTmdbConfiguration() {
        FetchTmdbConfigurationRunnable task = new FetchTmdbConfigurationRunnable();
        mInjector.inject(task);
        mExecutor.execute(task);
    }

    private void fetchTrending(final int callingId) {
        executeTask(new FetchTraktTrendingRunnable(callingId));
    }

    private void fetchTrendingIfNeeded(final int callingId) {
        if (PhilmCollections.isEmpty(mMoviesState.getTrending())) {
            fetchTrending(callingId);
        }
    }

    private void fetchUpcoming(final int callingId) {
        mMoviesState.setUpcoming(null);
        fetchUpcoming(callingId, TMDB_FIRST_PAGE);
    }

    private void fetchUpcoming(final int callingId, final int page) {
        executeTask(new FetchTmdbUpcomingRunnable(callingId, page));
    }

    private void fetchUpcomingIfNeeded(final int callingId) {
        MoviesState.MoviePaginatedResult upcoming = mMoviesState.getUpcoming();
        if (upcoming == null || PhilmCollections.isEmpty(upcoming.items)) {
            fetchUpcoming(callingId, TMDB_FIRST_PAGE);
        }
    }

    private void fetchWatchlist(final int callingId) {
        if (isLoggedIn()) {
            executeTask(new FetchTraktWatchlistRunnable(callingId, mMoviesState.getUsername()));
        }
    }

    private void fetchWatchlistIfNeeded(final int callingId) {
        if (mPopulatedWatchlistFromDb && PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            fetchWatchlist(callingId);
        }
    }

    private void fetchWatchingMovieIfNeeded() {
        // TODO Add some checks for time
        fetchWatchingMovie();
    }

    private void fetchWatchingMovie() {
        executeTask(new FetchTraktWatchingRunnable(0, mMoviesState.getUsername()));
    }

    private List<PhilmMovie> filterMovies(List<PhilmMovie> movies) {
        Preconditions.checkNotNull(movies, "movies cannot be null");

        final Set<MovieFilter> filters = mMoviesState.getFilters();
        Preconditions.checkNotNull(filters, "filters cannot be null");
        Preconditions.checkState(!filters.isEmpty(), "filters cannot be empty");

        ArrayList<PhilmMovie> filteredMovies = new ArrayList<>();
        for (PhilmMovie movie : movies) {
            boolean filtered = true;
            for (MovieFilter filter : filters) {
                if (movie == null || !filter.isFiltered(movie)) {
                    filtered = false;
                    break;
                }
            }
            if (filtered) {
                filteredMovies.add(movie);
            }
        }
        return filteredMovies;
    }

    private MovieUi getMovieUiAttached(MovieQueryType queryType) {
        for (MovieUi ui : getUis()) {
            if (ui.getMovieQueryType() == queryType) {
                return ui;
            }
        }
        return null;
    }

    private boolean isLoggedIn() {
        return mMoviesState.getCurrentAccount() != null;
    }

    private void markMovieRating(final int callingId, String imdbId, Rating rating) {
        if (Constants.DEBUG) {
            mLogger.d(LOG_TAG, "submitMovieRating: " + imdbId + ". " + rating.name());
        }
        executeTask(new SubmitTraktMovieRatingRunnable(callingId, imdbId, rating));
    }

    private void markMoviesSeen(final int callingId, String... ids) {
        executeTask(new MarkTraktMovieSeenRunnable(callingId, ids));

        if (isLoggedIn() && mPreferences.shouldRemoveFromWatchlistOnWatched()) {
            removeFromWatchlist(callingId, ids);
        }
    }

    private void markMoviesUnseen(final int callingId, String... ids) {
        executeTask(new MarkTraktMovieUnseenRunnable(callingId, ids));
    }

    private void populateCheckinUi(MovieCheckinUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());
        final PhilmUserProfile userProfile = mMoviesState.getUserProfile();

        if (movie != null) {
            ui.setMovie(movie);

            ui.showFacebookShare(userProfile != null && userProfile.isFacebookConnected());
            ui.showTwitterShare(userProfile != null && userProfile.isTwitterConnected());
            ui.showTumblrShare(userProfile != null && userProfile.isTumblrConnected());
            ui.showPathShare(userProfile != null && userProfile.isPathConnected());

            if (userProfile != null && userProfile.getDefaultShareMessage() != null) {
                ui.setShareText(userProfile.getDefaultShareMessage()
                        .replace(Constants.TRAKT_MESSAGE_ITEM_REPLACE, movie.getTitle()));
            }
        }
    }

    private void populateCancelCheckinUi(CancelCheckinUi ui) {
        WatchingMovie checkin = mMoviesState.getWatchingMovie();

        if (checkin != null && checkin.movie != null) {
            ui.setMovie(checkin.movie);
        }
    }

    private void populateDetailUi(MovieDetailUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());

        if (movie != null) {
            final boolean canUpdateTrakt = isLoggedIn() && movie.isLoadedFromTrakt();
            ui.setRateCircleEnabled(canUpdateTrakt);
            ui.setCollectionButtonEnabled(canUpdateTrakt);
            ui.setWatchlistButtonEnabled(canUpdateTrakt);
            ui.setToggleWatchedButtonEnabled(canUpdateTrakt);
            ui.setCheckinVisible(canUpdateTrakt && canCheckin(movie));
            ui.setCancelCheckinVisible(canUpdateTrakt && canCancelCheckin(movie));
            ui.setMovie(movie);
        }
    }

    private boolean canCancelCheckin(PhilmMovie movie) {
        WatchingMovie checkin = mMoviesState.getWatchingMovie();
        if (checkin != null) {
            // Allow cancel checkin if the last action is a checkin and movie matches
            return checkin.type == WatchingMovie.Type.CHECKIN
                    && Objects.equal(checkin.movie, movie);
        }
        return false;
    }

    private boolean canCheckin(PhilmMovie movie) {
        return mMoviesState.getWatchingMovie() == null;
    }

    private void populateMovieListUi(MovieListUi ui) {
        final MovieQueryType queryType = ui.getMovieQueryType();

        boolean requireFiltering = false;

        if (isLoggedIn()) {
            if (queryType.supportFiltering()) {
                ui.setFiltersVisibility(true);
                final Set<MovieFilter> filters = mMoviesState.getFilters();
                ui.showActiveFilters(filters);
                requireFiltering = !PhilmCollections.isEmpty(filters);
            }
        } else {
            ui.setFiltersVisibility(false);
        }

        List<PhilmMovie> items = null;
        ListItem.SectionTitle sectionTitle = null;

        List<MovieFilter> sections = null;
        List<MovieFilter> sectionProcessingOrder = null;

        switch (queryType) {
            case TRENDING:
                items = mMoviesState.getTrending();
                break;
            case POPULAR:
                MoviesState.MoviePaginatedResult popular = mMoviesState.getPopular();
                if (popular != null) {
                    items = popular.items;
                }
                break;
            case LIBRARY:
                items = mMoviesState.getLibrary();
                break;
            case WATCHLIST:
                items = mMoviesState.getWatchlist();
                sections = Arrays.asList(MovieFilter.UPCOMING, MovieFilter.SOON,
                        MovieFilter.RELEASED, MovieFilter.SEEN);
                sectionProcessingOrder = Arrays.asList(MovieFilter.UPCOMING, MovieFilter.SOON,
                        MovieFilter.SEEN, MovieFilter.RELEASED);
                break;
            case SEARCH_MOVIES:
                MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
                if (searchResult != null && searchResult.movies != null) {
                    items = searchResult.movies.items;
                }
                break;
            case NOW_PLAYING:
                MoviesState.MoviePaginatedResult nowPlaying = mMoviesState.getNowPlaying();
                if (nowPlaying != null) {
                    items = nowPlaying.items;
                }
                break;
            case UPCOMING:
                MoviesState.MoviePaginatedResult upcoming = mMoviesState.getUpcoming();
                if (upcoming != null) {
                    items = upcoming.items;
                }
                break;
            case RECOMMENDED:
                items = mMoviesState.getRecommended();
                break;
            case RELATED:
                PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());
                if (movie != null) {
                    items = movie.getRelated();
                }
                sectionTitle = ListItem.SectionTitle.RELATED;
                break;
        }

        if (requireFiltering && !PhilmCollections.isEmpty(items)) {
            items = filterMovies(items);
        }

        if (items == null) {
            ui.setItems(null);
        } else if (PhilmCollections.isEmpty(sections)) {
            ui.setItems(createListItemList(sectionTitle, items));

            if (isLoggedIn()) {
                ui.allowedBatchOperations(MovieOperation.MARK_SEEN,
                        MovieOperation.ADD_TO_COLLECTION, MovieOperation.ADD_TO_WATCHLIST);
            } else {
                ui.disableBatchOperations();
            }
        } else {
            ui.setItems(createSectionedListItemList(items, sections, sectionProcessingOrder));
        }
    }

    private void populatePersonListUi(PersonListUi ui) {
        switch (ui.getMovieQueryType()) {
            case SEARCH_PEOPLE:
                MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
                if (searchResult != null && searchResult.people != null) {
                    ui.setItems(createListItemList(null, searchResult.people.items));
                }
                break;
        }
    }

    private void populateMovieCreditListUi(MovieCreditListUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());

        switch (ui.getMovieQueryType()) {
            case MOVIE_CAST:
                if (movie != null) {
                    updateDisplayTitle(movie.getTitle());
                    if (!PhilmCollections.isEmpty(movie.getCast())) {
                        ui.setItems(createListItemList(
                                ListItem.SectionTitle.MOVIE_CAST, movie.getCast()));
                    }
                }
                break;
            case MOVIE_CREW:
                if (movie != null) {
                    updateDisplayTitle(movie.getTitle());
                    if (!PhilmCollections.isEmpty(movie.getCrew())) {
                        ui.setItems(createListItemList(
                                ListItem.SectionTitle.MOVIE_CREW, movie.getCrew()));
                    }
                }
                break;
        }
    }

    private void populatePersonCreditListUi(PersonCreditListUi ui) {
        final PhilmPerson person = mMoviesState.getPerson(ui.getRequestParameter());

        switch (ui.getMovieQueryType()) {
            case PERSON_CREDITS_CAST:
                if (person != null) {
                    updateDisplayTitle(person.getName());
                    if (!PhilmCollections.isEmpty(person.getCastCredits())) {
                        ui.setItems(createListItemList(ListItem.SectionTitle.MOVIE_CAST,
                                person.getCastCredits()));
                    }
                }
                break;
            case PERSON_CREDITS_CREW:
                if (person != null) {
                    updateDisplayTitle(person.getName());
                    if (!PhilmCollections.isEmpty(person.getCrewCredits())) {
                        ui.setItems(createListItemList(ListItem.SectionTitle.MOVIE_CREW,
                                person.getCrewCredits()));
                    }
                }
                break;
        }
    }

    private void populateMovieDiscoverUi(MovieDiscoverUi ui) {
        if (isLoggedIn()) {
            ui.setTabs(DiscoverTab.POPULAR, DiscoverTab.IN_THEATRES, DiscoverTab.UPCOMING,
                    DiscoverTab.RECOMMENDED);
        } else {
            ui.setTabs(DiscoverTab.POPULAR, DiscoverTab.IN_THEATRES, DiscoverTab.UPCOMING);
        }
    }

    private void populatePersonUi(PersonUi ui) {
        final PhilmPerson person = mMoviesState.getPerson(ui.getRequestParameter());
        if (person != null) {
            ui.setPerson(person);
        }
    }

    private void populateRateUi(MovieRateUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());
        ui.setMovie(movie);
        ui.setMarkMovieWatchedCheckboxVisible(!movie.isWatched());
    }

    private void populateSearchUi(MainSearchUi ui) {
        ui.setSearchResult(mMoviesState.getSearchResult());
    }

    private void populateSearchMovieUi(SearchMovieUi ui) {
        MoviesState.SearchResult result = mMoviesState.getSearchResult();
        updateDisplayTitle(result != null ? result.query : null);

        // Now carry on with list ui population
        populateMovieListUi(ui);
    }

    private void populateSearchPersonUi(SearchPersonUi ui) {
        MoviesState.SearchResult result = mMoviesState.getSearchResult();
        updateDisplayTitle(result != null ? result.query : null);

        // Now carry on with list ui population
        populatePersonListUi(ui);
    }

    private void populateStateFromDb() {
        if (PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            mDbHelper.getLibrary(new LibraryDbLoadCallback());
        }
        if (PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            mDbHelper.getWatchlist(new WatchlistDbLoadCallback());
        }
    }

    private void prefetchLibraryIfNeeded() {
        MovieUi ui = getMovieUiAttached(MovieQueryType.LIBRARY);
        fetchLibraryIfNeeded(ui != null ? getId(ui) : 0);
    }

    private void prefetchWatchlistIfNeeded() {
        MovieUi ui = getMovieUiAttached(MovieQueryType.WATCHLIST);
        fetchWatchlistIfNeeded(ui != null ? getId(ui) : 0);
    }

    private void removeFromCollection(final int callingId, String... ids) {
        executeTask(new RemoveFromTraktCollectionRunnable(callingId, ids));
    }

    private void removeFromWatchlist(final int callingId, String... ids) {
        executeTask(new RemoveFromTraktWatchlistRunnable(callingId, ids));
    }

    private void removeMutuallyExclusiveFilters(final MovieFilter filter) {
        List<MovieFilter> mutuallyExclusives = filter.getMutuallyExclusiveFilters();
        if (!PhilmCollections.isEmpty(mutuallyExclusives)) {
            for (MovieFilter mutualFilter : mutuallyExclusives) {
                mMoviesState.getFilters().remove(mutualFilter);
            }
        }
    }

    public interface Filter<T> {

        boolean isFiltered(T item);

        void sortListItems(List<ListItem<T>> items);

        ListItem.SectionTitle getSectionTitle();

    }

    public static enum MovieFilter implements Filter<PhilmMovie> {
        /**
         * Filters {@link PhilmMovie} that are in the user's collection.
         */
        COLLECTION,

        /**
         * Filters {@link PhilmMovie} that have been watched by the user.
         */
        SEEN,

        /**
         * Filters {@link PhilmMovie} that have not been watched by the user.
         */
        UNSEEN,

        /**
         * Filters {@link PhilmMovie} that have not been released yet.
         */
        NOT_RELEASED,

        /**
         * Filters {@link PhilmMovie} that have already been released.
         */
        RELEASED,

        /**
         * Filters {@link PhilmMovie} that are unreleased, and will be released in the far future.
         */
        UPCOMING,

        /**
         * Filters {@link PhilmMovie} that are unreleased, and will be released in the near future.
         */
        SOON,

        /**
         * Filters {@link PhilmMovie} which are highly rated, either by the user or the public.
         */
        HIGHLY_RATED;

        @Override
        public boolean isFiltered(PhilmMovie movie) {
            Preconditions.checkNotNull(movie, "movie cannot be null");

            switch (this) {
                case COLLECTION:
                    return movie.inCollection();
                case SEEN:
                    return movie.isWatched();
                case UNSEEN:
                    return !movie.isWatched();
                case NOT_RELEASED:
                    return isInFuture(movie.getReleasedTime());
                case UPCOMING:
                    return isAfterThreshold(movie.getReleasedTime(),
                            Constants.FUTURE_SOON_THRESHOLD);
                case SOON:
                    return isInFuture(movie.getReleasedTime()) && isBeforeThreshold(
                            movie.getReleasedTime(), Constants.FUTURE_SOON_THRESHOLD);
                case RELEASED:
                    return isInPast(movie.getReleasedTime());
                case HIGHLY_RATED:
                    return Math.max(movie.getTraktRatingPercent(), movie.getUserRating() * 10)
                            >= Constants.FILTER_HIGHLY_RATED;
            }
            return false;
        }

        public List<MovieFilter> getMutuallyExclusiveFilters() {
            switch (this) {
                case SEEN:
                    return Arrays.asList(UNSEEN);
                case UNSEEN:
                    return Arrays.asList(SEEN);
            }
            return null;
        }

        public void sortListItems(List<ListItem<PhilmMovie>> items) {
            switch (this) {
                default:
                    Collections.sort(items, PhilmMovie.COMPARATOR_LIST_ITEM_DATE_ASC);
                    break;
            }
        }

        @Override
        public ListItem.SectionTitle getSectionTitle() {
            switch (this) {
                case UPCOMING:
                    return ListItem.SectionTitle.UPCOMING;
                case SOON:
                    return ListItem.SectionTitle.SOON;
                case RELEASED:
                    return ListItem.SectionTitle.RELEASED;
                case SEEN:
                    return ListItem.SectionTitle.SEEN;
            }
            return null;
        }
    }

    public static enum MovieQueryType {
        TRENDING, POPULAR, LIBRARY, WATCHLIST, DETAIL, NOW_PLAYING, UPCOMING, RECOMMENDED,
        SEARCH, SEARCH_MOVIES, SEARCH_PEOPLE,
        RELATED, MOVIE_CAST, MOVIE_CREW,
        PERSON_DETAIL, PERSON_CREDITS_CAST, PERSON_CREDITS_CREW,
        NONE;

        public boolean requireLogin() {
            switch (this) {
                case WATCHLIST:
                case LIBRARY:
                case RECOMMENDED:
                    return true;
                default:
                    return false;
            }
        }

        public boolean supportFiltering() {
            switch (this) {
                case LIBRARY:
                case TRENDING:
                    return true;
                default:
                    return false;
            }
        }

        public boolean showUpNavigation() {
            switch (this) {
                case DETAIL:
                case RELATED:
                case MOVIE_CAST:
                case MOVIE_CREW:
                case PERSON_DETAIL:
                case PERSON_CREDITS_CAST:
                case PERSON_CREDITS_CREW:
                case SEARCH_MOVIES:
                case SEARCH_PEOPLE:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static enum MovieOperation {
        MARK_SEEN, ADD_TO_COLLECTION, ADD_TO_WATCHLIST
    }

    public static enum DiscoverTab {
        POPULAR, IN_THEATRES, UPCOMING, RECOMMENDED
    }

    public static enum PersonTab {
        CREDITS_CAST, CREDIT_CREW
    }

    public interface MovieUi extends BaseUiController.Ui<MovieUiCallbacks> {

        void showError(NetworkError error);

        void showLoadingProgress(boolean visible);

        void showSecondaryLoadingProgress(boolean visible);

        MovieQueryType getMovieQueryType();

        String getRequestParameter();
    }

    public interface BaseMovieListUi<E> extends MovieUi {
        void setItems(List<ListItem<E>> items);
    }

    public interface MovieListUi extends BaseMovieListUi<PhilmMovie> {
        void setFiltersVisibility(boolean visible);

        void showActiveFilters(Set<MovieFilter> filters);

        void allowedBatchOperations(MovieOperation... operations);

        void disableBatchOperations();
    }

    public interface MovieCreditListUi extends BaseMovieListUi<PhilmMovieCredit> {
    }

    public interface PersonCreditListUi extends BaseMovieListUi<PhilmPersonCredit> {
    }

    public interface MainSearchUi {
        void setSearchResult(MoviesState.SearchResult result);
    }

    public interface PersonListUi extends BaseMovieListUi<PhilmPerson> {}

    public interface SearchPersonUi extends PersonListUi {}

    public interface SearchMovieUi extends MovieListUi {}

    public interface MovieDetailUi extends MovieUi {

        void setMovie(PhilmMovie movie);

        void setToggleWatchedButtonEnabled(boolean enabled);

        void setCollectionButtonEnabled(boolean enabled);

        void setWatchlistButtonEnabled(boolean enabled);

        void setRateCircleEnabled(boolean enabled);

        void setCheckinVisible(boolean visible);

        void setCancelCheckinVisible(boolean visible);

    }

    public interface MovieRateUi extends MovieUi {

        void setMarkMovieWatchedCheckboxVisible(boolean visible);

        void setMovie(PhilmMovie movie);
    }

    public interface MovieDiscoverUi extends MovieUi {
        void setTabs(DiscoverTab... tabs);
    }

    public interface PersonUi extends MovieUi {
        void setPerson(PhilmPerson person);
    }

    public interface MovieCheckinUi extends MovieUi {
        void setMovie(PhilmMovie movie);
        void setShareText(String shareText);
        void showFacebookShare(boolean show);
        void showTwitterShare(boolean show);
        void showPathShare(boolean show);
        void showTumblrShare(boolean show);
    }

    public interface CancelCheckinUi extends MovieUi {
        void setMovie(PhilmMovie movie);
    }

    public interface MovieUiCallbacks {

        void onTitleChanged();

        void setTitleTextColor(int textColor);

        void addFilter(MovieFilter filter);

        void removeFilter(MovieFilter filter);

        void clearFilters();

        void refresh();

        void showMovieDetail(PhilmMovie movie);

        void showMovieDetail(PhilmPersonCredit credit);

        void toggleMovieSeen(PhilmMovie movie);

        void toggleInWatchlist(PhilmMovie movie);

        void toggleInCollection(PhilmMovie movie);

        void setMoviesSeen(List<PhilmMovie> movies, boolean seen);

        void setMoviesInWatchlist(List<PhilmMovie> movies, boolean inWatchlist);

        void setMoviesInCollection(List<PhilmMovie> movies, boolean inCollection);

        void search(String query);

        void clearSearch();

        void showRateMovie(PhilmMovie movie);

        void submitRating(PhilmMovie movie, Rating rating);

        void onScrolledToBottom();

        void showRelatedMovies(PhilmMovie movie);

        void showCastList(PhilmMovie movie);

        void showCrewList(PhilmMovie movie);

        void checkin(PhilmMovie movie, String message, boolean shareFacebook, boolean shareTwitter,
                boolean sharePath, boolean shareTumblr);

        void cancelCurrentCheckin();

        void requestCancelCurrentCheckin();

        void requestCheckin(PhilmMovie movie);

        void showPersonDetail(PhilmPerson person);

        void showPersonCastCredits(PhilmPerson person);

        void showPersonCrewCredits(PhilmPerson person);

        void showPeopleSearchResults();

        void showMovieSearchResults();

        void playTrailer(PhilmTrailer trailer);
    }

    private class LibraryDbLoadCallback
            implements AsyncDatabaseHelper.Callback<List<PhilmMovie>> {

        @Override
        public void onFinished(List<PhilmMovie> result) {
            mMoviesState.setLibrary(result);
            if (!PhilmCollections.isEmpty(result)) {
                for (PhilmMovie movie : result) {
                    mMoviesState.putMovie(movie);
                }
            }
            mPopulatedLibraryFromDb = true;

            prefetchLibraryIfNeeded();
        }
    }

    private class WatchlistDbLoadCallback
            implements AsyncDatabaseHelper.Callback<List<PhilmMovie>> {

        @Override
        public void onFinished(List<PhilmMovie> result) {
            mMoviesState.setWatchlist(result);
            if (!PhilmCollections.isEmpty(result)) {
                for (PhilmMovie movie : result) {
                    mMoviesState.putMovie(movie);
                }
            }
            mPopulatedWatchlistFromDb = true;

            prefetchWatchlistIfNeeded();
        }
    }
}
