package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.Movie;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.CountryProvider;
import retrofit.RetrofitError;

public class FetchTmdbDetailMovieRunnable extends BaseMovieRunnable<Movie> {

    private final int mId;
    @Inject CountryProvider mCountryProvider;

    public FetchTmdbDetailMovieRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Movie doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().summary(mId,
                null,
                new AppendToResponse(
                        AppendToResponseItem.CREDITS,
                        AppendToResponseItem.RELEASES,
                        AppendToResponseItem.TRAILERS,
                        AppendToResponseItem.SIMILAR
                )
        );
    }

    @Override
     public void onSuccess(Movie result) {
        PhilmMovie movie = getTmdbEntityMapper().map(result);

        // Need to manually update releases here due to country code
        if (result.releases != null) {
            movie.updateWithReleases(result.releases, mCountryProvider.getTwoLetterCountryCode());
        }

        // Need to manually update releases here due to entity mapper
        if (result.similar_movies != null) {
            movie.setRelated(getTmdbEntityMapper().map(result.similar_movies.results));
        }

        checkPhilmState(movie);

        getDbHelper().put(movie);

        getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
    }

    @Override
    public void onError(RetrofitError re) {
        if (re.getResponse() != null && re.getResponse().getStatus() == 404) {
            PhilmMovie movie = mMoviesState.getMovie(mId);
            if (movie != null) {
                movie.setLoadedFromTmdb(false);
                getDbHelper().put(movie);
                getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
            }
        }
        super.onError(re);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }
}