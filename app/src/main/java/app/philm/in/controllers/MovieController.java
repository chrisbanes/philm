package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.otto.Subscribe;

import java.util.List;

import app.philm.in.Display;
import app.philm.in.state.MoviesState;

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

    public MovieController(Display display, MoviesState movieState) {
        super();
        mDisplay = Preconditions.checkNotNull(display, "display cannot be null");
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");
    }

    @Override
    protected void onInited() {
        super.onInited();
        mMoviesState.registerForEvents(this);

        if (!mMoviesState.hasCollection()) {
            // Do Collection Request
        }
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
        MovieUi ui = getUi();
        if (ui != null) {
            ui.setCollection(mMoviesState.getCollection());
        }
    }

    @Subscribe
    public void collectionChange(MoviesState.CollectionChangedEvent event) {
        populateUi();
    }

}
