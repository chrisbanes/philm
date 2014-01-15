package app.philm.in.controllers;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.RatingResponse;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.enumerations.Rating;
import com.jakewharton.trakt.services.MovieService;
import com.jakewharton.trakt.services.RateService;
import com.squareup.otto.Subscribe;
import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.Configuration;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.ReleasesResult;
import com.uwetrottmann.tmdb.entities.ResultsPage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import app.philm.in.model.TmdbConfiguration;
import app.philm.in.modules.qualifiers.GeneralPurpose;
import app.philm.in.network.NetworkCallRunnable;
import app.philm.in.network.NetworkError;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.FileManager;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.TextUtils;
import retrofit.RetrofitError;

import static app.philm.in.util.TimeUtils.isAfterThreshold;
import static app.philm.in.util.TimeUtils.isBeforeThreshold;
import static app.philm.in.util.TimeUtils.isInFuture;
import static app.philm.in.util.TimeUtils.isInPast;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    private static final String LOG_TAG = MovieController.class.getSimpleName();
    private static final String FILENAME_TMDB_CONFIG = "tmdb.config";

    private static final int TMDB_FIRST_PAGE = 1;

    private final MoviesState mMoviesState;
    private final BackgroundExecutor mExecutor;
    private final Trakt mTraktClient;
    private final Tmdb mTmdbClient;
    private final AsyncDatabaseHelper mDbHelper;
    private final Logger mLogger;
    private final CountryProvider mCountryProvider;
    private final ImageHelper mImageHelper;
    private final TraktMovieEntityMapper mTraktMovieEntityMapper;
    private final TmdbMovieEntityMapper mTmdbMovieEntityMapper;
    private final FileManager mFileManager;

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
            FileManager fileManager) {
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

        mTraktMovieEntityMapper = new TraktMovieEntityMapper();
        mTmdbMovieEntityMapper = new TmdbMovieEntityMapper();
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
        mMoviesState.setTrending(null);
        mMoviesState.setWatchlist(null);
        mMoviesState.getImdbIdMovies().clear();
        mMoviesState.getTmdbIdMovies().clear();

        // TODO: Clear Database Too
    }

    @Subscribe
    public void onSearchResultChanged(MoviesState.SearchResultChangedEvent event) {
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
                    case POPULAR:
                        fetchPopular();
                        break;
                    case TRENDING:
                        fetchTrending();
                        break;
                    case LIBRARY:
                        fetchLibrary();
                        break;
                    case WATCHLIST:
                        fetchWatchlist();
                        break;
                    case DETAIL:
                        fetchDetailMovie(ui.getRequestParameter());
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
                    markMovieUnseen(movie.getTraktId());
                } else {
                    markMovieSeen(movie.getTraktId());
                }
            }

            @Override
            public void toggleInWatchlist(PhilmMovie movie) {
                if (movie.inWatchlist()) {
                    removeFromWatchlist(movie.getTraktId());
                } else {
                    addToWatchlist(movie.getTraktId());
                }
            }

            @Override
            public void toggleInCollection(PhilmMovie movie) {
                if (movie.inCollection()) {
                    removeFromCollection(movie.getTraktId());
                } else {
                    addToCollection(movie.getTraktId());
                }
            }

            @Override
            public void search(String query) {
                fetchSearchResults(query);
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
                markMovieRating(movie.getTraktId(), rating);
            }

            @Override
            public void onScrolledToBottom() {
                MoviesState.MoviePaginatedResult result;

                switch (ui.getMovieQueryType()) {
                    case POPULAR:
                        result = mMoviesState.getPopular();
                        if (canFetchNextPage(result)) {
                            fetchPopular(result.page + 1);
                        }
                        break;
                    case SEARCH:
                        MoviesState.SearchPaginatedResult searchResult = mMoviesState.getSearchResult();
                        if (canFetchNextPage(searchResult)) {
                            fetchSearchResults(searchResult.query, searchResult.page + 1);
                        }
                        break;
                    case UPCOMING:
                        result = mMoviesState.getUpcoming();
                        if (canFetchNextPage(result)) {
                            fetchUpcoming(result.page + 1);
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
            mLogger.i(LOG_TAG, queryType.name() + " UI Attached but not logged in");
            return;
        }

        Display display = getDisplay();
        if (display != null && !ui.isModal()) {
            display.showUpNavigation(ui instanceof MovieDetailUi);
        }

        switch (queryType) {
            case TRENDING:
                fetchTrendingIfNeeded();
                break;
            case POPULAR:
                fetchPopularIfNeeded();
                break;
            case LIBRARY:
                fetchLibraryIfNeeded();
                break;
            case WATCHLIST:
                fetchWatchlistIfNeeded();
                break;
            case DETAIL:
                fetchDetailMovieIfNeeded(ui.getRequestParameter());
                break;
            case NOW_PLAYING:
                fetchNowPlayingIfNeeded();
                break;
            case UPCOMING:
                fetchUpcomingIfNeeded();
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

    private void addToCollection(String imdbId) {
        mExecutor.execute(new AddMovieToCollectionRunnable(imdbId));
    }

    private void addToWatchlist(String imdbId) {
        mExecutor.execute(new AddMovieToWatchlistRunnable(imdbId));
    }

    private void checkDetailMovieResult(PhilmMovie movie) {
        if (PhilmCollections.isEmpty(movie.getRelated())) {
            fetchRelatedMovies(movie);
        }

        if (TextUtils.isEmpty(movie.getReleaseCountryCode())) {
            fetchMovieReleases(movie);
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

    private void fetchDetailMovie(String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        PhilmMovie movie = getMovie(id);
        if (movie != null && movie.getTmdbId() != null) {
            fetchDetailMovieFromTmdb(movie.getTmdbId());

            if (isLoggedIn()) {
                fetchDetailMovieFromTrakt(id);
            }
        } else {
            fetchDetailMovieFromTrakt(id);
        }
    }

    private void fetchDetailMovieIfNeeded(String traktId) {
        Preconditions.checkNotNull(traktId, "traktId cannot be null");

        PhilmMovie cached = getMovie(traktId);
        if (cached == null || requireMovieDetailFetch(cached)) {
            fetchDetailMovie(traktId);
        } else {
            checkDetailMovieResult(cached);
        }
    }

    private void fetchDetailMovieFromTrakt(String id) {
        Preconditions.checkNotNull(id, "id cannot be null");
        mExecutor.execute(new FetchDetailMovieRunnable(id));
    }

    private void fetchDetailMovieFromTmdb(int tmdbId) {
        mExecutor.execute(new FetchTmdbDetailMovieRunnable(tmdbId));
    }

    private void fetchLibrary() {
        if (isLoggedIn()) {
            mExecutor.execute(new FetchLibraryRunnable(mMoviesState.getUsername()));
        }
    }

    private void fetchLibraryIfNeeded() {
        if (mPopulatedLibraryFromDb && PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            fetchLibrary();
        }
    }

    private void fetchMovieReleases(PhilmMovie movie) {
        mExecutor.execute(new FetchTmdbReleasesRunnable(movie));
    }

    private void fetchNowPlaying() {
        mMoviesState.setNowPlaying(null);
        fetchNowPlaying(TMDB_FIRST_PAGE);
    }

    private void fetchNowPlaying(final int page) {
        mExecutor.execute(new FetchTmdbNowPlayingRunnable(page));
    }

    private void fetchNowPlayingIfNeeded() {
        MoviesState.MoviePaginatedResult nowPlaying = mMoviesState.getNowPlaying();
        if (nowPlaying == null || PhilmCollections.isEmpty(nowPlaying.items)) {
            fetchNowPlaying(TMDB_FIRST_PAGE);
        }
    }

    private void fetchPopular() {
        mMoviesState.setPopular(null);
        fetchPopular(TMDB_FIRST_PAGE);
    }

    private void fetchPopular(final int page) {
        mExecutor.execute(new FetchTmdbPopularRunnable(page));
    }

    private void fetchPopularIfNeeded() {
        MoviesState.MoviePaginatedResult popular = mMoviesState.getPopular();
        if (popular == null || PhilmCollections.isEmpty(popular.items)) {
            fetchPopular(TMDB_FIRST_PAGE);
        }
    }

    private void fetchUpcoming() {
        mMoviesState.setUpcoming(null);
        fetchUpcoming(TMDB_FIRST_PAGE);
    }

    private void fetchUpcoming(final int page) {
        mExecutor.execute(new FetchTmdbUpcomingRunnable(page));
    }

    private void fetchUpcomingIfNeeded() {
        MoviesState.MoviePaginatedResult upcoming = mMoviesState.getUpcoming();
        if (upcoming == null || PhilmCollections.isEmpty(upcoming.items)) {
            fetchUpcoming(TMDB_FIRST_PAGE);
        }
    }

    private void fetchRelatedMovies(PhilmMovie movie) {
        if (movie.getTmdbId() != null) {
            mExecutor.execute(new FetchTmdbRelatedMoviesRunnable(movie.getTmdbId()));
        } else if (!TextUtils.isEmpty(movie.getImdbId())) {
            mExecutor.execute(new FetchRelatedMoviesRunnable(movie.getImdbId()));
        }
    }

    private void fetchSearchResults(String query) {
        mMoviesState.setSearchResult(null);
        fetchSearchResults(query, TMDB_FIRST_PAGE);
    }

    private void fetchSearchResults(String query, int page) {
        mExecutor.execute(new SearchMoviesRunnable(query, page));
    }

    private void fetchTmdbConfiguration() {
        mExecutor.execute(new FetchTmdbConfigurationRunnable());
    }

    private void fetchTrending() {
        mExecutor.execute(new FetchTrendingRunnable());
    }

    private void fetchTrendingIfNeeded() {
        if (PhilmCollections.isEmpty(mMoviesState.getTrending())) {
            fetchTrending();
        }
    }

    private void fetchWatchlist() {
        if (isLoggedIn()) {
            mExecutor.execute(new FetchWatchlistRunnable(mMoviesState.getUsername()));
        }
    }

    private void fetchWatchlistIfNeeded() {
        if (mPopulatedWatchlistFromDb
                && PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            fetchWatchlist();
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

    private PhilmMovie getMovie(String id) {
        if (mMoviesState.getImdbIdMovies().containsKey(id)) {
            return mMoviesState.getImdbIdMovies().get(id);
        } else if (mMoviesState.getTmdbIdMovies().containsKey(id)) {
            return mMoviesState.getTmdbIdMovies().get(id);
        }
        return null;
    }

    private boolean hasMovieUiAttached(MovieQueryType queryType) {
        for (MovieUi ui : getUis()) {
            if (ui.getMovieQueryType() == queryType) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoggedIn() {
        return mMoviesState.getCurrentAccount() != null;
    }

    private void markMovieRating(String imdbId, Rating rating) {
        mLogger.d(LOG_TAG, "markMovieRating: " + imdbId + ". " + rating.name());
        mExecutor.execute(new SubmitMovieRatingRunnable(imdbId, rating));
    }

    private void markMovieSeen(String imdbId) {
        mExecutor.execute(new MarkMovieSeenRunnable(imdbId));
    }

    private void markMovieUnseen(String imdbId) {
        mExecutor.execute(new MarkMovieUnseenRunnable(imdbId));
    }

    private void persistLibraryToDb(final List<PhilmMovie> movies) {
        assertInited();
        mDbHelper.mergeLibrary(movies);
    }

    private void persistWatchlistToDb(final List<PhilmMovie> movies) {
        assertInited();
        mDbHelper.mergeWatchlist(movies);
    }

    private void populateDetailUi(MovieDetailUi ui) {
        final PhilmMovie movie = getMovie(ui.getRequestParameter());

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
        }

        if (requireFiltering && !PhilmCollections.isEmpty(items)) {
            items = filterMovies(items);
        }

        if (items == null) {
            ui.setItems(null);
        } else if (PhilmCollections.isEmpty(sections)) {
            ui.setItems(createListItemList(items));
        } else {
            ui.setItems(createSectionedListItemList(items, sections, sectionProcessingOrder));
        }
    }

    private void populateMovieDiscoverUi(MovieDiscoverUi ui) {
        ui.setTabs(DiscoverTab.values());
    }

    private void populateRateUi(MovieRateUi ui) {
        final PhilmMovie movie = getMovie(ui.getRequestParameter());
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

    private PhilmMovie putMovie(PhilmMovie movie) {
        if (!TextUtils.isEmpty(movie.getImdbId())) {
            mMoviesState.getImdbIdMovies().put(movie.getImdbId(), movie);
        }
        if (movie.getTmdbId() != null) {
            mMoviesState.getTmdbIdMovies().put(String.valueOf(movie.getTmdbId()), movie);
        }
        return null;
    }

    private void removeFromCollection(String imdbId) {
        mExecutor.execute(new RemoveMovieFromCollectionRunnable(imdbId));
    }

    private void removeFromWatchlist(String imdbId) {
        mExecutor.execute(new RemoveMovieFromWatchlistRunnable(imdbId));
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

    private void showLoadingProgress(boolean show) {
        for (MovieUi ui : getUis()) {
            ui.showLoadingProgress(show);
        }
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
        TRENDING, POPULAR, LIBRARY, WATCHLIST, DETAIL, SEARCH, NOW_PLAYING, UPCOMING, NONE;

        public boolean requireLogin() {
            switch (this) {
                case WATCHLIST:
                case LIBRARY:
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

    public static enum DiscoverTab {
        POPULAR, IN_THEATRES, UPCOMING;
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
        void setTabs(DiscoverTab[] tabs);
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

        void search(String query);

        void clearSearch();

        void showRateMovie(PhilmMovie movie);

        void submitRating(PhilmMovie movie, Rating rating);

        void onScrolledToBottom();
    }

    private abstract class BaseMovieRunnable<R> extends NetworkCallRunnable<R> {

        @Override
        public void onPreTraktCall() {
            showLoadingProgress(true);
        }

        @Override
        public void onError(RetrofitError re) {
            for (MovieUi ui : getUis()) {
                ui.showError(NetworkError.from(re));
            }
        }

        @Override
        public void onFinished() {
            showLoadingProgress(false);
        }
    }

    private class FetchTmdbConfigurationRunnable extends NetworkCallRunnable<TmdbConfiguration> {
        @Override
        public TmdbConfiguration doBackgroundCall() throws RetrofitError {
            TmdbConfiguration configuration = getConfigFromFile();

            if (configuration != null && configuration.isValid()) {
                if (Constants.DEBUG) {
                    mLogger.d(LOG_TAG, "Got valid TMDB config from file");
                }
            } else {
                if (Constants.DEBUG) {
                    mLogger.d(LOG_TAG, "Fectching TMDB config from network");
                }

                // No config in file, so download from web
                Configuration tmdbConfig = mTmdbClient.configurationService().configuration();

                if (tmdbConfig != null) {
                    // Downloaded config from web so file it to file
                    configuration = new TmdbConfiguration();
                    configuration.setFromTmdb(tmdbConfig);
                    writeConfigToFile(configuration);
                } else {
                    configuration = null;
                }
            }

            return configuration;
        }

        @Override
        public void onSuccess(TmdbConfiguration result) {
            if (result != null) {
                mImageHelper.setTmdbBaseUrl(result.getImagesBaseUrl());
                mImageHelper.setTmdbBackdropSizes(result.getImagesBackdropSizes());
                mImageHelper.setTmdbPosterSizes(result.getImagesPosterSizes());
            }

            mMoviesState.setTmdbConfiguration(result);
        }

        @Override
        public void onError(RetrofitError re) {
            // Ignore
        }

        private TmdbConfiguration getConfigFromFile() {
            File file = mFileManager.getFile(FILENAME_TMDB_CONFIG);
            if (file.exists()) {
                FileReader reader = null;
                try {
                    reader = new FileReader(file);
                    Gson gson = new Gson();
                    return gson.fromJson(reader, TmdbConfiguration.class);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        private void writeConfigToFile(TmdbConfiguration configuration) {
            FileWriter writer = null;

            try {
                File file = mFileManager.getFile(FILENAME_TMDB_CONFIG);
                if (!file.exists()) {
                    file.createNewFile();
                }

                writer = new FileWriter(file, false);
                Gson gson = new Gson();
                gson.toJson(configuration, writer);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class FetchTrendingRunnable extends BaseMovieRunnable<List<Movie>> {

        @Override
        public List<Movie> doBackgroundCall() throws RetrofitError {
            return mTraktClient.moviesService().trending();
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                mMoviesState.setTrending(mTraktMovieEntityMapper.map(result));
            } else {
                mMoviesState.setTrending(null);
            }
        }

        @Override
        public void onError(RetrofitError re) {
            for (MovieUi ui : getUis()) {
                ui.showError(NetworkError.from(re));
            }
        }
    }

    private abstract class BasePaginatedTmdbRunnable extends BaseMovieRunnable<ResultsPage> {
        private final int mPage;

        BasePaginatedTmdbRunnable(int page) {
            mPage = page;
        }

        @Override
        public final void onSuccess(ResultsPage result) {
            if (result != null) {
                MoviesState.MoviePaginatedResult paginatedResult = getResultFromState();
                if (paginatedResult == null) {
                    paginatedResult = createPaginatedResult();
                    paginatedResult.items = new ArrayList<PhilmMovie>();
                }

                paginatedResult.items.addAll(mTmdbMovieEntityMapper.map(result.results));
                paginatedResult.page = result.page;

                if (result.total_pages != null) {
                    paginatedResult.totalPages = result.total_pages;
                }

                updateState(paginatedResult);
            }
        }

        @Override
        public void onError(RetrofitError re) {
            for (MovieUi ui : getUis()) {
                ui.showError(NetworkError.from(re));
            }
        }

        protected int getPage() {
            return mPage;
        }

        protected abstract MoviesState.MoviePaginatedResult getResultFromState();

        protected abstract void updateState(MoviesState.MoviePaginatedResult result);

        protected MoviesState.MoviePaginatedResult createPaginatedResult() {
            return new MoviesState.MoviePaginatedResult();
        }
    }

    private class FetchTmdbPopularRunnable extends BasePaginatedTmdbRunnable {
        FetchTmdbPopularRunnable(int page) {
            super(page);
        }

        @Override
        public ResultsPage doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().popular(getPage(), null);
        }

        @Override
        protected MoviesState.MoviePaginatedResult getResultFromState() {
            return mMoviesState.getPopular();
        }

        @Override
        protected void updateState(MoviesState.MoviePaginatedResult result) {
            mMoviesState.setPopular(result);
        }
    }

    private class FetchTmdbNowPlayingRunnable extends BasePaginatedTmdbRunnable {
        FetchTmdbNowPlayingRunnable(int page) {
            super(page);
        }

        @Override
        public ResultsPage doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().nowPlaying(getPage(), null);
        }

        @Override
        protected MoviesState.MoviePaginatedResult getResultFromState() {
            return mMoviesState.getNowPlaying();
        }

        @Override
        protected void updateState(MoviesState.MoviePaginatedResult result) {
            mMoviesState.setNowPlaying(result);
        }
    }

    private class FetchTmdbUpcomingRunnable extends BasePaginatedTmdbRunnable {
        FetchTmdbUpcomingRunnable(int page) {
            super(page);
        }

        @Override
        public ResultsPage doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().upcoming(getPage(), null);
        }

        @Override
        protected MoviesState.MoviePaginatedResult getResultFromState() {
            return mMoviesState.getUpcoming();
        }

        @Override
        protected void updateState(MoviesState.MoviePaginatedResult result) {
            mMoviesState.setUpcoming(result);
        }
    }

    private class FetchLibraryRunnable extends BaseMovieRunnable<List<Movie>> {

        private final String mUsername;

        FetchLibraryRunnable(String username) {
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public List<Movie> doBackgroundCall() throws RetrofitError {
            return mTraktClient.userService().libraryMoviesAll(mUsername);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                List<PhilmMovie> movies = mTraktMovieEntityMapper.map(result);
                mMoviesState.setLibrary(movies);

                if (isInited()) {
                    persistLibraryToDb(movies);
                }
            } else {
                mMoviesState.setLibrary(null);
            }
        }
    }

    private class FetchWatchlistRunnable extends BaseMovieRunnable<List<Movie>> {

        private final String mUsername;

        FetchWatchlistRunnable(String username) {
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public List<Movie> doBackgroundCall() throws RetrofitError {
            return mTraktClient.userService().watchlistMovies(mUsername);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                List<PhilmMovie> movies = mTraktMovieEntityMapper.map(result);
                mMoviesState.setWatchlist(movies);

                if (isInited()) {
                    persistWatchlistToDb(movies);
                }
            } else {
                mMoviesState.setWatchlist(null);
            }
        }
    }

    private class SearchMoviesRunnable extends BasePaginatedTmdbRunnable {
        private final String mQuery;

        SearchMoviesRunnable(String query, int page) {
            super(page);
            mQuery = Preconditions.checkNotNull(query, "query cannot be null");
        }

        @Override
        public ResultsPage doBackgroundCall() throws RetrofitError {
            return mTmdbClient.searchService().movie(mQuery, getPage(),
                    null, null, null, null, null);
        }

        @Override
        protected MoviesState.MoviePaginatedResult getResultFromState() {
            return mMoviesState.getSearchResult();
        }

        @Override
        protected void updateState(MoviesState.MoviePaginatedResult result) {
            mMoviesState.setSearchResult((MoviesState.SearchPaginatedResult) result);
        }

        @Override
        protected MoviesState.MoviePaginatedResult createPaginatedResult() {
            MoviesState.SearchPaginatedResult result = new MoviesState.SearchPaginatedResult();
            result.query = mQuery;
            return result;
        }
    }

    private abstract class BaseMovieActionTraktRunnable extends BaseMovieRunnable<Response> {
        private final String mId;

        BaseMovieActionTraktRunnable(String id) {
            mId = Preconditions.checkNotNull(id, "id cannot be null");
        }

        @Override
        public final Response doBackgroundCall() throws RetrofitError {
            MovieService.SeenMovie seenMovie = new MovieService.SeenMovie(mId);
            return doTraktCall(mTraktClient, new MovieService.Movies(seenMovie));
        }

        public abstract Response doTraktCall(Trakt trakt, MovieService.Movies body);

        @Override
        public final void onSuccess(Response result) {
            if (result instanceof ActionResponse) {
                onActionCompleted(((ActionResponse) result).skipped == 0);
            } else {
                onActionCompleted("success".equals(result.status));
            }
        }

        protected abstract void movieRequiresModifying(PhilmMovie movie);

        private void onActionCompleted(final boolean successful) {
            if (successful) {
                PhilmMovie movie = getMovie(mId);
                if (movie != null) {
                    movieRequiresModifying(movie);
                    checkPhilmState(movie);
                    populateUis();
                } else {
                    fetchDetailMovie(mId);
                }
            }
        }
    }

    private class FetchDetailMovieRunnable extends BaseMovieRunnable<Movie> {

        private final String mImdbId;

        FetchDetailMovieRunnable(String imdbId) {
            mImdbId = Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        }

        @Override
        public Movie doBackgroundCall() throws RetrofitError {
            return mTraktClient.movieService().summary(mImdbId);
        }

        @Override
        public void onSuccess(Movie result) {
            PhilmMovie movie = mTraktMovieEntityMapper.map(result);
            checkPhilmState(movie);
            if (isInited()) {
                mDbHelper.put(movie);
            }

            populateUis();
            checkDetailMovieResult(movie);
        }
    }

    private class FetchTmdbDetailMovieRunnable
            extends BaseMovieRunnable<com.uwetrottmann.tmdb.entities.Movie> {
        private final int mTmdbId;

        FetchTmdbDetailMovieRunnable(int tmdbId) {
            mTmdbId = tmdbId;
        }

        @Override
        public com.uwetrottmann.tmdb.entities.Movie doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().summary(mTmdbId);
        }

        @Override
        public void onSuccess(com.uwetrottmann.tmdb.entities.Movie result) {
            PhilmMovie movie = mTmdbMovieEntityMapper.map(result);
            checkPhilmState(movie);
            if (isInited()) {
                mDbHelper.put(movie);
            }

            populateUis();
            checkDetailMovieResult(movie);
        }
    }

    private class FetchRelatedMoviesRunnable extends BaseMovieRunnable<List<Movie>> {
        private final String mId;

        FetchRelatedMoviesRunnable(String id) {
            mId = Preconditions.checkNotNull(id, "id cannot be null");
        }

        @Override
        public void onPreTraktCall() {
            showRelatedLoadingProgress(true);
        }

        @Override
        public List<Movie> doBackgroundCall() throws RetrofitError {
            return mTraktClient.movieService().related(mId);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            PhilmMovie movie = getMovie(mId);
            movie.setRelated(mTraktMovieEntityMapper.map(result));
            populateUis();
        }

        @Override
        public void onFinished() {
            showRelatedLoadingProgress(false);
        }

        private void showRelatedLoadingProgress(boolean show) {
            for (MovieUi ui : getUis()) {
                if (ui instanceof MovieDetailUi) {
                    ((MovieDetailUi) ui).showRelatedMoviesLoadingProgress(show);
                }
            }
        }
    }

    private class FetchTmdbRelatedMoviesRunnable extends BaseMovieRunnable<ResultsPage> {
        private final int mId;

        FetchTmdbRelatedMoviesRunnable(int id) {
            mId = id;
        }

        @Override
        public void onPreTraktCall() {
            showRelatedLoadingProgress(true);
        }

        @Override
        public ResultsPage doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().similarMovies(mId);
        }

        @Override
        public void onSuccess(ResultsPage result) {
            PhilmMovie movie = getMovie(String.valueOf(mId));
            movie.setRelated(mTmdbMovieEntityMapper.map(result.results));
            populateUis();
        }

        @Override
        public void onFinished() {
            showRelatedLoadingProgress(false);
        }

        private void showRelatedLoadingProgress(boolean show) {
            for (MovieUi ui : getUis()) {
                if (ui instanceof MovieDetailUi) {
                    ((MovieDetailUi) ui).showRelatedMoviesLoadingProgress(show);
                }
            }
        }
    }

    private class MarkMovieSeenRunnable extends BaseMovieActionTraktRunnable {

        MarkMovieSeenRunnable(String imdbId) {
            super(imdbId);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().seen(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setWatched(true);
        }
    }

    private class MarkMovieUnseenRunnable extends BaseMovieActionTraktRunnable {

        MarkMovieUnseenRunnable(String imdbId) {
            super(imdbId);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().unseen(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setWatched(false);
        }
    }

    private class SubmitMovieRatingRunnable extends BaseMovieRunnable<RatingResponse> {
        private final String mId;
        private final Rating mRating;

        SubmitMovieRatingRunnable(String id, Rating rating) {
            mId = Preconditions.checkNotNull(id, "id cannot be null");
            mRating = Preconditions.checkNotNull(rating, "rating cannot be null");
        }

        @Override
        public RatingResponse doBackgroundCall() throws RetrofitError {
            return mTraktClient.rateService().movie(new RateService.MovieRating(mId, mRating));
        }

        @Override
        public void onSuccess(RatingResponse result) {
            if ("success".equals(result.status)) {
                PhilmMovie movie = getMovie(mId);
                if (movie != null) {
                    if (result.rating != null) {
                        movie.setUserRatingAdvanced(result.rating);
                    } else {
                        movie.setUserRatingAdvanced(mRating);
                    }
                    mDbHelper.put(movie);
                    populateUis();
                }
            }
        }
    }

    private class AddMovieToWatchlistRunnable extends BaseMovieActionTraktRunnable {
        AddMovieToWatchlistRunnable(String id) {
            super(id);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().watchlist(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setInWatched(true);
        }
    }

    private class RemoveMovieFromWatchlistRunnable extends BaseMovieActionTraktRunnable {
        RemoveMovieFromWatchlistRunnable(String id) {
            super(id);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().unwatchlist(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setInWatched(false);
        }
    }

    private class AddMovieToCollectionRunnable extends BaseMovieActionTraktRunnable {
        AddMovieToCollectionRunnable(String id) {
            super(id);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().library(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setInCollection(true);
        }
    }

    private class RemoveMovieFromCollectionRunnable extends BaseMovieActionTraktRunnable {
        RemoveMovieFromCollectionRunnable(String id) {
            super(id);
        }

        @Override
        public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
            return trakt.movieService().unlibrary(body);
        }

        @Override
        protected void movieRequiresModifying(PhilmMovie movie) {
            movie.setInCollection(false);
        }
    }

    private class LibraryDbLoadCallback implements AsyncDatabaseHelper.Callback<List<PhilmMovie>> {

        @Override
        public void onFinished(List<PhilmMovie> result) {
            mMoviesState.setLibrary(result);
            if (!PhilmCollections.isEmpty(result)) {
                for (PhilmMovie movie : result) {
                    putMovie(movie);
                }
            }
            mPopulatedLibraryFromDb = true;

            if (hasMovieUiAttached(MovieQueryType.LIBRARY)) {
                fetchWatchlistIfNeeded();
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
                    putMovie(movie);
                }
            }
            mPopulatedWatchlistFromDb = true;

            if (hasMovieUiAttached(MovieQueryType.WATCHLIST)) {
                fetchWatchlistIfNeeded();
            }
        }
    }

    private class FetchTmdbReleasesRunnable extends BaseMovieRunnable<ReleasesResult> {
        private final PhilmMovie mMovie;

        FetchTmdbReleasesRunnable(PhilmMovie movie) {
            mMovie = Preconditions.checkNotNull(movie, "movie cannot be null");
        }

        @Override
        public ReleasesResult doBackgroundCall() throws RetrofitError {
            return mTmdbClient.moviesService().releases(mMovie.getTmdbId());
        }

        @Override
        public void onSuccess(ReleasesResult result) {
            final String countryCode = mCountryProvider.getTwoLetterCountryCode();

            if (!PhilmCollections.isEmpty(result.countries)) {
                CountryRelease countryRelease = null;
                CountryRelease usRelease = null;

                for (CountryRelease release : result.countries) {
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
                    mMovie.updateFrom(countryRelease);
                    mDbHelper.put(mMovie);
                    populateUis();
                }
            }
        }
    }

    private abstract class MovieEntityMapper<T> {

        abstract PhilmMovie map(T entity);

        List<PhilmMovie> map(List<T> entities) {
            final ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>(entities.size());
            for (T entity : entities) {
                movies.add(map(entity));
            }
            return movies;
        }
    }

    private class TraktMovieEntityMapper extends
            MovieEntityMapper<com.jakewharton.trakt.entities.Movie> {

        @Override
        PhilmMovie map(com.jakewharton.trakt.entities.Movie entity) {
            PhilmMovie movie = getMovie(entity.imdb_id);
            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putMovie(movie);

            return movie;
        }
    }

    private class TmdbMovieEntityMapper extends
            MovieEntityMapper<com.uwetrottmann.tmdb.entities.Movie> {

        @Override
        PhilmMovie map(com.uwetrottmann.tmdb.entities.Movie entity) {
            PhilmMovie movie = getMovie(String.valueOf(entity.id));
            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putMovie(movie);

            return movie;
        }
    }

}
