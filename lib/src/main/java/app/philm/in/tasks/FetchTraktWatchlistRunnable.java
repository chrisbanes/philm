package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTraktWatchlistRunnable extends BaseMovieRunnable<List<Movie>> {

    private final String mUsername;

    public FetchTraktWatchlistRunnable(int callingId, String username) {
        super(callingId);
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return getTraktClient().userService().watchlistMovies(mUsername);
    }

    @Override
    public void onSuccess(List<Movie> result) {
        if (!PhilmCollections.isEmpty(result)) {
            List<PhilmMovie> movies = getTraktMovieEntityMapper().map(result);
            mMoviesState.setWatchlist(movies);
            getDbHelper().mergeWatchlist(movies);
        } else {
            mMoviesState.setWatchlist(null);
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}