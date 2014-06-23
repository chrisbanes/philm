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

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.services.MovieService;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class MarkTraktMovieUnseenRunnable extends BaseTraktActionRunnable {

    public MarkTraktMovieUnseenRunnable(int callingId, String... ids) {
        super(callingId, ids);
    }

    @Override
    public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
        return trakt.movieService().unseen(body);
    }

    @Override
    protected void movieRequiresModifying(PhilmMovie movie) {
        movie.setWatched(false);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}