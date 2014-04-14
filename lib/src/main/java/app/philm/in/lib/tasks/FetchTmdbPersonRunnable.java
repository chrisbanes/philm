package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.Person;
import com.uwetrottmann.tmdb.entities.PersonCredits;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.philm.in.lib.model.PhilmPerson;
import app.philm.in.lib.model.PhilmPersonCredit;
import app.philm.in.lib.network.NetworkError;
import app.philm.in.lib.state.MoviesState;
import app.philm.in.lib.util.PhilmCollections;
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
                for (PersonCredits.CastCredit credit : result.movie_credits.cast) {
                    credits.add(new PhilmPersonCredit(credit));
                }
                Collections.sort(credits, PhilmPersonCredit.COMPARATOR_SORT_DATE);
                person.setCastCredits(credits);
            }

            if (!PhilmCollections.isEmpty(result.movie_credits.crew)) {
                List<PhilmPersonCredit> credits = new ArrayList<>();
                for (PersonCredits.CrewCredit credit : result.movie_credits.crew) {
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