package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.PersonCredits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.philm.in.lib.model.PhilmPerson;
import app.philm.in.lib.model.PhilmPersonCredit;
import app.philm.in.lib.network.NetworkError;
import app.philm.in.lib.state.MoviesState;
import app.philm.in.lib.util.PhilmCollections;
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
                for (PersonCredits.CastCredit credit : result.cast) {
                    credits.add(new PhilmPersonCredit(credit));
                }
                Collections.sort(credits, PhilmPersonCredit.COMPARATOR_SORT_DATE);
                person.setCastCredits(credits);
            }

            if (!PhilmCollections.isEmpty(result.crew)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCredits.CrewCredit credit : result.crew) {
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