package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.Releases;

import javax.inject.Inject;

import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.network.NetworkError;
import app.philm.in.lib.state.MoviesState;
import app.philm.in.lib.util.CountryProvider;
import retrofit.RetrofitError;

public class FetchTmdbMoviesReleasesRunnable extends BaseMovieRunnable<Releases> {

    @Inject CountryProvider mCountryProvider;
    private final int mId;

    public FetchTmdbMoviesReleasesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Releases doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().releases(mId);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    public void onSuccess(Releases result) {
        final String countryCode = mCountryProvider.getTwoLetterCountryCode();

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            movie.updateWithReleases(result, countryCode);

            getDbHelper().put(movie);

            getEventBus().post(new MoviesState.MovieReleasesUpdatedEvent(getCallingId(), movie));
        }
    }
}