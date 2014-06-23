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

import com.uwetrottmann.tmdb.entities.Releases;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.CountryProvider;
import retrofit.RetrofitError;

public class FetchTmdbMoviesReleasesRunnable extends BaseMovieRunnable<Releases> {

    @Inject CountryProvider mCountryProvider;
    private final int mId;

    public FetchTmdbMoviesReleasesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Releases doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().releases(mId);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    public void onSuccess(Releases result) {
        final String countryCode = mCountryProvider.getTwoLetterCountryCode();

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            movie.updateWithReleases(result, countryCode);

            getDbHelper().put(movie);

            getEventBus().post(new MoviesState.MovieReleasesUpdatedEvent(getCallingId(), movie));
        }
    }
}