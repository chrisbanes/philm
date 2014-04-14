package app.philm.in.lib.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import app.philm.in.lib.model.PhilmModel;
import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.network.NetworkError;
import app.philm.in.lib.state.MoviesState;
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
        PhilmMovie movie = getTraktMovieEntityMapper().map(result);
        movie.markFullFetchCompleted(PhilmModel.TYPE_TRAKT);

        checkPhilmState(movie);
        getDbHelper().put(movie);

        getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
    }

    @Override
    public void onError(RetrofitError re) {
        if (re.getResponse() != null && re.getResponse().getStatus() == 404) {
            PhilmMovie movie = mMoviesState.getMovie(mId);
            if (movie != null) {
                getDbHelper().put(movie);
                getEventBus().post(
                        new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
            }
        }
        super.onError(re);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}