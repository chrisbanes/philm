package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbPopularRunnable extends BaseTmdbPaginatedMovieRunnable {

    public FetchTmdbPopularRunnable(int callingId, int page) {
        super(callingId, page);
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().popular(
                getPage(),
                getCountryProvider().getTwoLetterLanguageCode());
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