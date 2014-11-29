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
import com.uwetrottmann.tmdb.entities.Person;
import com.uwetrottmann.tmdb.entities.PersonCastCredit;
import com.uwetrottmann.tmdb.entities.PersonCrewCredit;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbPersonRunnable extends BaseMovieRunnable<Person> {

    private final int mId;

    public FetchTmdbPersonRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Person doBackgroundCall() throws RetrofitError {
        return getTmdbClient().personService().summary(
                mId,
                new AppendToResponse(AppendToResponseItem.MOVIE_CREDITS)
        );
    }

    @Override
    public void onSuccess(Person result) {
        PhilmPerson person = getTmdbPersonEntityMapper().map(result);

        if (person != null && result.movie_credits != null) {

            if (!PhilmCollections.isEmpty(result.movie_credits.cast)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCastCredit credit : result.movie_credits.cast) {
                    credits.add(new PhilmPersonCredit(credit));
                }
                Collections.sort(credits, PhilmPersonCredit.COMPARATOR_SORT_DATE);
                person.setCastCredits(credits);
            }

            if (!PhilmCollections.isEmpty(result.movie_credits.crew)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCrewCredit credit : result.movie_credits.crew) {
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