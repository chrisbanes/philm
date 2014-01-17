package app.philm.in.tasks;

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTraktWatchlistRunnable extends BaseMovieRunnable<List<Movie>> {

    private final String mUsername;

    public FetchTraktWatchlistRunnable(String username) {
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return mLazyTraktClient.get().userService().watchlistMovies(mUsername);
    }

    @Override
    public void onSuccessfulResult(List<Movie> result) {
        if (!PhilmCollections.isEmpty(result)) {
            List<PhilmMovie> movies = mLazyTraktMovieEntityMapper.get().map(result);
            mMoviesState.setWatchlist(movies);
            mDbHelper.get().mergeWatchlist(movies);
        } else {
            mMoviesState.setWatchlist(null);
        }
    }
}