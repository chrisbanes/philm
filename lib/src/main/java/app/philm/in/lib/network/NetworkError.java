package app.philm.in.lib.network;

import retrofit.RetrofitError;
import retrofit.client.Response;

public enum NetworkError {

    UNAUTHORIZED_TRAKT, NOT_FOUND_TRAKT, NOT_FOUND_TMDB, NETWORK_ERROR, UNKNOWN;

    public static final int SOURCE_TRAKT = 0;
    public static final int SOURCE_TMDB = 1;

    public static NetworkError from(final RetrofitError error, final int source) {
        if (error == null) {
            return UNKNOWN;
        }

        final Response response = error.getResponse();

        if (response == null) {
            return UNKNOWN;
        }

        if (error.isNetworkError()) {
            return NETWORK_ERROR;
        }

        final int statusCode = response.getStatus();

        if (statusCode == 401) {
            switch (source) {
                case SOURCE_TRAKT:
                    return UNAUTHORIZED_TRAKT;
            }
        } else if (statusCode == 404) {
            switch (source) {
                case SOURCE_TMDB:
                    return NOT_FOUND_TMDB;
                case SOURCE_TRAKT:
                    return NOT_FOUND_TRAKT;
            }
        }

        return UNKNOWN;
    }
}