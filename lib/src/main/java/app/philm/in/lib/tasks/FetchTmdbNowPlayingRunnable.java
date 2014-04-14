package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.lib.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbNowPlayingRunnable extends BaseTmdbPaginatedMovieRunnable {

    public FetchTmdbNowPlayingRunnable(int callingId, int page) {
        super(callingId, page);
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().nowPlaying(getPage(), null);
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