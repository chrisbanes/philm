package app.philm.in.tasks;

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.services.MovieService;

import java.util.ArrayList;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

abstract class BaseTraktActionRunnable extends BaseMovieRunnable<Response> {
    private final String[] mIds;

    BaseTraktActionRunnable(String[] id) {
        mIds = Preconditions.checkNotNull(id, "id cannot be null");
    }

    @Override
    public final Response doBackgroundCall() throws RetrofitError {
        MovieService.Movies body;

        if (mIds.length == 1) {
            body = new MovieService.Movies(new MovieService.SeenMovie(mIds[0]));
        } else {
            final ArrayList<MovieService.SeenMovie> seenMovies
                    = new ArrayList<MovieService.SeenMovie>(mIds.length);
            for (int i = 0 ; i < mIds.length ; i++) {
                seenMovies.add(new MovieService.SeenMovie(mIds[i]));
            }
            body = new MovieService.Movies(seenMovies);
        }

        return doTraktCall(mLazyTraktClient.get(), body);
    }

    public abstract Response doTraktCall(Trakt trakt, MovieService.Movies body);

    @Override
    public final void onSuccessfulResult(Response result) {
        if (result instanceof ActionResponse) {
            onActionCompleted(((ActionResponse) result).skipped < mIds.length);
        } else {
            onActionCompleted("success".equals(result.status));
        }
    }

    protected abstract void movieRequiresModifying(PhilmMovie movie);

    private void onActionCompleted(final boolean successful) {
        if (successful) {
            for (int i = 0; i < mIds.length; i++) {
                onSuccessfulAction(mIds[i]);
            }
            getCallback().populateUis();
        }
    }

    private void onSuccessfulAction(final String movieId) {
        PhilmMovie movie = mMoviesState.getMovie(movieId);
        if (movie != null) {
            movieRequiresModifying(movie);
            checkPhilmState(movie);
        }
    }
}