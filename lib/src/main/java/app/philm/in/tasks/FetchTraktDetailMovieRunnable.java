package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTraktDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final String mId;

    public FetchTraktDetailMovieRunnable(int callingId, String imdbId) {
        super(callingId);
        mId = Preconditions.checkNotNull(imdbId, "id cannot be null");
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return getTraktClient().movieService().summary(mId);
    }

    @Override
    public void onSuccess(Movie result) {
        PhilmMovie movie = getTraktEntityMapper().map(result);
        checkPhilmState(movie);
        getDbHelper().put(movie);

        getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
    }
}