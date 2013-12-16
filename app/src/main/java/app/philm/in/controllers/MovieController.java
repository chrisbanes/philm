package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.ExecutorService;

import app.philm.in.Display;
import app.philm.in.state.MoviesState;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.BackgroundRunnable;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    public interface MovieUi extends BaseUiController.Ui<MovieUiCallbacks> {
        void setCollection(List<Movie> collection);
    }

    public interface MovieUiCallbacks {
    }

    public interface MovieControllerProvider {
        MovieController getMovieController();
    }

    private final MoviesState mMoviesState;
    private final Display mDisplay;
    private final ExecutorService mExecutor;
    private final Trakt mTraktClient;

    public MovieController(Display display,
            MoviesState movieState,
            Trakt traktClient,
            ExecutorService executor) {
        super();
        mDisplay = Preconditions.checkNotNull(display, "display cannot be null");
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
        };
    }

    @Override
    protected void populateUi() {
        List<Movie> library = mMoviesState.getLibrary();

        if (library == null || library.isEmpty()) {
            fetchLibrary();
        }

        getUi().setCollection(library);
    }

    private void fetchLibrary() {
        mExecutor.execute(new FetchLibraryRunnable());
    }

    @Subscribe
    public void onCollectionChange(MoviesState.LibraryChangedEvent event) {
        populateUi();
    }

    private class FetchLibraryRunnable extends BackgroundRunnable<List<Movie>> {
        @Override
        public List<Movie> runAsync() {
            return mTraktClient.moviesService().trending();
        }

        @Override
        public void postExecute(List<Movie> result) {
            mMoviesState.setLibrary(result);
        }
    }
}
