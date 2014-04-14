package app.philm.in.lib.tasks;

import com.jakewharton.trakt.entities.Response;

import app.philm.in.lib.network.NetworkError;
import retrofit.RetrofitError;

public class CancelCheckinTraktRunnable extends BaseMovieRunnable<Response> {

    public CancelCheckinTraktRunnable(int callingId) {
        super(callingId);
    }

    @Override
    public Response doBackgroundCall() throws RetrofitError {
        return getTraktClient().movieService().cancelcheckin();
    }

    @Override
    public void onSuccess(Response result) {
        if (RESULT_TRAKT_SUCCESS.equals(result.status)) {
            mMoviesState.setWatchingMovie(null);
        } else if (RESULT_TRAKT_FAILURE.equals(result.status)) {
            // TODO
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
