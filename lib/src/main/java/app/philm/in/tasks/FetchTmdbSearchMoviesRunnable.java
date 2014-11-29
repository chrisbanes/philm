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

import com.uwetrottmann.tmdb.entities.MovieResultsPage;

import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbSearchMoviesRunnable extends BaseTmdbPaginatedMovieRunnable {
    private final String mQuery;

    public FetchTmdbSearchMoviesRunnable(int callingId, String query, int page) {
        super(callingId, page);
        mQuery = Preconditions.checkNotNull(query, "query cannot be null");
    }

    @Override
    public MovieResultsPage doBackgroundCall() throws RetrofitError {
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
