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

package app.philm.in;

public class Constants {

    public static final String TRAKT_API_KEY = "6e9904413059e7d847bbd28f0d71da1ad36d1588";

    public static final String TMDB_API_KEY = "f413bc4bacac8dff174a909f8ef535ae";

    public static final String TRAKT_ACCOUNT_TYPE = "app.philm.in.account";
    public static final String TRAKT_AUTHTOKEN_PASSWORD_TYPE = "password";

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    public static final long FUTURE_SOON_THRESHOLD = 30 * DAY_IN_MILLIS;

    public static final long STALE_USER_PROFILE_THRESHOLD = 3 * DAY_IN_MILLIS;
    public static final long STALE_MOVIE_DETAIL_THRESHOLD = 2 * DAY_IN_MILLIS;
    public static final long FULL_MOVIE_DETAIL_ATTEMPT_THRESHOLD = 60 * 60 * 1000; // 60 secs

    public static final int FILTER_HIGHLY_RATED = 70;

    public static final boolean DEBUG = true;
    public static final boolean DEBUG_NETWORK = false;

    public static final String TRAKT_MESSAGE_ITEM_REPLACE = "[item]";

    public static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    public static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s

}
