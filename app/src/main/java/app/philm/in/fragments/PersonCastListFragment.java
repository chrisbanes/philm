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

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.text.TextUtils;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePersonCreditListFragment;

public class PersonCastListFragment extends BasePersonCreditListFragment {

    private static final String KEY_QUERY_PERSON_ID = "person_id";

    public static PersonCastListFragment create(String personId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(personId), "personId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_PERSON_ID, personId);

        PersonCastListFragment fragment = new PersonCastListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.PERSON_CREDITS_CAST;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_PERSON_ID);
    }
}
