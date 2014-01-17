package app.philm.in.tasks;

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.entities.Movie;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

public class FetchTraktDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final String mId;

    public FetchTraktDetailMovieRunnable(String imdbId) {
        mId = Preconditions.checkNotNull(imdbId, "id cannot be null");
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return mLazyTraktClient.get().movieService().summary(mId);
    }

    @Override
    public void onSuccess(Movie result) {
        PhilmMovie movie = mLazyTraktMovieEntityMapper.get().map(result);
        checkPhilmState(movie);
        mDbHelper.get().put(movie);

        if (hasCallback()) {
            getCallback().populateUis();
        }
        checkDetailMovieResult(movie);
    }
}