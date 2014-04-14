package app.philm.in.lib.util;


public interface PhilmPreferences {

    public boolean shouldRemoveFromWatchlistOnWatched();

    public void setRemoveFromWatchlistOnWatched(boolean remove);

    public boolean hasShownTraktLoginPrompt();

    public void setShownTraktLoginPrompt();

}
