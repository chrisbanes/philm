/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.network;

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