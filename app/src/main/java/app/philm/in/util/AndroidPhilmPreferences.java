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

package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.content.SharedPreferences;

public class AndroidPhilmPreferences implements PhilmPreferences {

    private static final String KEY_REMOVE_WATCHLIST_ON_WATCHED = "remove_watchlist_on_watched";
    private static final String KEY_SHOWN_TRAKT_LOGIN_PROMPT = "shown_trakt_login_prompt";

    private final SharedPreferences mPrefs;

    public AndroidPhilmPreferences(SharedPreferences prefs) {
        mPrefs = Preconditions.checkNotNull(prefs, "prefs cannot be null");
    }

    @Override
    public boolean shouldRemoveFromWatchlistOnWatched() {
        return mPrefs.getBoolean(KEY_REMOVE_WATCHLIST_ON_WATCHED, false);
    }

    @Override
    public void setRemoveFromWatchlistOnWatched(boolean remove) {
        mPrefs.edit().putBoolean(KEY_REMOVE_WATCHLIST_ON_WATCHED, remove).apply();
    }

    @Override
    public boolean hasShownTraktLoginPrompt() {
        return mPrefs.getBoolean(KEY_SHOWN_TRAKT_LOGIN_PROMPT, false);
    }

    @Override
    public void setShownTraktLoginPrompt() {
        mPrefs.edit().putBoolean(KEY_SHOWN_TRAKT_LOGIN_PROMPT, true).apply();
    }
}
