package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ReleasesResult;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.CountryProvider;
import retrofit.RetrofitError;

public class FetchTmdbMoviesReleasesRunnable extends BaseMovieRunnable<ReleasesResult> {

    @Inject CountryProvider mCountryProvider;
    private final int mId;

    public FetchTmdbMoviesReleasesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public ReleasesResult doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().releases(mId);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    public void onSuccess(ReleasesResult result) {
        final String countryCode = mCountryProvider.getTwoLetterCountryCode();

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            movie.updateWithReleases(result, countryCode);

            getDbHelper().put(movie);

            getEventBus().post(new MoviesState.MovieReleasesUpdatedEvent(getCallingId(), movie));
        }
    }
}