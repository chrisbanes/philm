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

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import app.philm.in.model.PhilmModel;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTraktDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final String mId;

    public FetchTraktDetailMovieRunnable(int callingId, String imdbId) {
        super(callingId);
        mId = Preconditions.checkNotNull(imdbId, "id cannot be null");
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return getTraktClient().movieService().summary(mId);
    }

    @Override
    public void onSuccess(Movie result) {
        PhilmMovie movie = getTraktMovieEntityMapper().map(result);
        movie.markFullFetchCompleted(PhilmModel.TYPE_TRAKT);

        checkPhilmState(movie);
        getDbHelper().put(movie);

        getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
    }

    @Override
    public void onError(RetrofitError re) {
        if (re.getResponse() != null && re.getResponse().getStatus() == 404) {
            PhilmMovie movie = mMoviesState.getMovie(mId);
            if (movie != null) {
                getDbHelper().put(movie);
                getEventBus().post(
                        new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
            }
        }
        super.onError(re);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}