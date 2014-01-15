package app.philm.in.modules;

import com.jakewharton.trakt.Trakt;
import com.uwetrottmann.tmdb.Tmdb;

import javax.inject.Singleton;

import app.philm.in.Constants;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class NetworkProvider {

    @Provides @Singleton
    public Trakt provideTraktClient() {
        Trakt trakt = new Trakt();
        trakt.setApiKey(Constants.TRAKT_API_KEY);
        trakt.setIsDebug(Constants.DEBUG_NETWORK);
        return trakt;
    }

    @Provides @Singleton
    public Tmdb provideTmdbClient() {
        Tmdb tmdb = new Tmdb();
        tmdb.setApiKey(Constants.TMDB_API_KEY);
        tmdb.setIsDebug(Constants.DEBUG_NETWORK);
        return tmdb;
    }

}
