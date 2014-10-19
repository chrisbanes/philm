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

import com.uwetrottmann.tmdb.entities.MovieResultsPage;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;

abstract class BaseTmdbPaginatedMovieRunnable extends BaseTmdbPaginatedRunnable<
        MoviesState.MoviePaginatedResult, PhilmMovie, MovieResultsPage> {

    BaseTmdbPaginatedMovieRunnable(int callingId, int page) {
        super(callingId, page);
    }

    @Override
    protected void updatePaginatedResult(
            MoviesState.MoviePaginatedResult result,
            MovieResultsPage tmdbResult) {
        result.items.addAll(getTmdbMovieEntityMapper().mapAll(tmdbResult.results));

        result.page = tmdbResult.page;
        if (tmdbResult.total_pages != null) {
            result.totalPages = tmdbResult.total_pages;
        }
    }

    @Override
    protected MoviesState.MoviePaginatedResult createPaginatedResult() {
        return new MoviesState.MoviePaginatedResult();
    }
}