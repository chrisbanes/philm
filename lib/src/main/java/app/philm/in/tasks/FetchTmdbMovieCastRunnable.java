package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Credits;

import java.util.ArrayList;

import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieCastRunnable extends BaseMovieRunnable<Credits> {

    private final int mId;

    public FetchTmdbMovieCastRunnable(int callingId, int id) {
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

        if (movie != null && !PhilmCollections.isEmpty(result.cast)) {
            final ArrayList<PhilmCast> castList = new ArrayList<PhilmCast>();

            for (Credits.CastMember castMember : result.cast) {
                final PhilmCast philmCastMember = new PhilmCast();
                philmCastMember.setFromCast(castMember);
                castList.add(philmCastMember);
            }

            movie.setCast(castList);
            getEventBus().post(new MoviesState.MovieInformationUpdatedEvent(getCallingId(), movie));
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