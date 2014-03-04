package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Watching;
import com.jakewharton.trakt.enumerations.ActivityAction;
import com.jakewharton.trakt.enumerations.ActivityType;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.WatchingMovie;
import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class FetchTraktWatchingRunnable extends BaseMovieRunnable<Watching> {

    private final String mUsername;

    public FetchTraktWatchingRunnable(int callingId, String username) {
        super(callingId);
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public Watching doBackgroundCall() throws RetrofitError {
        return getTraktClient().userService().watching(mUsername);
    }

    @Override
    public void onSuccess(Watching result) {
        if (result.action == ActivityAction.Checkin && result.type == ActivityType.Movie) {

            PhilmMovie movie = getTraktEntityMapper().map(result.movie);
            if (movie != null) {
                WatchingMovie watching = new WatchingMovie(movie, 0, 0);
                mMoviesState.setWatchingMovie(watching);
            }
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
