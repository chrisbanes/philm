package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Movie;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

public class FetchTmdbDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final int mId;

    public FetchTmdbDetailMovieRunnable(int id) {
        mId = id;
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return mLazyTmdbClient.get().moviesService().summary(mId);
    }

    @Override
    public void onSuccessfulResult(Movie result) {
        PhilmMovie movie = mLazyTmdbMovieEntityMapper.get().map(result);
        checkPhilmState(movie);
        mDbHelper.get().put(movie);

        if (hasCallback()) {
            getCallback().populateUis();
        }

        // TODO: checkDetailMovieResult(movie);
    }
}