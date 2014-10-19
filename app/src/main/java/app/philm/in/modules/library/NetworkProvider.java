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

package app.philm.in.modules.library;

import android.content.Context;

import com.jakewharton.trakt.Trakt;
import com.uwetrottmann.tmdb.Tmdb;

import java.io.File;

import javax.inject.Singleton;

import app.philm.in.Constants;
import app.philm.in.network.PhilmTmdb;
import app.philm.in.network.PhilmTrakt;
import app.philm.in.qualifiers.ApplicationContext;
import app.philm.in.qualifiers.CacheDirectory;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        includes = ContextProvider.class
)
public class NetworkProvider {

    @Provides @Singleton
    public Trakt provideTraktClient(@CacheDirectory File cacheLocation) {
        Trakt trakt = new PhilmTrakt(cacheLocation);
        trakt.setApiKey(Constants.TRAKT_API_KEY);
        trakt.setIsDebug(Constants.DEBUG_NETWORK);
        return trakt;
    }

    @Provides @Singleton
    public Tmdb provideTmdbClient(@CacheDirectory File cacheLocation) {
        Tmdb tmdb = new PhilmTmdb(cacheLocation);
        tmdb.setApiKey(Constants.TMDB_API_KEY);
        tmdb.setIsDebug(Constants.DEBUG_NETWORK);
        return tmdb;
    }

    @Provides @Singleton @CacheDirectory
    public File provideHttpCacheLocation(@ApplicationContext Context context) {
        return context.getCacheDir();
    }

}
