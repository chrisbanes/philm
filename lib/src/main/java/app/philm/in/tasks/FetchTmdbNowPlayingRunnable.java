package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.controllers.MovieController;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbNowPlayingRunnable extends BaseTmdbPaginatedRunnable {

    FetchTmdbNowPlayingRunnable(MovieController.MovieUi ui, int page) {
        super(ui, page);
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return mLazyTmdbClient.get().moviesService().nowPlaying(getPage(), null);
    }

    @Override
    protected MoviesState.MoviePaginatedResult getResultFromState() {
        return mMoviesState.getNowPlaying();
    }

    @Override
    protected void updateState(MoviesState.MoviePaginatedResult result) {
        mMoviesState.setNowPlaying(result);
    }
}