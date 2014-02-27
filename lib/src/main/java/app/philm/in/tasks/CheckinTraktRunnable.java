package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.CheckinResponse;
import com.jakewharton.trakt.services.MovieService;

import app.philm.in.network.NetworkError;
import retrofit.RetrofitError;

public class CheckinTraktRunnable extends BaseMovieRunnable<CheckinResponse> {

    private final String mId;

    public CheckinTraktRunnable(int callingId, String id) {
        super(callingId);
        mId = Preconditions.checkNotNull(id, "id cannot be null");
    }

    @Override
    public CheckinResponse doBackgroundCall() throws RetrofitError {
        MovieService.MovieCheckin checkin = new MovieService.MovieCheckin(mId, null, null, null);

        return getTraktClient().movieService().checkin(checkin);
    }

    @Override
    public void onSuccess(CheckinResponse result) {
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}
