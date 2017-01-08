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


import com.uwetrottmann.tmdb.entities.CastMember;
import com.uwetrottmann.tmdb.entities.Credits;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieCreditsRunnable extends BaseMovieRunnable<Credits> {

    private final int mId;

    public FetchTmdbMovieCreditsRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Credits doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().credits(mId);
    }

    @Override
    public void onSuccess(Credits result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            if (!PhilmCollections.isEmpty(result.cast)) {
                // Sort the Cast based on order first
                Collections.sort(result.cast, new Comparator<CastMember>() {
                    @Override
                    public int compare(CastMember castMember, CastMember castMember2) {
                        return castMember.order - castMember2.order;
                    }
                });
                movie.setCast(getTmdbCastEntityMapper().mapCredits(result.cast));
            }

            if (!PhilmCollections.isEmpty(result.crew)) {
                List<PhilmMovieCredit> crew = getTmdbCrewEntityMapper().mapCredits(result.crew);
                Collections.sort(crew);
                movie.setCrew(crew);
            }

            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowCreditLoadingProgressEvent(getCallingId(), show);
    }
}