package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.WatchingBase;
import com.jakewharton.trakt.enumerations.ActivityType;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.WatchingMovie;
import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class FetchTraktWatchingRunnable extends BaseMovieRunnable<WatchingBase> {

    private final String mUsername;

    public FetchTraktWatchingRunnable(int callingId, String username) {
        super(callingId);
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public WatchingBase doBackgroundCall() throws RetrofitError {
        return getTraktClient().userService().watching(mUsername);
    }

    @Override
    public void onSuccess(WatchingBase result) {
        if (result.type == ActivityType.Movie && WatchingMovie.validAction(result.action)) {

            PhilmMovie movie = getTraktMovieEntityMapper().map(result.movie);
            if (movie != null) {
                // TODO Fix timestamps
                WatchingMovie watching = new WatchingMovie(movie,
                        WatchingMovie.from(result.action), 0, 0);
                mMoviesState.setWatchingMovie(watching);
            }
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
