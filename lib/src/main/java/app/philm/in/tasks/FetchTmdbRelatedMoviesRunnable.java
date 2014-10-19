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
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTmdbRelatedMoviesRunnable extends BaseMovieRunnable<MovieResultsPage> {

    private final int mId;

    public FetchTmdbRelatedMoviesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public MovieResultsPage doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().similarMovies(mId,
                null,
                getCountryProvider().getTwoLetterLanguageCode());
    }

    @Override
    public void onSuccess(MovieResultsPage result) {
        PhilmMovie movie = mMoviesState.getMovie(String.valueOf(mId));

        if (movie != null) {
            movie.setRelated(getTmdbMovieEntityMapper().mapAll(result.results));

            getEventBus().post(new MoviesState.MovieRelatedItemsUpdatedEvent(
                    getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(String.valueOf(mId));
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieRelatedItemsUpdatedEvent(
                    getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowRelatedLoadingProgressEvent(getCallingId(), show);
    }
}