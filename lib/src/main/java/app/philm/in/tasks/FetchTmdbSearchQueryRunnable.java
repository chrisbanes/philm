package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbSearchQueryRunnable extends BaseTmdbPaginatedRunnable {
    private final String mQuery;

    public FetchTmdbSearchQueryRunnable(String query, int page) {
        super(page);
        mQuery = Preconditions.checkNotNull(query, "query cannot be null");
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().searchService()
                .movie(mQuery, getPage(), null, null, null, null, null);
    }

    @Override
    protected MoviesState.MoviePaginatedResult getResultFromState() {
        return mMoviesState.getSearchResult();
    }

    @Override
    protected void updateState(MoviesState.MoviePaginatedResult result) {
        mMoviesState.setSearchResult((MoviesState.SearchPaginatedResult) result);
    }

    @Override
    protected MoviesState.MoviePaginatedResult createPaginatedResult() {
        MoviesState.SearchPaginatedResult result = new MoviesState.SearchPaginatedResult();
        result.query = mQuery;
        return result;
    }
}
