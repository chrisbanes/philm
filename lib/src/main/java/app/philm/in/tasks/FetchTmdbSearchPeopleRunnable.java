/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
