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

import com.jakewharton.trakt.entities.WatchingBase;
import com.jakewharton.trakt.enumerations.ActivityType;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.WatchingMovie;
import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class FetchTraktWatchingRunnable extends BaseMovieRunnable<WatchingBase> {

    private final String mUsername;

    public FetchTraktWatchingRunnable(int callingId, String username) {
        super(callingId);
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public WatchingBase doBackgroundCall() throws RetrofitError {
        return getTraktClient().userService().watching(mUsername);
    }

    @Override
    public void onSuccess(WatchingBase result) {
        if (result.type == ActivityType.Movie && WatchingMovie.validAction(result.action)) {

            PhilmMovie movie = getTraktMovieEntityMapper().map(result.movie);
            if (movie != null) {
                // TODO Fix timestamps
                WatchingMovie watching = new WatchingMovie(movie,
                        WatchingMovie.from(result.action), 0, 0);
                mMoviesState.setWatchingMovie(watching);
            }
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
