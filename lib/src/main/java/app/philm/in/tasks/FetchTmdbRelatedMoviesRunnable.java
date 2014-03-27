package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbRelatedMoviesRunnable extends BaseMovieRunnable<ResultsPage> {

    private final int mId;

    public FetchTmdbRelatedMoviesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().similarMovies(mId);
    }

    @Override
    public void onSuccess(ResultsPage result) {
        PhilmMovie movie = mMoviesState.getMovie(String.valueOf(mId));

        if (movie != null) {
            movie.setRelated(getTmdbMovieEntityMapper().mapAll(result.results));

            getEventBus().post(new MoviesState.MovieRelatedItemsUpdatedEvent(
                    getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(String.valueOf(mId));
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieRelatedItemsUpdatedEvent(
                    getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowRelatedLoadingProgressEvent(getCallingId(), show);
    }
}