package app.philm.in.util;

import android.content.SharedPreferences;

import com.google.common.base.Preconditions;

public class AndroidPhilmPreferences implements PhilmPreferences {

    private static final String KEY_REMOVE_WATCHLIST_ON_WATCHED = "remove_watchlist_on_watched";

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

}
