package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbPopularRunnable extends BaseTmdbPaginatedRunnable {

    public FetchTmdbPopularRunnable(int page) {
        super(page);
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return mLazyTmdbClient.get().moviesService().popular(getPage(), null);
    }

    @Override
    protected MoviesState.MoviePaginatedResult getResultFromState() {
        return mMoviesState.getPopular();
    }

    @Override
    protected void updateState(MoviesState.MoviePaginatedResult result) {
        mMoviesState.setPopular(result);
    }
}