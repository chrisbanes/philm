package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.PersonCredits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.philm.in.model.Person;
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
        Person person = mMoviesState.getPerson(mId);

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

            getEventBus().post(new MoviesState.PersonCreditsChangedEvent(getCallingId(), person));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        Person person = mMoviesState.getPerson(mId);
        if (person != null) {
            getEventBus().post(new MoviesState.PersonCreditsChangedEvent(getCallingId(), person));
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