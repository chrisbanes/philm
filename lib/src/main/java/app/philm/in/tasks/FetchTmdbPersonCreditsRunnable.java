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

import com.uwetrottmann.tmdb.entities.PersonCastCredit;
import com.uwetrottmann.tmdb.entities.PersonCredits;
import com.uwetrottmann.tmdb.entities.PersonCrewCredit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbPersonCreditsRunnable extends BaseMovieRunnable<PersonCredits> {

    private final int mId;

    public FetchTmdbPersonCreditsRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public PersonCredits doBackgroundCall() throws RetrofitError {
        return getTmdbClient().personService().movieCredits(mId);
    }

    @Override
    public void onSuccess(PersonCredits result) {
        PhilmPerson person = mMoviesState.getPerson(mId);

        if (person != null) {
            if (!PhilmCollections.isEmpty(result.cast)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCastCredit credit : result.cast) {
                    credits.add(new PhilmPersonCredit(credit));
                }
                Collections.sort(credits, PhilmPersonCredit.COMPARATOR_SORT_DATE);
                person.setCastCredits(credits);
            }

            if (!PhilmCollections.isEmpty(result.crew)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCrewCredit credit : result.crew) {
                    credits.add(new PhilmPersonCredit(credit));
                }
                Collections.sort(credits, PhilmPersonCredit.COMPARATOR_SORT_DATE);
                person.setCrewCredits(credits);
            }

            person.setFetchedCredits(true);

            getEventBus().post(new MoviesState.PersonChangedEvent(getCallingId(), person));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmPerson person = mMoviesState.getPerson(mId);
        if (person != null) {
            getEventBus().post(new MoviesState.PersonChangedEvent(getCallingId(), person));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

// TODO
//    @Override
//    protected Object createLoadingProgressEvent(boolean show) {
//        return new BaseState.ShowCreditLoadingProgressEvent(getCallingId(), show);
//    }
}