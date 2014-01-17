package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Movie;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final int mId;

    public FetchTmdbDetailMovieRunnable(int id) {
        mId = id;
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().summary(mId);
    }

    @Override
    public void onSuccess(Movie result) {
        PhilmMovie movie = getTmdbEntityMapper().map(result);
        checkPhilmState(movie);
        getDbHelper().put(movie);

        getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(movie));
    }
}