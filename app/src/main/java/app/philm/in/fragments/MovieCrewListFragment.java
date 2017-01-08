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

package app.philm.in.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseMovieCreditListFragment;

public class MovieCrewListFragment extends BaseMovieCreditListFragment {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    public static MovieCrewListFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        MovieCrewListFragment fragment = new MovieCrewListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.MOVIE_CREW;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_MOVIE_ID);
    }
}
