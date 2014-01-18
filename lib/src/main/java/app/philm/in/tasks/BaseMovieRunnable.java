package app.philm.in.tasks;

import com.jakewharton.trakt.Trakt;
import com.squareup.otto.Bus;
import com.uwetrottmann.tmdb.Tmdb;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkCallRunnable;
import app.philm.in.network.NetworkError;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import dagger.Lazy;
import retrofit.RetrofitError;

public abstract class BaseMovieRunnable<R> extends NetworkCallRunnable<R> {

    @Inject MoviesState mMoviesState;

    @Inject Lazy<Tmdb> mLazyTmdbClient;
    @Inject Lazy<Trakt> mLazyTraktClient;
    @Inject Lazy<AsyncDatabaseHelper> mDbHelper;
    @Inject Lazy<MoviesState.TraktMovieEntityMapper> mLazyTraktMovieEntityMapper;
    @Inject Lazy<MoviesState.TmdbMovieEntityMapper> mLazyTmdbMovieEntityMapper;
    @Inject Lazy<Bus> mEventBus;


    private final int mCallingId;
    private MovieTaskCallback mCallback;

    public BaseMovieRunnable(int callingId) {
        mCallingId = callingId;
    }

    protected boolean hasCallback() {
        return mCallback != null;
    }

    public void setCallback(MovieTaskCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onPreTraktCall() {
        if (mCallback != null) {
            mCallback.showLoadingProgress(true);
        }
    }

    @Override
    public void onError(RetrofitError re) {
        if (mCallback != null) {
            mCallback.showError(NetworkError.from(re));
        }
    }

    @Override
    public void onFinished() {
        if (mCallback != null) {
            mCallback.showLoadingProgress(false);
        }
    }

    protected MovieTaskCallback getCallback() {
        return mCallback;
    }

    protected void checkPhilmState(PhilmMovie movie) {
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

    protected Tmdb getTmdbClient() {
        return mLazyTmdbClient.get();
    }

    protected Trakt getTraktClient() {
        return mLazyTraktClient.get();
    }

    protected AsyncDatabaseHelper getDbHelper() {
        return mDbHelper.get();
    }

    protected MoviesState.TraktMovieEntityMapper getTraktEntityMapper() {
        return mLazyTraktMovieEntityMapper.get();
    }

    protected MoviesState.TmdbMovieEntityMapper getTmdbEntityMapper() {
        return mLazyTmdbMovieEntityMapper.get();
    }

    protected Bus getEventBus() {
        return mEventBus.get();
    }

    protected int getCallingId() {
        return mCallingId;
    }
}