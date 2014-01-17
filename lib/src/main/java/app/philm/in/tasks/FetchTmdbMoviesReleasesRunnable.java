package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.ReleasesResult;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMoviesReleasesRunnable extends BaseMovieRunnable<ReleasesResult> {

    @Inject CountryProvider mCountryProvider;
    private final int mId;

    public FetchTmdbMoviesReleasesRunnable(int id) {
        mId = id;
    }

    @Override
    public ReleasesResult doBackgroundCall() throws RetrofitError {
        return mLazyTmdbClient.get().moviesService().releases(mId);
    }

    @Override
    public void onSuccessfulResult(ReleasesResult result) {
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
                    mDbHelper.get().put(movie);

                    if (hasCallback()) {
                        getCallback().populateUis();
                    }
                }
            }
        }
    }
}