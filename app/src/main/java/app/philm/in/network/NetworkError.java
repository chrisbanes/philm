package app.philm.in.network;

import retrofit.RetrofitError;

public enum NetworkError {

    UNAUTHORIZED, NETWORK_ERROR, UNKNOWN;

    public static NetworkError from(RetrofitError error) {
        if (error.isNetworkError()) {
            return NETWORK_ERROR;
        } else if (error.getResponse().getStatus() == 401) {
            return UNAUTHORIZED;
        }
        return UNKNOWN;
    }
}