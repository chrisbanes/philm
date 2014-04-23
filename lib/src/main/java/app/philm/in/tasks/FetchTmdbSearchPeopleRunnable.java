package app.philm.in.tasks;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.uwetrottmann.tmdb.entities.PersonResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbSearchPeopleRunnable extends BaseTmdbPaginatedPersonRunnable {
    private final String mQuery;

    public FetchTmdbSearchPeopleRunnable(int callingId, String query, int page) {
        super(callingId, page);
        mQuery = Preconditions.checkNotNull(query, "query cannot be null");
    }

    @Override
    public PersonResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().searchService().person(mQuery, getPage(), null, null);
    }

    @Override
    protected MoviesState.PersonPaginatedResult getResultFromState() {
        MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
        return searchResult != null ? searchResult.people : null;
    }

    @Override
    protected void updateState(MoviesState.PersonPaginatedResult result) {
        MoviesState.SearchResult searchResult = mMoviesState.getSearchResult();
        if (searchResult != null && Objects.equal(mQuery, searchResult.query)) {
            searchResult.people = result;
            mMoviesState.setSearchResult(searchResult);
        }
    }

    @Override
    protected MoviesState.PersonPaginatedResult createPaginatedResult() {
        return new MoviesState.PersonPaginatedResult();
    }
}
