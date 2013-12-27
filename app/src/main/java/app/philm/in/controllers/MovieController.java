package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.services.MovieService;
import com.squareup.otto.Subscribe;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.R;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.network.TraktNetworkCallRunnable;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    private static final String LOG_TAG = MovieController.class.getSimpleName();

    public static enum Filter {
        COLLECTION(R.string.filter_collection),
        WATCHED(R.string.filter_watched),
        UNWATCHED(R.string.filter_unwatched),
        IN_FUTURE(R.string.filter_upcoming),
        UPCOMING(R.string.filter_upcoming),
        SOON(R.string.filter_soon),
        RELEASED(R.string.filter_released);

        private final int mTitle;

        private Filter(int titleResId) {
            mTitle = titleResId;
        }

        public int getTitle() {
            return mTitle;
        }

        public boolean isMovieFiltered(PhilmMovie movie) {
            return isMovieFiltered(movie, this);
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

        private static boolean isMovieFiltered(PhilmMovie movie, Filter filter) {
            // TODO: Move dependent methods to PhilmMovie
            final Movie unpacked = movie.getMovie();

            switch (filter) {
                case COLLECTION:
                    return movie.inCollection();
                case WATCHED:
                    return movie.isWatched();
                case UNWATCHED:
                    return !movie.isWatched();
                case IN_FUTURE:
                    if (unpacked.released != null) {
                        return unpacked.released.getTime() >= System.currentTimeMillis();
                    }
                    break;
                case UPCOMING:
                    if (unpacked.released != null) {
                        final long time = unpacked.released.getTime();
                        return  time - Constants.FUTURE_SOON_THRESHOLD >= System.currentTimeMillis();
                    }
                    break;
                case SOON:
                    if (unpacked.released != null) {
                        final long time = unpacked.released.getTime();
                        return time >= System.currentTimeMillis()
                                && time - Constants.FUTURE_SOON_THRESHOLD < System
                                .currentTimeMillis();
                    }
                    break;
                case RELEASED:
                    if (unpacked.released != null) {
                        return unpacked.released.getTime() < System.currentTimeMillis();
                    }
                    break;
            }
            return false;
        }
    }

    public static enum MovieQueryType {
        LIBRARY, TRENDING, WATCHLIST, DETAIL;

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
                    return true;
                default:
                    return false;
            }
        }
    }

    interface MovieUi extends BaseUiController.Ui<MovieUiCallbacks> {
        void showError(NetworkError error);
        void showLoadingProgress(boolean visible);
        MovieQueryType getMovieQueryType();
        String getRequestParameter();
    }

    public interface MovieListUi extends MovieUi {
        void setItems(List<PhilmMovie> items);
        void setItemsWithSections(List<PhilmMovie> items, List<Filter> sections);
        void setFiltersVisibility(boolean visible);
        void showActiveFilters(Set<Filter> filters);
    }

    public interface MovieDetailUi extends MovieUi {
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
    }

    private final MoviesState mMoviesState;
    private final ExecutorService mExecutor;
    private final Trakt mTraktClient;

    public MovieController(
            MoviesState movieState,
            Trakt traktClient,
            ExecutorService executor) {
        super();
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");
        mTraktClient = Preconditions.checkNotNull(traktClient, "trackClient cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
    }

    @Override
    protected void onInited() {
        super.onInited();
        mMoviesState.registerForEvents(this);
    }

    @Override
    protected void onSuspended() {
        super.onSuspended();
        mMoviesState.unregisterForEvents(this);
    }

    @Override
    protected MovieUiCallbacks createUiCallbacks() {
        return new MovieUiCallbacks() {

            @Override
            public void addFilter(Filter filter) {
                if (mMoviesState.getFilters().add(filter)) {
                    removeMutuallyExclusiveFilters(filter);
                    populateUi();
                }
            }

            @Override
            public void removeFilter(Filter filter) {
                if (mMoviesState.getFilters().remove(filter)) {
                    populateUi();
                }
            }

            @Override
            public void clearFilters() {
                if (!mMoviesState.getFilters().isEmpty()) {
                    mMoviesState.getFilters().clear();
                    populateUi();
                }
            }

            @Override
            public void refresh() {
                switch (getUi().getMovieQueryType()) {
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
                    display.showMovieDetailFragment(movie.getImdbId());
                }
            }

            @Override
            public void toggleMovieSeen(PhilmMovie movie) {
                if (movie.isWatched()) {
                    markMovieUnseen(movie.getImdbId());
                } else {
                    markMovieSeen(movie.getImdbId());
                }
            }

            @Override
            public void toggleInWatchlist(PhilmMovie movie) {
                if (movie.inWatchlist()) {
                    removeFromWatchlist(movie.getImdbId());
                } else {
                    addToWatchlist(movie.getImdbId());
                }
            }

            @Override
            public void toggleInCollection(PhilmMovie movie) {
                if (movie.inCollection()) {
                    removeFromCollection(movie.getImdbId());
                } else {
                    addToCollection(movie.getImdbId());
                }
            }
        };
    }

    @Override
    protected void onUiAttached() {
        super.onUiAttached();

        final MovieQueryType queryType = getUi().getMovieQueryType();

        if (queryType.requireLogin() && !isLoggedIn()) {
            Log.i(LOG_TAG, queryType.name() + " UI Attached but not logged in");
            return;
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
                fetchDetailMovieIfNeeded();
                break;
        }
    }

    @Override
    protected void populateUi() {
        super.populateUi();

        final MovieUi ui = getUi();

        if (!isLoggedIn() && ui.getMovieQueryType().requireLogin()) {
            ui.showError(NetworkError.UNAUTHORIZED);
            return;
        }

        if (ui instanceof MovieListUi) {
            populateListUi((MovieListUi) ui);
        } else if (ui instanceof MovieDetailUi) {
            populateDetailUi((MovieDetailUi) ui);
        }
    }

    private void populateListUi(MovieListUi ui) {
        final MovieQueryType queryType = ui.getMovieQueryType();

        if (isLoggedIn()) {
            if (queryType.supportFiltering()) {
                ui.setFiltersVisibility(true);
                ui.showActiveFilters(mMoviesState.getFilters());
            }
        } else {
            ui.setFiltersVisibility(false);
        }

        List<PhilmMovie> items;

        switch (queryType) {
            case TRENDING:
                items = mMoviesState.getTrending();
                if (requireFiltering()) {
                    items = filterMovies(items);
                }
                ui.setItems(items);
                break;
            case LIBRARY:
                items = mMoviesState.getLibrary();
                if (requireFiltering()) {
                    items = filterMovies(items);
                }
                ui.setItems(items);
                break;
            case WATCHLIST:
                items = mMoviesState.getWatchlist();
                if (requireFiltering()) {
                    items = filterMovies(items);
                }
                ui.setItemsWithSections(items, Arrays.asList(Filter.UPCOMING, Filter.SOON,
                        Filter.RELEASED, Filter.WATCHED));
                break;
        }
    }

    private void populateDetailUi(MovieDetailUi ui) {
        ui.setMovie(getMovie(ui.getRequestParameter()));
    }

    private PhilmMovie getMovie(String tmdbId) {
        return mMoviesState.getMovies().get(tmdbId);
    }

    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(mMoviesState.getUsername())
                && !TextUtils.isEmpty(mMoviesState.getHashedPassword());
    }

    private boolean requireFiltering() {
        return !PhilmCollections.isEmpty(mMoviesState.getFilters());
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

    private void fetchLibrary() {
        if (isLoggedIn()) {
            mExecutor.execute(new FetchLibraryRunnable(mMoviesState.getUsername()));
        }
    }

    private void fetchLibraryIfNeeded() {
        if (PhilmCollections.isEmpty(mMoviesState.getLibrary())) {
            fetchLibrary();
        }
    }

    private void fetchTrending() {
        mExecutor.execute(new FetchTrendingRunnable());
    }

    private void fetchTrendingIfNeeded() {
        if (PhilmCollections.isEmpty(mMoviesState.getTrending())) {
            fetchTrending();
        }
    }

    private void markMovieSeen(String imdbId) {
        mExecutor.execute(new MarkMovieSeenRunnable(imdbId));
    }

    private void markMovieUnseen(String imdbId) {
        mExecutor.execute(new MarkMovieUnseenRunnable(imdbId));
    }

    private void addToWatchlist(String imdbId) {
        mExecutor.execute(new AddMovieToWatchlistRunnable(imdbId));
    }

    private void removeFromWatchlist(String imdbId) {
        mExecutor.execute(new RemoveMovieFromWatchlistRunnable(imdbId));
    }

    private void addToCollection(String imdbId) {
        mExecutor.execute(new AddMovieToCollectionRunnable(imdbId));
    }

    private void removeFromCollection(String imdbId) {
        mExecutor.execute(new RemoveMovieFromCollectionRunnable(imdbId));
    }

    private void fetchWatchlist() {
        mExecutor.execute(new FetchWatchlistRunnable(mMoviesState.getUsername()));
    }

    private void fetchWatchlistIfNeeded() {
        if (PhilmCollections.isEmpty(mMoviesState.getWatchlist())) {
            fetchWatchlist();
        }
    }

    private void fetchDetailMovie() {
        fetchDetailMovie(getUi().getRequestParameter());
    }

    private void fetchDetailMovie(String imdbId) {
        Preconditions.checkNotNull(imdbId, "imdbId cannot be null");
        mExecutor.execute(new FetchDetailMovieRunnable(imdbId));
    }

    private void fetchDetailMovieIfNeeded() {
        PhilmMovie cached = getMovie(getUi().getRequestParameter());
        if (cached == null) {
            fetchDetailMovie();
        }
    }

    private void removeMutuallyExclusiveFilters(final Filter filter) {
        List<Filter> mutuallyExclusives = filter.getMutuallyExclusiveFilters();
        if (!PhilmCollections.isEmpty(mutuallyExclusives)) {
            for (Filter mutualFilter : mutuallyExclusives) {
                mMoviesState.getFilters().remove(mutualFilter);
            }
        }
    }

    private void showLoadingProgress(boolean show) {
        MovieUi ui = getUi();
        if (ui != null) {
            ui.showLoadingProgress(show);
        }
    }

    @Subscribe
    public void onLibraryChanged(MoviesState.LibraryChangedEvent event) {
        populateUi();
    }

    @Subscribe
    public void onTrendingChanged(MoviesState.TrendingChangedEvent event) {
        populateUi();
    }

    @Subscribe
    public void onWatchlistChanged(MoviesState.WatchlistChangedEvent event) {
        populateUi();
    }

    @Subscribe
    public void onAccountChanged(UserState.AccountChangedEvent event) {
        // Nuke all Movie State...
        mMoviesState.setLibrary(null);
        mMoviesState.setTrending(null);
        mMoviesState.setWatchlist(null);
        mMoviesState.getMovies().clear();
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

        PhilmMovie movie = stateMovies.get(rawMovie.imdb_id);
        if (movie != null) {
            // We already have a movie, so just update it wrapped value
            movie.setMovie(rawMovie);
        } else {
            // No movie, so create one
            movie = new PhilmMovie(rawMovie);
            stateMovies.put(movie.getImdbId(), movie);
        }

        return movie;
    }

    private class FetchTrendingRunnable extends BaseMovieTraktRunnable<List<Movie>> {
        public FetchTrendingRunnable() {
            super(mTraktClient);
        }

        @Override
        public List<Movie> doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.moviesService().trending();
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
            MovieUi ui = getUi();
            if (ui != null) {
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
                mMoviesState.setWatchlist(mapTraktMoviesFromState(result));
            } else {
                mMoviesState.setWatchlist(null);
            }
        }
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
            MovieUi ui = getUi();
            if (ui != null) {
                ui.showError(NetworkError.from(re));
            }
        }

        @Override
        public void onFinished() {
            showLoadingProgress(false);
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

        private void onActionCompleted(final boolean successful) {
            if (successful) {
                PhilmMovie movie = mMoviesState.getMovies().get(mImdbId);
                if (movie != null) {
                    movieRequiresModifying(movie);
                    checkPhilmState(movie);
                    populateUi();
                } else {
                    fetchDetailMovie(mImdbId);
                }
            }
        }

        protected abstract void movieRequiresModifying(PhilmMovie movie);
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
            checkPhilmState(mapTraktMovieFromState(result));

            // TODO: Should do something better here
            populateUi();
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
}
