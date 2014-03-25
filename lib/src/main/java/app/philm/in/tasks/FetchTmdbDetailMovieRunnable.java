package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.Credits;
import com.uwetrottmann.tmdb.entities.Movie;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import app.philm.in.model.PhilmMovieCrewCredit;
import app.philm.in.model.PhilmModel;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.PhilmCollections;
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
        PhilmMovie movie = getTmdbMovieEntityMapper().map(result);
        movie.markFullFetchCompleted(PhilmModel.TYPE_TMDB);

        // Need to manually update releases here due to country code
        if (result.releases != null) {
            movie.updateWithReleases(result.releases, mCountryProvider.getTwoLetterCountryCode());
        }

        // Need to manually update releases here due to entity mapper
        if (result.similar_movies != null) {
            movie.setRelated(getTmdbMovieEntityMapper().map(result.similar_movies.results));
        }

        if (result.credits != null && !PhilmCollections.isEmpty(result.credits.cast)) {
            // Sort the Cast based on order first
            Collections.sort(result.credits.cast, new Comparator<Credits.CastMember>() {
                @Override
                public int compare(Credits.CastMember castMember, Credits.CastMember castMember2) {
                    return castMember.order - castMember2.order;
                }
            });
            movie.setCast(getTmdbCastEntityMapper().mapCredits(result.credits.cast));
        }

        if (result.credits != null && !PhilmCollections.isEmpty(result.credits.crew)) {
            List<PhilmMovieCrewCredit> crew = getTmdbCrewEntityMapper().mapCredits(result.credits.crew);
            Collections.sort(crew);
            movie.setCrew(crew);
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
                getDbHelper().put(movie);
                getEventBus()
                        .post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
            }
        }
        super.onError(re);
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }
}