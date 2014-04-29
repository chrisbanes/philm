package app.philm.in.tasks;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbSearchMoviesRunnable extends BaseTmdbPaginatedMovieRunnable {
    private final String mQuery;

    public FetchTmdbSearchMoviesRunnable(int callingId, String query, int page) {
        super(callingId, page);
        mQuery = Preconditions.checkNotNull(query, "query cannot be null");
    }

    @Override
    public ResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().searchService().movie(
                mQuery,
                getPage(),
                getCountryProvider().getTwoLetterLanguageCode(),
                null,
                null,
                null,
                null);
    }

    @Override
    protected MoviesState.MoviePaginatedResult getResultFromState() {
        MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
        return searchResult != null ? searchResult.movies : null;
    }

    @Override
    protected void updateState(MoviesState.MoviePaginatedResult result) {
        MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
        if (searchResult != null && Objects.equal(mQuery, searchResult.query)) {
            searchResult.movies = result;
            mMoviesState.setSearchResult(searchResult);
        }
    }

    @Override
    protected MoviesState.MoviePaginatedResult createPaginatedResult() {
        return new MoviesState.MoviePaginatedResult();
    }
}
