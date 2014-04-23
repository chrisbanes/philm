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
