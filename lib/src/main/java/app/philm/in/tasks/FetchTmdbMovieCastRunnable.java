package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Credits;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbMovieCastRunnable extends BaseMovieRunnable<Credits> {

    private final int mId;

    public FetchTmdbMovieCastRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Credits doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().credits(mId);
    }

    @Override
    public void onSuccess(Credits result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            movie.updateWithCast(result);

            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowCastLoadingProgressEvent(getCallingId(), show);
    }
}