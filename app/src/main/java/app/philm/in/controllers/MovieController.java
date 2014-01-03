package app.philm.in.controllers;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.RatingResponse;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.enumerations.Rating;
import com.jakewharton.trakt.services.MovieService;
import com.jakewharton.trakt.services.RateService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.SearchResult;
import app.philm.in.network.NetworkError;
import app.philm.in.network.TraktNetworkCallRunnable;
import app.philm.in.state.DatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

import static app.philm.in.util.TimeUtils.isAfterThreshold;
import static app.philm.in.util.TimeUtils.isBeforeThreshold;
import static app.philm.in.util.TimeUtils.isInFuture;
import static app.philm.in.util.TimeUtils.isInPast;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    private static final String LOG_TAG = MovieController.class.getSimpleName();

    private final MoviesState mMoviesState;
    private final ExecutorService mExecutor;
    private final Trakt mTraktClient;
    private final DatabaseHelper mDbHelper;

    private boolean mPopulatedLibraryFromDb = false;
    private boolean mPopulatedWatchlistFromDb = false;

    public MovieController(
            MoviesState movieState,
            Trakt traktClient,
            ExecutorService executor,
            DatabaseHelper dbHelper) {
        super();
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");
        mTraktClient = Preconditions.checkNotNull(traktClient, "trackClient cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
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
    public void onWatchlistChanged(MoviesState.WatchlistChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onAccountChanged(UserState.AccountChangedEvent event) {
        // Nuke all Movie State...
        mMoviesState.setLibrary(null);
        mMoviesState.setTrending(null);
        mMoviesState.setWatchlist(null);
        mMoviesState.getMovies().clear();
    }

    @Subscribe
    public void onSearchResultChanged(MoviesState.SearchResultChangedEvent event) {
        populateUis();
    }

    @Override
    protected void onInited() {
        super.onInited();
        populateStateFromDb();
        mMoviesState.registerForEvents(this);
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
                        fetchTrending();
                        break;
                    case LIBRARY:
                        fetchLibrary();
                        break;
                    case WATCHLIST:
                        fetchWatchlist();
                        break;
                }
            }

            @Override
            public void showMovieDetail(PhilmMovie movie) {
                Display display = getDisplay();
                if (display != null) {
                    display.showMovieDetailFragment(movie.getTraktId());
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
        };
    }

    @Override
    protected void onUiAttached(final MovieUi ui) {
        final MovieQueryType queryType = ui.getMovieQueryType();

        if (queryType.requireLogin() && !isLoggedIn()) {
            Log.i(LOG_TAG, queryType.name() + " UI Attached but not logged in");
            return;
        }

        Display display = getDisplay();
        if (display != null) {
            display.setDrawerToggleEnabled(!(ui instanceof MovieDetailUi));

            if (queryType.getTitle() != 0) {
                display.setActionBarTitle(queryType.getTitle());
            }
        }

        switch (queryType) {
            case TRENDING:
                fetchTrendingIfNeeded();
                break;
            case LIBRARY:
                fetchLibraryIfNeeded();
                break;
            case WATCHLIST:
                fetchWatchlistIfNeeded();
                break;
            case DETAIL:
                fetchDetailMovieIfNeeded(ui);
                break;
        }
    }

    @Override
    protected void populateUi(final MovieUi ui) {
        if (!isLoggedIn() && ui.getMovieQueryType().requireLogin()) {
            ui.showError(NetworkError.UNAUTHORIZED);
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
        }
    }

    List<PhilmMovie> mapTraktMoviesFromState(List<Movie> rawMovies) {
        final ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>(rawMovies.size());
        for (Movie rawMovie : rawMovies) {
            movies.add(mapTraktMovieFromState(rawMovie));
        }
        return movies;
    }

    PhilmMovie mapTraktMovieFromState(Movie rawMovie) {
        final Map<String, PhilmMovie> stateMovies = mMoviesState.getMovies();

        PhilmMovie movie = stateMovies.get(PhilmMovie.getTraktId(rawMovie));
        if (movie != null) {
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(rawMovie);
        } else {
            // No movie, so create one
            movie = new PhilmMovie(rawMovie);
            stateMovies.put(movie.getTraktId(), movie);
        }

        return movie;
    }

    private void addToCollection(String imdbId) {
        mExecutor.execute(new AddMovieToCollectionRunnable(imdbId));
    }

    private void addToWatchlist(String imdbId) {
        mExecutor.execute(new AddMovieToWatchlistRunnable(imdbId));
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

        ArrayMap<Filter, List<ListItem<PhilmMovie>>> sectionsItemLists = null;

        for (MovieController.Filter filter : sectionProcessingOrder) {
            List<ListItem<PhilmMovie>> sectionItems = null;

            for (Iterator<PhilmMovie> i = movies.iterator(); i.hasNext(); ) {
                PhilmMovie movie = i.next();
                if (filter.isMovieFiltered(movie)) {
                    if (sectionItems == null) {
                        sectionItems = new ArrayList<ListItem<PhilmMovie>>();
                        // Now add Title
                        sectionItems.add(new ListItem<PhilmMovie>(filter.getTitle()));
                    }
                    sectionItems.add(new ListItem<PhilmMovie>(movie));
                    i.remove();
                }
            }

            if (!PhilmCollections.isEmpty(sectionItems)) {
                if (sectionsItemLists == null) {
                    sectionsItemLists = new ArrayMap<Filter, List<ListItem<PhilmMovie>>>();
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

    private void fetchDetailMovie(MovieUi ui) {
        fetchDetailMovie(ui.getRequestParameter());
    }

    private void fetchDetailMovie(String imdbId) {
        Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        mExecutor.execute(new FetchDetailMovieRunnable(imdbId));
    }

    private void fetchDetailMovieIfNeeded(MovieUi ui) {
        PhilmMovie cached = getMovie(ui.getRequestParameter());
        if (cached == null || requireMovieDetailFetch(cached)) {
            fetchDetailMovie(ui);
        } else {
            checkDetailMovieResult(cached);
        }
    }

    private void checkDetailMovieResult(PhilmMovie movie) {
        if (PhilmCollections.isEmpty(movie.getRelated())) {
            fetchRelatedMovies(movie.getTraktId());
        }
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

    private void fetchSearchResults(String query) {
        Preconditions.checkNotNull(query, "query cannot be null");
        final SearchResult result = new SearchResult(query);

        mMoviesState.setSearchResult(result);
        mExecutor.execute(new SearchMoviesRunnable(result));
    }

    private void fetchRelatedMovies(String traktId) {
        mExecutor.execute(new FetchRelatedMoviesRunnable(traktId));
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
        mExecutor.execute(new FetchWatchlistRunnable(mMoviesState.getUsername()));
    }

    private void fetchWatchlistIfNeeded() {
        if (mPopulatedWatchlistFromDb && PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
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

    private PhilmMovie getMovie(String tmdbId) {
        return mMoviesState.getMovies().get(tmdbId);
    }

    private boolean isLoggedIn() {
        return mMoviesState.getCurrentAccount() != null;
    }

    private void markMovieSeen(String imdbId) {
        mExecutor.execute(new MarkMovieSeenRunnable(imdbId));
    }

    private void markMovieUnseen(String imdbId) {
        mExecutor.execute(new MarkMovieUnseenRunnable(imdbId));
    }

    private void markMovieRating(String imdbId, Rating rating) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "markMovieRating: " + imdbId + ". " + rating.name());
        }
        mExecutor.execute(new SubmitMovieRatingRunnable(imdbId, rating));
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
        ui.setMovie(movie);

        boolean isLoggedIn = isLoggedIn();
        ui.setRateCircleEnabled(isLoggedIn);
        ui.setCollectionButtonEnabled(isLoggedIn);
        ui.setWatchlistButtonEnabled(isLoggedIn);
        ui.setToggleWatchedButtonEnabled(isLoggedIn);

        Display display = getDisplay();
        if (display != null) {
            display.setActionBarTitle(movie != null ? movie.getTitle() : null);
        }
    }

    private void populateRateUi(MovieRateUi ui) {
        final PhilmMovie movie = getMovie(ui.getRequestParameter());
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
            case LIBRARY:
                items = mMoviesState.getLibrary();
                break;
            case WATCHLIST:
                items = mMoviesState.getWatchlist();
                sections = Arrays.asList(Filter.UPCOMING, Filter.SOON, Filter.RELEASED,
                        Filter.WATCHED);
                sectionProcessingOrder = Arrays.asList(Filter.UPCOMING, Filter.SOON, Filter.WATCHED,
                        Filter.RELEASED);
                break;
            case SEARCH:
                SearchResult result = mMoviesState.getSearchResult();
                if (result != null) {
                    items = result.getMovies();
                }
                break;
        }

        if (requireFiltering) {
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

    private void populateSearchUi(SearchMovieUi ui) {
        SearchResult result = mMoviesState.getSearchResult();
        ui.setQuery(result != null ? result.getQuery() : null);

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
        COLLECTION(R.string.filter_collection),

        /**
         * Filters {@link PhilmMovie} that have been watched by the user.
         */
        WATCHED(R.string.filter_watched),

        /**
         * Filters {@link PhilmMovie} that have not been watched by the user.
         */
        UNWATCHED(R.string.filter_unwatched),

        /**
         * Filters {@link PhilmMovie} that have not been released yet.
         */
        NOT_RELEASED(R.string.filter_upcoming),

        /**
         * Filters {@link PhilmMovie} that have already been released.
         */
        RELEASED(R.string.filter_released),

        /**
         * Filters {@link PhilmMovie} that are unreleased, and will be released in the far future.
         */
        UPCOMING(R.string.filter_upcoming),

        /**
         * Filters {@link PhilmMovie} that are unreleased, and will be released in the near future.
         */
        SOON(R.string.filter_soon),

        /**
         * Filters {@link PhilmMovie} which are highly rated, either by the user or the public.
         */
        HIGHLY_RATED(R.string.filter_highly_rated);


        private final int mTitle;

        private Filter(int titleResId) {
            mTitle = titleResId;
        }

        public int getTitle() {
            return mTitle;
        }

        public boolean isMovieFiltered(PhilmMovie movie) {
            switch (this) {
                case COLLECTION:
                    return movie.inCollection();
                case WATCHED:
                    return movie.isWatched();
                case UNWATCHED:
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
                case WATCHED:
                    return Arrays.asList(UNWATCHED);
                case UNWATCHED:
                    return Arrays.asList(WATCHED);
            }
            return null;
        }
    }

    public static enum MovieQueryType {
        TRENDING(R.string.trending_title),
        LIBRARY(R.string.library_title),
        WATCHLIST(R.string.watchlist_title),
        DETAIL(0),
        SEARCH(R.string.search_title),
        NONE(0);

        private final int mTitleResId;

        private MovieQueryType(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitle() {
            return mTitleResId;
        }

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

        void setToggleWatchedButtonEnabled(boolean enabled);
        void setCollectionButtonEnabled(boolean enabled);
        void setWatchlistButtonEnabled(boolean enabled);
        void setRateCircleEnabled(boolean enabled);
    }

    public interface MovieRateUi extends MovieUi {
        void setMovie(PhilmMovie movie);
    }

    public interface MovieUiCallbacks {
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
    }

    private abstract class BaseMovieTraktRunnable<R> extends TraktNetworkCallRunnable<R> {

        public BaseMovieTraktRunnable(Trakt traktClient) {
            super(traktClient);
        }

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

    private class FetchTrendingRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        public FetchTrendingRunnable() {
            super(mTraktClient);
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.philmMoviesService().trending();
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                mMoviesState.setTrending(mapTraktMoviesFromState(result));
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

    private class FetchLibraryRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        private final String mUsername;

        FetchLibraryRunnable(String username) {
            super(mTraktClient);
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.philmUserService().libraryMoviesAll(mUsername);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                List<PhilmMovie> movies = mapTraktMoviesFromState(result);
                mMoviesState.setLibrary(movies);

                if (isInited()) {
                    persistLibraryToDb(movies);
                }
            } else {
                mMoviesState.setLibrary(null);
            }
        }
    }

    private class FetchWatchlistRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        private final String mUsername;

        FetchWatchlistRunnable(String username) {
            super(mTraktClient);
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.philmUserService().watchlistMovies(mUsername);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (!PhilmCollections.isEmpty(result)) {
                List<PhilmMovie> movies = mapTraktMoviesFromState(result);
                mMoviesState.setWatchlist(movies);

                if (isInited()) {
                    persistWatchlistToDb(movies);
                }
            } else {
                mMoviesState.setWatchlist(null);
            }
        }
    }

    private class SearchMoviesRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        private final SearchResult mSearchResult;

        SearchMoviesRunnable(SearchResult searchResult) {
            super(mTraktClient);
            mSearchResult = Preconditions.checkNotNull(searchResult, "searchResult cannot be null");
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.philmSearchService().movies(mSearchResult.getQuery());
        }

        @Override
        public void onSuccess(List<Movie> result) {
            if (Objects.equal(mSearchResult, mMoviesState.getSearchResult())) {
                mSearchResult.setMovies(mapTraktMoviesFromState(result));
                mMoviesState.setSearchResult(mSearchResult);
            }
        }
    }

    private abstract class BaseMovieActionTraktRunnable extends BaseMovieTraktRunnable<Response> {
        private final String mImdbId;

        BaseMovieActionTraktRunnable(String imdbId) {
            super(mTraktClient);
            mImdbId = Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        }

        @Override
        public final Response doTraktCall(Trakt trakt) throws RetrofitError {
            MovieService.SeenMovie seenMovie = new MovieService.SeenMovie(mImdbId);

            return doTraktCall(trakt, new MovieService.Movies(seenMovie));
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
                PhilmMovie movie = mMoviesState.getMovies().get(mImdbId);
                if (movie != null) {
                    movieRequiresModifying(movie);
                    checkPhilmState(movie);
                    populateUis();
                } else {
                    fetchDetailMovie(mImdbId);
                }
            }
        }
    }

    private class FetchDetailMovieRunnable extends BaseMovieTraktRunnable<Movie> {
        private final String mImdbId;

        FetchDetailMovieRunnable(String imdbId) {
            super(mTraktClient);
            mImdbId = Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        }

        @Override
        public Movie doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.movieService().summary(mImdbId);
        }

        @Override
        public void onSuccess(Movie result) {
            PhilmMovie movie = mapTraktMovieFromState(result);
            checkPhilmState(movie);
            if (isInited()) {
                mDbHelper.put(movie);
            }

            populateUis();
            checkDetailMovieResult(movie);
        }
    }

    private class FetchRelatedMoviesRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        private final String mImdbId;

        FetchRelatedMoviesRunnable(String imdbId) {
            super(mTraktClient);
            mImdbId = Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.philmMovieService().related(mImdbId);
        }

        @Override
        public void onSuccess(List<Movie> result) {
            PhilmMovie movie = mMoviesState.getMovies().get(mImdbId);
            movie.setRelated(mapTraktMoviesFromState(result));
            populateUis();
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

    private class SubmitMovieRatingRunnable extends BaseMovieTraktRunnable<RatingResponse> {
        private final String mImdbId;
        private final Rating mRating;

        SubmitMovieRatingRunnable(String imdbId, Rating rating) {
            super(mTraktClient);
            mImdbId = Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
            mRating = Preconditions.checkNotNull(rating, "rating cannot be null");
        }

        @Override
        public RatingResponse doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.rateService().movie(new RateService.MovieRating(mImdbId, mRating));
        }

        @Override
        public void onSuccess(RatingResponse result) {
            if ("success".equals(result.status)) {
                PhilmMovie movie = mMoviesState.getMovies().get(mImdbId);
                if (movie != null) {
                    movie.setUserRatingAdvanced(result.rating);
                    mDbHelper.put(movie);
                    populateUis();
                }
            }
        }
    }

    private class AddMovieToWatchlistRunnable extends BaseMovieActionTraktRunnable {
        AddMovieToWatchlistRunnable(String imdbId) {
            super(imdbId);
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
        RemoveMovieFromWatchlistRunnable(String imdbId) {
            super(imdbId);
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
        AddMovieToCollectionRunnable(String imdbId) {
            super(imdbId);
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
        RemoveMovieFromCollectionRunnable(String imdbId) {
            super(imdbId);
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

    private class LibraryDbLoadCallback implements DatabaseHelper.Callback<List<PhilmMovie>> {
        @Override
        public void onFinished(List<PhilmMovie> result) {
            mMoviesState.setLibrary(result);
            for (PhilmMovie movie : result) {
                mMoviesState.getMovies().put(movie.getTraktId(), movie);
            }
            mPopulatedLibraryFromDb = true;
            fetchLibraryIfNeeded();
        }
    }

    private class WatchlistDbLoadCallback implements DatabaseHelper.Callback<List<PhilmMovie>> {
        @Override
        public void onFinished(List<PhilmMovie> result) {
            mMoviesState.setWatchlist(result);
            for (PhilmMovie movie : result) {
                mMoviesState.getMovies().put(movie.getTraktId(), movie);
            }
            mPopulatedWatchlistFromDb = true;
            fetchWatchlistIfNeeded();
        }
    }
}
