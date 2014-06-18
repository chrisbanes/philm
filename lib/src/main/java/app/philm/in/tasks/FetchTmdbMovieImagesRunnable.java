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


import com.uwetrottmann.tmdb.entities.Image;
import com.uwetrottmann.tmdb.entities.Images;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieImagesRunnable extends BaseMovieRunnable<Images> {

    private final int mId;

    public FetchTmdbMovieImagesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Images doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().images(mId);
    }

    @Override
    public void onSuccess(Images result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            if (!PhilmCollections.isEmpty(result.backdrops)) {
                List<PhilmMovie.BackdropImage> backdrops = new ArrayList<>();
                for (Image image : result.backdrops) {
                    backdrops.add(new PhilmMovie.BackdropImage(image));
                }
                movie.setBackdropImages(backdrops);
            }

            getEventBus().post(new MoviesState.MovieImagesUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

}