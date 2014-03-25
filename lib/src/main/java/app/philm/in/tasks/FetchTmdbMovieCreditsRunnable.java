package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Credits;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieCreditsRunnable extends BaseMovieRunnable<Credits> {

    private final int mId;

    public FetchTmdbMovieCreditsRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Credits doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().credits(mId);
    }

    @Override
    public void onSuccess(Credits result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            if (!PhilmCollections.isEmpty(result.cast)) {
                // Sort the Cast based on order first
                Collections.sort(result.cast, new Comparator<Credits.CastMember>() {
                    @Override
                    public int compare(Credits.CastMember castMember, Credits.CastMember castMember2) {
                        return castMember.order - castMember2.order;
                    }
                });
                movie.setCast(getTmdbCastEntityMapper().mapCredits(result.cast));
            }

            if (!PhilmCollections.isEmpty(result.crew)) {
                List<PhilmMovieCredit> crew = getTmdbCrewEntityMapper().mapCredits(result.crew);
                Collections.sort(crew);
                movie.setCrew(crew);
            }

            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieCastItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowCastLoadingProgressEvent(getCallingId(), show);
    }
}