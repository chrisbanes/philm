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

import com.jakewharton.trakt.entities.Response;

import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class CancelCheckinTraktRunnable extends BaseMovieRunnable<Response> {

    public CancelCheckinTraktRunnable(int callingId) {
        super(callingId);
    }

    @Override
    public Response doBackgroundCall() throws RetrofitError {
        return getTraktClient().movieService().cancelcheckin();
    }

    @Override
    public void onSuccess(Response result) {
        if (RESULT_TRAKT_SUCCESS.equals(result.status)) {
            mMoviesState.setWatchingMovie(null);
        } else if (RESULT_TRAKT_FAILURE.equals(result.status)) {
            // TODO
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
