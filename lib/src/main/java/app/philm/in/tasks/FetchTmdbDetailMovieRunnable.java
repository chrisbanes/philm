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


import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.Movie;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

import app.philm.in.model.PhilmModel;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final int mId;

    public FetchTmdbDetailMovieRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().summary(mId,
                getCountryProvider().getTwoLetterLanguageCode(),
                new AppendToResponse(
                        AppendToResponseItem.CREDITS,
                        AppendToResponseItem.RELEASES,
                        AppendToResponseItem.VIDEOS,
                        AppendToResponseItem.SIMILAR
                )
        );
    }

    @Override
    public void onSuccess(Movie result) {
        PhilmMovie movie = getTmdbMovieEntityMapper().map(result);
        movie.markFullFetchCompleted(PhilmModel.TYPE_TMDB);

        // Need to manually update releases here due to country code
        if (result.releases != null) {
            movie.updateWithReleases(result.releases,
                    getCountryProvider().getTwoLetterCountryCode());
        }

        // Need to manually update releases here due to entity mapper
        if (result.similar_movies != null) {
            movie.setRelated(getTmdbMovieEntityMapper().mapAll(result.similar_movies.results));
        }

        if (result.credits != null && !PhilmCollections.isEmpty(result.credits.cast)) {
            movie.setCast(getTmdbCastEntityMapper().mapCredits(result.credits.cast));
        }

        if (result.credits != null && !PhilmCollections.isEmpty(result.credits.crew)) {
            movie.setCrew(getTmdbCrewEntityMapper().mapCredits(result.credits.crew));
        }

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
                getEventBus()
                        .post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
            }
        }
        super.onError(re);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }
}