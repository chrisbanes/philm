package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

public class FetchTmdbRelatedMoviesRunnable extends BaseMovieRunnable<ResultsPage> {

    private final int mId;

    public FetchTmdbRelatedMoviesRunnable(int id) {
        mId = id;
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return mLazyTmdbClient.get().moviesService().similarMovies(mId);
    }

    @Override
    public void onSuccessfulResult(ResultsPage result) {
        PhilmMovie movie = mMoviesState.getMovie(String.valueOf(mId));
        movie.setRelated(mLazyTmdbMovieEntityMapper.get().map(result.results));

        if (hasCallback()) {
            getCallback().populateUis();
        }
    }

}