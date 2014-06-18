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
