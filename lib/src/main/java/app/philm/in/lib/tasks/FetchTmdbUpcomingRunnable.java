package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.lib.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbUpcomingRunnable extends BaseTmdbPaginatedMovieRunnable {

    public FetchTmdbUpcomingRunnable(int callingId, int page) {
        super(callingId, page);
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().upcoming(getPage(), null);
    }

    @Override
    protected MoviesState.MoviePaginatedResult getResultFromState() {
        return mMoviesState.getUpcoming();
    }

    @Override
    protected void updateState(MoviesState.MoviePaginatedResult result) {
        mMoviesState.setUpcoming(result);
    }
}