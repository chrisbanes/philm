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

package app.philm.in.state.mappers;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;
import app.philm.in.util.TextUtils;

abstract class MovieEntityMapper<T> extends BaseEntityMapper<T, PhilmMovie> {

    public MovieEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    PhilmMovie getEntity(String id) {
        if (mMoviesState.getImdbIdMovies().containsKey(id)) {
            return mMoviesState.getImdbIdMovies().get(id);
        } else if (mMoviesState.getTmdbIdMovies().containsKey(id)) {
            return mMoviesState.getTmdbIdMovies().get(id);
        }
        return null;
    }

    @Override
    void putEntity(PhilmMovie movie) {
        if (!TextUtils.isEmpty(movie.getImdbId())) {
            mMoviesState.getImdbIdMovies().put(movie.getImdbId(), movie);
        }
        if (movie.getTmdbId() != null) {
            mMoviesState.getTmdbIdMovies().put(String.valueOf(movie.getTmdbId()), movie);
        }
    }

}
