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

package app.philm.in.model;

import app.philm.in.util.TextUtils;

public abstract class PhilmModel {

    public static final int TYPE_TMDB = 1;
    public static final int TYPE_IMDB = 2;
    public static final int TYPE_TRAKT = 3;

    static String select(String tmdbVersion, String traktVersion) {
        if (!TextUtils.isEmpty(tmdbVersion)) {
            return tmdbVersion;
        }
        return traktVersion;
    }

    static int select(int tmdbVersion, int traktVersion) {
        if (tmdbVersion > 0) {
            return tmdbVersion;
        }
        return traktVersion;
    }

    static long select(long tmdbVersion, long traktVersion) {
        if (tmdbVersion > 0) {
            return tmdbVersion;
        }
        return traktVersion;
    }

}
