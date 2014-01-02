package app.philm.in.network;

import app.philm.in.R;
import retrofit.RetrofitError;

public enum NetworkError {

    UNAUTHORIZED(R.string.error_unauthorized),
    NETWORK_ERROR(R.string.error_network),
    UNKNOWN(R.string.error_unknown);

    private int mTitle;

    private NetworkError(int title) {
        mTitle = title;
    }

    public int getTitle() {
        return mTitle;
    }

    public static NetworkError from(RetrofitError error) {
        if (error.isNetworkError()) {
            return NETWORK_ERROR;
        } else if (error.getResponse().getStatus() == 401) {
            return UNAUTHORIZED;
        }
        return UNKNOWN;
    }
}