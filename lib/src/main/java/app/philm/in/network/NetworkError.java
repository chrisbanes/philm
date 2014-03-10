package app.philm.in.network;

import retrofit.RetrofitError;

public enum NetworkError {

    UNAUTHORIZED, NOT_FOUND_TRAKT, NOT_FOUND_TMDB, NETWORK_ERROR, UNKNOWN;

    public static final int SOURCE_TRAKT = 0;
    public static final int SOURCE_TMDB = 1;

    public static NetworkError from(RetrofitError error, int source) {
        if (error == null || error.isNetworkError() || error.getResponse() == null) {
            return NETWORK_ERROR;
        } else if (error.getResponse().getStatus() == 404) {
            switch (source) {
                case SOURCE_TMDB:
                    return NOT_FOUND_TMDB;
                case SOURCE_TRAKT:
                    return NOT_FOUND_TRAKT;
            }
        } else if (error.getResponse().getStatus() == 401) {
            return UNAUTHORIZED;
        }
        return UNKNOWN;
    }
}