package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.otto.Subscribe;

import java.util.List;

import app.philm.in.state.MoviesState;

public class MovieController extends BaseUiController<MovieController.MovieUi,
        MovieController.MovieUiCallbacks> {

    public interface MovieUi extends BaseUiController.Ui<MovieUiCallbacks> {
        void setCollection(List<Movie> collection);
    }

    public interface MovieUiCallbacks {
    }

    private final MovieUiCallbacks mUiCallbacks;
    private final MoviesState mMoviesState;

    public MovieController(MoviesState movieState) {
        super();
        mMoviesState = Preconditions.checkNotNull(movieState, "moviesState cannot be null");

        mUiCallbacks = new MovieUiCallbacks() {
        };
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
    protected MovieUiCallbacks getUiCallbacks() {
        return mUiCallbacks;
    }

    @Override
    protected void populateUi() {
        if (getUi() != null) {
            getUi().setCollection(mMoviesState.getCollection());
        }
    }

    @Subscribe
    public void collectionChange(MoviesState.CollectionChangedEvent event) {
        populateUi();
    }

}
