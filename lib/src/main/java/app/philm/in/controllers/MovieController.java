package app.philm.in.controllers;

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.enumerations.Rating;
import com.squareup.otto.Subscribe;
import com.uwetrottmann.tmdb.Tmdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.modules.qualifiers.GeneralPurpose;
import app.philm.in.network.NetworkError;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.tasks.AddToTraktCollectionRunnable;
import app.philm.in.tasks.AddToTraktWatchlistRunnable;
import app.philm.in.tasks.BaseMovieRunnable;
import app.philm.in.tasks.FetchTmdbConfigurationRunnable;
import app.philm.in.tasks.FetchTmdbDetailMovieRunnable;
import app.philm.in.tasks.FetchTmdbMoviesReleasesRunnable;
import app.philm.in.tasks.FetchTmdbNowPlayingRunnable;
import app.philm.in.tasks.FetchTmdbPopularRunnable;
import app.philm.in.tasks.FetchTmdbRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTmdbSearchQueryRunnable;
import app.philm.in.tasks.FetchTmdbUpcomingRunnable;
import app.philm.in.tasks.FetchTraktDetailMovieRunnable;
import app.philm.in.tasks.FetchTraktLibraryRunnable;
import app.philm.in.tasks.FetchTraktRecommendationsRunnable;
import app.philm.in.tasks.FetchTraktRelatedMoviesRunnable;
import app.philm.in.tasks.FetchTraktTrendingRunnable;
import app.philm.in.tasks.FetchTraktWatchlistRunnable;
import app.philm.in.tasks.MarkTraktMovieSeenRunnable;
import app.philm.in.tasks.MovieTaskCallback;
import app.philm.in.tasks.RemoveFromTraktCollectionRunnable;
import app.philm.in.tasks.RemoveFromTraktWatchlistRunnable;
import app.philm.in.tasks.SubmitTraktMovieRatingRunnable;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.FileManager;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.Injector;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;

import static app.philm.in.util.TimeUtils.isAfterThreshold;
import static app.philm.in.util.TimeUtils.isBeforeThreshold;
import static app.philm.in.util.TimeUtils.isInFuture;
import static app.philm.in.util.TimeUtils.isInPast;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    private static final String LOG_TAG = MovieController.class.getSimpleName();

    private static final int TMDB_FIRST_PAGE = 1;

    private final MoviesState mMoviesState;
    private final BackgroundExecutor mExecutor;
    private final Trakt mTraktClient;
    private final Tmdb mTmdbClient;
    private final AsyncDatabaseHelper mDbHelper;
    private final Logger mLogger;
    private final CountryProvider mCountryProvider;
    private final ImageHelper mImageHelper;
    private final FileManager mFileManager;
    private final Injector mInjector;

    private boolean mPopulatedLibraryFromDb = false;
    private boolean mPopulatedWatchlistFromDb = false;

    @Inject
    public MovieController(
            MoviesState movieState,
            Trakt traktClient,
            Tmdb tmdbClient,
            @GeneralPurpose BackgroundExecutor executor,
            AsyncDatabaseHelper dbHelper,
            Logger logger,
            CountryProvider countryProvider,
            ImageHelper imageHelper,
            FileManager fileManager,
            Injector injector) {
        super();
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");
        mTraktClient = Preconditions.checkNotNull(traktClient, "trackClient cannot be null");
        mTmdbClient = Preconditions.checkNotNull(tmdbClient, "tmdbClient cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
        mLogger = Preconditions.checkNotNull(logger, "logger cannot be null");
        mCountryProvider = Preconditions.checkNotNull(countryProvider,
                "countryProvider cannot be null");
        mImageHelper = Preconditions.checkNotNull(imageHelper, "imageHelper cannot be null");
        mFileManager = Preconditions.checkNotNull(fileManager, "fileManager cannot be null");
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
        mMoviesState.getImdbIdMovies().clear();
        mMoviesState.getTmdbIdMovies().clear();

        // TODO: Clear Database Too

        populateUis();
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

    @Override
    protected void onInited() {
        super.onInited();
        populateStateFromDb();
        mMoviesState.registerForEvents(this);

        if (mMoviesState.getTmdbConfiguration() == null) {
            fetchTmdbConfiguration();
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

            @Override
            public void onTitleChanged(String newTitle) {
                updateDisplayTitle(newTitle);
            }

            @Override
            public void addFilter(Filter filter) {
                if (mMoviesState.getFilters().add(filter)) {
                    removeMutuallyExclusiveFilters(filter);
                    populateUis();
                }
            }

            @Override
            public void removeFilter(Filter filter) {
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
                        fetchTrending(ui);
                        break;
                    case LIBRARY:
                        fetchLibrary(ui);
                        break;
                    case WATCHLIST:
                        fetchWatchlist(ui);
                        break;
                    case DETAIL:
                        fetchDetailMovie(ui, ui.getRequestParameter());
                        break;
                    case POPULAR:
                        fetchPopular(ui);
                        break;
                    case RECOMMENDED:
                        fetchRecommended(ui);
                        break;
                    case UPCOMING:
                        fetchUpcoming(ui);
                        break;
                    case NOW_PLAYING:
                        fetchNowPlaying(ui);
                        break;
                }
            }

            @Override
            public void showMovieDetail(PhilmMovie movie) {
                Display display = getDisplay();
                if (display != null) {
                    if (TextUtils.isEmpty(movie.getTraktId())) {
                        // TODO: Should be do something better here
                    } else {
                        display.showMovieDetailFragment(movie.getTraktId());
                    }
                }
            }

            @Override
            public void toggleMovieSeen(PhilmMovie movie) {
                if (movie.isWatched()) {
                    markMoviesUnseen(ui, movie.getTraktId());
                } else {
                    markMoviesSeen(ui, movie.getTraktId());
                }
            }

            @Override
            public void toggleInWatchlist(PhilmMovie movie) {
                if (movie.inWatchlist()) {
                    removeFromWatchlist(ui, movie.getTraktId());
                } else {
                    addToWatchlist(ui, movie.getTraktId());
                }
            }

            @Override
            public void toggleInCollection(PhilmMovie movie) {
                if (movie.inCollection()) {
                    removeFromCollection(ui, movie.getTraktId());
                } else {
                    addToCollection(ui, movie.getTraktId());
                }
            }

            @Override
            public void setMoviesInCollection(List<PhilmMovie> movies, boolean inCollection) {
                final ArrayList<String> ids = new ArrayList<String>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (inCollection != movie.inCollection()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (inCollection) {
                    addToCollection(ui, ids.toArray(idsArray));
                } else {
                    removeFromCollection(ui, ids.toArray(idsArray));
                }
            }

            @Override
            public void setMoviesInWatchlist(List<PhilmMovie> movies, boolean inWatchlist) {
                final ArrayList<String> ids = new ArrayList<String>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (inWatchlist != movie.inWatchlist()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (inWatchlist) {
                    addToWatchlist(ui, ids.toArray(idsArray));
                } else {
                    removeFromWatchlist(ui, ids.toArray(idsArray));
                }
            }

            @Override
            public void setMoviesSeen(List<PhilmMovie> movies, boolean seen) {
                final ArrayList<String> ids = new ArrayList<String>(movies.size());
                for (PhilmMovie movie : movies) {
                    if (seen != movie.isWatched()) {
                        ids.add(movie.getTraktId());
                    }
                }

                final String[] idsArray = new String[ids.size()];
                if (seen) {
                    markMoviesSeen(ui, ids.toArray(idsArray));
                } else {
                    markMoviesUnseen(ui, ids.toArray(idsArray));
                }
            }

            @Override
            public void search(String query) {
                fetchSearchResults(ui, query);
            }

            @Override
            public void clearSearch() {
                mMoviesState.setSearchResult(null);
            }

            @Override
            public void showRateMovie(PhilmMovie movie) {
                Display display = getDisplay();
                if (display != null) {
                    display.showRateMovieFragment(movie.getTraktId());
                }
            }

            @Override
            public void submitRating(PhilmMovie movie, Rating rating) {
                markMovieRating(ui, movie.getTraktId(), rating);
            }

            @Override
            public void onScrolledToBottom() {
                MoviesState.MoviePaginatedResult result;

                switch (ui.getMovieQueryType()) {
                    case POPULAR:
                        result = mMoviesState.getPopular();
                        if (canFetchNextPage(result)) {
                            fetchPopular(ui, result.page + 1);
                        }
                        break;
                    case SEARCH:
                        MoviesState.SearchPaginatedResult searchResult = mMoviesState.getSearchResult();
                        if (canFetchNextPage(searchResult)) {
                            fetchSearchResults(ui, searchResult.query, searchResult.page + 1);
                        }
                        break;
                    case UPCOMING:
                        result = mMoviesState.getUpcoming();
                        if (canFetchNextPage(result)) {
                            fetchUpcoming(ui, result.page + 1);
                        }
                        break;
                }
            }

            private boolean canFetchNextPage(MoviesState.MoviePaginatedResult paginatedResult) {
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
            display.showUpNavigation(ui instanceof MovieDetailUi);
        }

        switch (queryType) {
            case TRENDING:
                fetchTrendingIfNeeded(ui);
                break;
            case POPULAR:
                fetchPopularIfNeeded(ui);
                break;
            case LIBRARY:
                fetchLibraryIfNeeded(ui);
                break;
            case WATCHLIST:
                fetchWatchlistIfNeeded(ui);
                break;
            case DETAIL:
                fetchDetailMovieIfNeeded(ui, ui.getRequestParameter());
                break;
            case NOW_PLAYING:
                fetchNowPlayingIfNeeded(ui);
                break;
            case UPCOMING:
                fetchUpcomingIfNeeded(ui);
                break;
            case RECOMMENDED:
                fetchRecommendedIfNeeded(ui);
                break;
        }
    }

    @Override
    protected void populateUi(final MovieUi ui) {
        if (!isLoggedIn() && ui.getMovieQueryType().requireLogin()) {
            ui.showError(NetworkError.UNAUTHORIZED);
            return;
        }

        if (mMoviesState.getTmdbConfiguration() == null) {
            mLogger.i(LOG_TAG, "TMDB Configuration not downloaded yet.");
            return;
        }

        if (ui instanceof SearchMovieUi) {
            populateSearchUi((SearchMovieUi) ui);
        } else if (ui instanceof MovieListUi) {
            populateListUi((MovieListUi) ui);
        } else if (ui instanceof MovieDetailUi) {
            populateDetailUi((MovieDetailUi) ui);
        } else if (ui instanceof MovieRateUi) {
            populateRateUi((MovieRateUi) ui);
        } else if (ui instanceof MovieDiscoverUi) {
            populateMovieDiscoverUi((MovieDiscoverUi) ui);
        }
    }

    private void addToCollection(MovieUi ui, String... ids) {
        executeTask(new AddToTraktCollectionRunnable(ids), ui);
    }

    private void addToWatchlist(MovieUi ui, String... ids) {
        executeTask(new AddToTraktWatchlistRunnable(ids), ui);
    }

    private void checkDetailMovieResult(MovieUi ui, PhilmMovie movie) {
        if (PhilmCollections.isEmpty(movie.getRelated())) {
            fetchRelatedMovies(ui, movie);
        }

        if (TextUtils.isEmpty(movie.getReleaseCountryCode())) {
            fetchMovieReleases(ui, movie.getTmdbId());
        }
    }

    private void checkPhilmState(PhilmMovie movie) {
        final List<PhilmMovie> library = mMoviesState.getLibrary();
        final List<PhilmMovie> watchlist = mMoviesState.getWatchlist();

        if (!PhilmCollections.isEmpty(library)) {
            final boolean shouldBeInLibrary = movie.isWatched() || movie.inCollection();

            if (shouldBeInLibrary != library.contains(movie)) {
                if (shouldBeInLibrary) {
                    library.add(movie);
                    Collections.sort(library, PhilmMovie.COMPARATOR);
                } else {
                    library.remove(movie);
                }
            }
        }

        if (!PhilmCollections.isEmpty(watchlist)) {
            final boolean shouldBeInWatchlist = movie.inWatchlist();
            if (shouldBeInWatchlist != watchlist.contains(movie)) {
                if (shouldBeInWatchlist) {
                    watchlist.add(movie);
                    Collections.sort(watchlist, PhilmMovie.COMPARATOR);
                } else {
                    watchlist.remove(movie);
                }
            }
        }
    }

    private List<ListItem<PhilmMovie>> createListItemList(final List<PhilmMovie> items) {
        Preconditions.checkNotNull(items, "items cannot be null");

        ArrayList<ListItem<PhilmMovie>> movies = new ArrayList<ListItem<PhilmMovie>>(items.size());
        for (PhilmMovie movie : items) {
            movies.add(new ListItem<PhilmMovie>(movie));
        }
        return movies;
    }

    private List<ListItem<PhilmMovie>> createSectionedListItemList(
            final List<PhilmMovie> items,
            final List<Filter> sections,
            List<Filter> sectionProcessingOrder) {
        Preconditions.checkNotNull(items, "items cannot be null");
        Preconditions.checkNotNull(sections, "sections cannot be null");

        if (sectionProcessingOrder != null) {
            Preconditions.checkArgument(sections.size() == sectionProcessingOrder.size(),
                    "sections and sectionProcessingOrder must be the same size");
        } else {
            sectionProcessingOrder = sections;
        }

        final List<ListItem<PhilmMovie>> result = new ArrayList<ListItem<PhilmMovie>>(items.size());
        final HashSet<PhilmMovie> movies = new HashSet<PhilmMovie>(items);

        Map<Filter, List<ListItem<PhilmMovie>>> sectionsItemLists = null;

        for (MovieController.Filter filter : sectionProcessingOrder) {
            List<ListItem<PhilmMovie>> sectionItems = null;

            for (Iterator<PhilmMovie> i = movies.iterator(); i.hasNext(); ) {
                PhilmMovie movie = i.next();
                if (filter.isMovieFiltered(movie)) {
                    if (sectionItems == null) {
                        sectionItems = new ArrayList<ListItem<PhilmMovie>>();
                        // Now add Title
                        sectionItems.add(new ListItem<PhilmMovie>(filter));
                    }
                    sectionItems.add(new ListItem<PhilmMovie>(movie));
                    i.remove();
                }
            }

            if (!PhilmCollections.isEmpty(sectionItems)) {
                if (sectionsItemLists == null) {
                    sectionsItemLists = new HashMap<Filter, List<ListItem<PhilmMovie>>>();
                }
                sectionsItemLists.put(filter, sectionItems);
            }
        }

        if (sectionsItemLists != null) {
            for (MovieController.Filter filter : sections) {
                if (sectionsItemLists.containsKey(filter)) {
                    result.addAll(sectionsItemLists.get(filter));
                }
            }
        }

        return result;
    }

//    private <R> void executeTask(BaseMovieRunnable<R> task) {
//        executeTask(task, (MovieTaskCallback) null);
//    }

    private <R> void executeTask(BaseMovieRunnable<R> task, MovieUi ui) {
        Preconditions.checkNotNull(ui, "ui cannot be null");
        executeTask(task, new BaseTaskCallback(ui));
    }

    private <R> void executeTask(BaseMovieRunnable<R> task, MovieTaskCallback callback) {
        if (callback != null) {
            task.setCallback(callback);
        }

        mInjector.inject(task);

        mExecutor.execute(task);
    }

    private void fetchDetailMovie(MovieUi ui, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = mMoviesState.getMovie(id);
        if (movie != null && movie.getTmdbId() != null) {
            fetchDetailMovieFromTmdb(ui, movie.getTmdbId());

            if (isLoggedIn()) {
                fetchDetailMovieFromTrakt(ui, id);
            }
        } else {
            fetchDetailMovieFromTrakt(ui, id);
        }
    }

    private void fetchDetailMovieIfNeeded(MovieUi ui, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie cached = mMoviesState.getMovie(id);
        if (cached == null || requireMovieDetailFetch(cached)) {
            fetchDetailMovie(ui, id);
        } else {
            checkDetailMovieResult(ui, cached);
        }
    }

    private void fetchDetailMovieFromTrakt(MovieUi ui, String id) {
        Preconditions.checkNotNull(id, "id cannot be null");
        executeTask(new FetchTraktDetailMovieRunnable(id), ui);
    }

    private void fetchDetailMovieFromTmdb(MovieUi ui, int id) {
        executeTask(new FetchTmdbDetailMovieRunnable(id), ui);
    }

    private void fetchLibrary(MovieUi ui) {
        if (isLoggedIn()) {
            executeTask(new FetchTraktLibraryRunnable(mMoviesState.getUsername()), ui);
        }
    }

    private void fetchLibraryIfNeeded(MovieUi ui) {
        if (mPopulatedLibraryFromDb && PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            fetchLibrary(ui);
        }
    }

    private void fetchMovieReleases(MovieUi ui, int tmdbId) {
        executeTask(new FetchTmdbMoviesReleasesRunnable(tmdbId), ui);
    }

    private void fetchNowPlaying(MovieUi ui) {
        mMoviesState.setNowPlaying(null);
        fetchNowPlaying(ui, TMDB_FIRST_PAGE);
    }

    private void fetchNowPlaying(MovieUi ui, final int page) {
        executeTask(new FetchTmdbNowPlayingRunnable(page), ui);
    }

    private void fetchNowPlayingIfNeeded(MovieUi ui) {
        MoviesState.MoviePaginatedResult nowPlaying = mMoviesState.getNowPlaying();
        if (nowPlaying == null || PhilmCollections.isEmpty(nowPlaying.items)) {
            fetchNowPlaying(ui, TMDB_FIRST_PAGE);
        }
    }

    private void fetchPopular(MovieUi ui) {
        mMoviesState.setPopular(null);
        fetchPopular(ui, TMDB_FIRST_PAGE);
    }

    private void fetchPopular(MovieUi ui, final int page) {
        executeTask(new FetchTmdbPopularRunnable(page), ui);
    }

    private void fetchPopularIfNeeded(MovieUi ui) {
        MoviesState.MoviePaginatedResult popular = mMoviesState.getPopular();
        if (popular == null || PhilmCollections.isEmpty(popular.items)) {
            fetchPopular(ui, TMDB_FIRST_PAGE);
        }
    }

    private void fetchUpcoming(MovieUi ui) {
        mMoviesState.setUpcoming(null);
        fetchUpcoming(ui, TMDB_FIRST_PAGE);
    }

    private void fetchUpcoming(MovieUi ui, final int page) {
        executeTask(new FetchTmdbUpcomingRunnable(page), ui);
    }

    private void fetchUpcomingIfNeeded(MovieUi ui) {
        MoviesState.MoviePaginatedResult upcoming = mMoviesState.getUpcoming();
        if (upcoming == null || PhilmCollections.isEmpty(upcoming.items)) {
            fetchUpcoming(ui, TMDB_FIRST_PAGE);
        }
    }

    private void fetchRelatedMovies(MovieUi ui, PhilmMovie movie) {
        if (movie.getTmdbId() != null) {
            executeTask(new FetchTmdbRelatedMoviesRunnable(movie.getTmdbId()),
                    new RelatedMoviesTaskCallback(ui));
        } else if (!TextUtils.isEmpty(movie.getImdbId())) {
            executeTask(new FetchTraktRelatedMoviesRunnable(movie.getImdbId()),
                    new RelatedMoviesTaskCallback(ui));
        }
    }

    private void fetchSearchResults(MovieUi ui, String query) {
        mMoviesState.setSearchResult(null);
        fetchSearchResults(ui, query, TMDB_FIRST_PAGE);
    }

    private void fetchSearchResults(MovieUi ui, String query, int page) {
        executeTask(new FetchTmdbSearchQueryRunnable(query, page), ui);
    }

    private void fetchRecommended(MovieUi ui) {
        Preconditions.checkState(isLoggedIn(), "Must be logged in to trakt for recommendations");
        executeTask(new FetchTraktRecommendationsRunnable(), ui);
    }

    private void fetchRecommendedIfNeeded(MovieUi ui) {
        if (PhilmCollections.isEmpty(mMoviesState.getRecommended())) {
            fetchRecommended(ui);
        }
    }

    private void fetchTmdbConfiguration() {
        FetchTmdbConfigurationRunnable task = new FetchTmdbConfigurationRunnable();
        mInjector.inject(task);
        mExecutor.execute(task);
    }

    private void fetchTrending(MovieUi ui) {
        executeTask(new FetchTraktTrendingRunnable(), ui);
    }

    private void fetchTrendingIfNeeded(MovieUi ui) {
        if (PhilmCollections.isEmpty(mMoviesState.getTrending())) {
            fetchTrending(ui);
        }
    }

    private void fetchWatchlist(MovieUi ui) {
        if (isLoggedIn()) {
            executeTask(new FetchTraktWatchlistRunnable(mMoviesState.getUsername()), ui);
        }
    }

    private void fetchWatchlistIfNeeded(MovieUi ui) {
        if (mPopulatedWatchlistFromDb && PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            fetchWatchlist(ui);
        }
    }

    private List<PhilmMovie> filterMovies(List<PhilmMovie> movies) {
        Preconditions.checkNotNull(movies, "movies cannot be null");

        final Set<Filter> filters = mMoviesState.getFilters();
        Preconditions.checkNotNull(filters, "filters cannot be null");
        Preconditions.checkState(!filters.isEmpty(), "filters cannot be empty");

        ArrayList<PhilmMovie> filteredMovies = new ArrayList<PhilmMovie>();
        for (PhilmMovie movie : movies) {
            boolean filtered = true;
            for (Filter filter : filters) {
                if (!filter.isMovieFiltered(movie)) {
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

    private void markMovieRating(MovieUi ui, String imdbId, Rating rating) {
        mLogger.d(LOG_TAG, "submitMovieRating: " + imdbId + ". " + rating.name());
        executeTask(new SubmitTraktMovieRatingRunnable(imdbId, rating), ui);
    }

    private void markMoviesSeen(MovieUi ui, String... ids) {
        executeTask(new MarkTraktMovieSeenRunnable(ids), ui);
    }

    private void markMoviesUnseen(MovieUi ui, String... ids) {
        executeTask(new MarkTraktMovieSeenRunnable(ids), ui);
    }

    private void populateDetailUi(MovieDetailUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());

        final boolean isLoggedIn = isLoggedIn();
        ui.setRateCircleEnabled(isLoggedIn);
        ui.setCollectionButtonEnabled(isLoggedIn);
        ui.setWatchlistButtonEnabled(isLoggedIn);
        ui.setToggleWatchedButtonEnabled(isLoggedIn);

        ui.setMovie(movie);
    }

    private void populateListUi(MovieListUi ui) {
        final MovieQueryType queryType = ui.getMovieQueryType();

        boolean requireFiltering = false;

        if (isLoggedIn()) {
            if (queryType.supportFiltering()) {
                ui.setFiltersVisibility(true);
                final Set<Filter> filters = mMoviesState.getFilters();
                ui.showActiveFilters(filters);
                requireFiltering = !PhilmCollections.isEmpty(filters);
            }
        } else {
            ui.setFiltersVisibility(false);
        }

        List<PhilmMovie> items = null;
        List<Filter> sections = null;
        List<Filter> sectionProcessingOrder = null;

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
                sections = Arrays.asList(Filter.UPCOMING, Filter.SOON, Filter.RELEASED,
                        Filter.SEEN);
                sectionProcessingOrder = Arrays.asList(Filter.UPCOMING, Filter.SOON, Filter.SEEN,
                        Filter.RELEASED);
                break;
            case SEARCH:
                MoviesState.MoviePaginatedResult searchResult = mMoviesState.getSearchResult();
                if (searchResult != null) {
                    items = searchResult.items;
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
        }

        if (requireFiltering && !PhilmCollections.isEmpty(items)) {
            items = filterMovies(items);
        }

        if (items == null) {
            ui.setItems(null);
        } else if (PhilmCollections.isEmpty(sections)) {
            ui.setItems(createListItemList(items));

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

    private void populateMovieDiscoverUi(MovieDiscoverUi ui) {
        if (isLoggedIn()) {
            ui.setTabs(DiscoverTab.POPULAR, DiscoverTab.IN_THEATRES, DiscoverTab.UPCOMING,
                    DiscoverTab.RECOMMENDED);
        } else {
            ui.setTabs(DiscoverTab.POPULAR, DiscoverTab.IN_THEATRES, DiscoverTab.UPCOMING);
        }
    }

    private void populateRateUi(MovieRateUi ui) {
        final PhilmMovie movie = mMoviesState.getMovie(ui.getRequestParameter());
        ui.setMovie(movie);
        ui.setMarkMovieWatchedCheckboxVisible(!movie.isWatched());
    }

    private void populateSearchUi(SearchMovieUi ui) {
        MoviesState.SearchPaginatedResult result = mMoviesState.getSearchResult();
        ui.setQuery(result != null ? result.query : null);

        // Now carry on with list ui population
        populateListUi(ui);
    }

    private void populateStateFromDb() {
        if (PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            mDbHelper.getLibrary(new LibraryDbLoadCallback());
        }
        if (PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            mDbHelper.getWatchlist(new WatchlistDbLoadCallback());
        }
    }

    private void removeFromCollection(MovieUi ui, String... ids) {
        executeTask(new RemoveFromTraktCollectionRunnable(ids), ui);
    }

    private void removeFromWatchlist(MovieUi ui, String... ids) {
        executeTask(new RemoveFromTraktWatchlistRunnable(ids), ui);
    }

    private void removeMutuallyExclusiveFilters(final Filter filter) {
        List<Filter> mutuallyExclusives = filter.getMutuallyExclusiveFilters();
        if (!PhilmCollections.isEmpty(mutuallyExclusives)) {
            for (Filter mutualFilter : mutuallyExclusives) {
                mMoviesState.getFilters().remove(mutualFilter);
            }
        }
    }

    private boolean requireMovieDetailFetch(PhilmMovie movie) {
        Preconditions.checkNotNull(movie, "movie cannot be null");
        return isAfterThreshold(movie.getLastFetchedTime(),
                Constants.STALE_MOVIE_DETAIL_THRESHOLD)
                || TextUtils.isEmpty(movie.getOverview());
    }

    public static enum Filter {
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

        public boolean isMovieFiltered(PhilmMovie movie) {
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
                    return Math.max(movie.getRatingPercent(), movie.getUserRating() * 10)
                            >= Constants.FILTER_HIGHLY_RATED;
            }
            return false;
        }

        public List<Filter> getMutuallyExclusiveFilters() {
            switch (this) {
                case SEEN:
                    return Arrays.asList(UNSEEN);
                case UNSEEN:
                    return Arrays.asList(SEEN);
            }
            return null;
        }
    }

    public static enum MovieQueryType {
        TRENDING, POPULAR, LIBRARY, WATCHLIST, DETAIL, SEARCH, NOW_PLAYING, UPCOMING, RECOMMENDED,
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
    }

    public static enum MovieOperation {
        MARK_SEEN, ADD_TO_COLLECTION, ADD_TO_WATCHLIST
    }

    public static enum DiscoverTab {
        POPULAR, IN_THEATRES, UPCOMING, RECOMMENDED;
    }

    public interface MovieUi extends BaseUiController.Ui<MovieUiCallbacks> {

        void showError(NetworkError error);

        void showLoadingProgress(boolean visible);

        MovieQueryType getMovieQueryType();

        String getRequestParameter();
    }

    public interface MovieListUi extends MovieUi {

        void setItems(List<ListItem<PhilmMovie>> items);

        void setFiltersVisibility(boolean visible);

        void showActiveFilters(Set<Filter> filters);

        void allowedBatchOperations(MovieOperation... operations);

        void disableBatchOperations();
    }

    public interface SearchMovieUi extends MovieListUi {

        void setQuery(String query);
    }

    public interface MovieDetailUi extends MovieUi {

        void setMovie(PhilmMovie movie);

        void showRelatedMoviesLoadingProgress(boolean visible);

        void setToggleWatchedButtonEnabled(boolean enabled);

        void setCollectionButtonEnabled(boolean enabled);

        void setWatchlistButtonEnabled(boolean enabled);

        void setRateCircleEnabled(boolean enabled);
    }

    public interface MovieRateUi extends MovieUi {

        void setMarkMovieWatchedCheckboxVisible(boolean visible);

        void setMovie(PhilmMovie movie);
    }

    public interface MovieDiscoverUi extends MovieUi {
        void setTabs(DiscoverTab... tabs);
    }

    public interface MovieUiCallbacks {

        void onTitleChanged(String newTitle);

        void addFilter(Filter filter);

        void removeFilter(Filter filter);

        void clearFilters();

        void refresh();

        void showMovieDetail(PhilmMovie movie);

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

            final MovieUi ui = getMovieUiAttached(MovieQueryType.LIBRARY);
            if (ui != null) {
                fetchLibraryIfNeeded(ui);
            }
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

            final MovieUi ui = getMovieUiAttached(MovieQueryType.WATCHLIST);
            if (ui != null) {
                fetchWatchlistIfNeeded(ui);
            }
        }
    }

    private class BaseTaskCallback implements MovieTaskCallback {

        final MovieUi mUi;

        BaseTaskCallback(MovieUi ui) {
            mUi = ui;
        }

        @Override
        public void showLoadingProgress(boolean show) {
            if (mUi != null) {
                mUi.showLoadingProgress(show);
            }
        }

        @Override
        public void showError(NetworkError error) {
            if (mUi != null) {
                mUi.showError(error);
            }
        }

        @Override
        public void populateUis() {
            populateUis();
        }

    }

    private class RelatedMoviesTaskCallback extends BaseTaskCallback {

        RelatedMoviesTaskCallback(MovieUi ui) {
            super(ui);
        }

        @Override
        public void showLoadingProgress(boolean show) {
            if (mUi instanceof MovieController.MovieDetailUi) {
                ((MovieController.MovieDetailUi) mUi).showRelatedMoviesLoadingProgress(show);
            }
        }

    }
}
