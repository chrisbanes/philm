package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.ReleasesResult;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.PhilmCollections;
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

        if (!PhilmCollections.isEmpty(result.countries)) {
            CountryRelease countryRelease = null;
            CountryRelease usRelease = null;

            for (CountryRelease release : result.countries) {
                if (countryCode != null && countryCode.equalsIgnoreCase(release.iso_3166_1)) {
                    countryRelease = release;
                    break;
                } else if (CountryProvider.US_TWO_LETTER_CODE
                        .equalsIgnoreCase(release.iso_3166_1)) {
                    usRelease = release;
                }
            }

            if (countryRelease == null) {
                countryRelease = usRelease;
            }

            if (countryRelease != null) {
                PhilmMovie movie = mMoviesState.getMovie(mId);
                if (movie != null) {
                    movie.updateFrom(countryRelease);
                    getDbHelper().put(movie);

                    getEventBus().post(
                            new MoviesState.MovieReleasesUpdatedEvent(getCallingId(), movie));
                }
            }
        }
    }
}