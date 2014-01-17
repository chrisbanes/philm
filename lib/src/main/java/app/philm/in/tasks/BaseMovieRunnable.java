package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.Trakt;
import com.uwetrottmann.tmdb.Tmdb;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkCallRunnable;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import dagger.Lazy;
import retrofit.RetrofitError;

abstract class BaseMovieRunnable<R> extends NetworkCallRunnable<R> {

    @Inject Lazy<Tmdb> mLazyTmdbClient;
    @Inject Lazy<Trakt> mLazyTraktClient;
    @Inject MoviesState mMoviesState;

    @Inject Lazy<MoviesState.TraktMovieEntityMapper> mLazyTraktMovieEntityMapper;
    @Inject Lazy<MoviesState.TmdbMovieEntityMapper> mLazyTmdbMovieEntityMapper;

    private final WeakReference<MovieController.MovieUi> mMovieUiWeakReference;

    public BaseMovieRunnable(MovieController.MovieUi ui) {
        Preconditions.checkNotNull(ui, "ui cannot be null");
        mMovieUiWeakReference = new WeakReference<MovieController.MovieUi>(ui);
    }

    @Override
    public void onPreTraktCall() {
        MovieController.MovieUi ui = getUi();
        if (ui != null) {
            ui.showLoadingProgress(true);
        }
    }

    @Override
    public void onError(RetrofitError re) {
        MovieController.MovieUi ui = getUi();
        if (ui != null) {
            ui.showError(NetworkError.from(re));
        }
    }

    @Override
    public void onFinished() {
        MovieController.MovieUi ui = getUi();
        if (ui != null) {
            ui.showLoadingProgress(false);
        }
    }

    protected MovieController.MovieUi getUi() {
        return mMovieUiWeakReference.get();
    }
}