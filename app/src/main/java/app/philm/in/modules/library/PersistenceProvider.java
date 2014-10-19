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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

import javax.inject.Singleton;

import app.philm.in.qualifiers.ApplicationContext;
import app.philm.in.qualifiers.FilesDirectory;
import app.philm.in.qualifiers.ForDatabase;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.AsyncDatabaseHelperImpl;
import app.philm.in.state.DatabaseHelper;
import app.philm.in.state.PhilmSQLiteOpenHelper;
import app.philm.in.util.AndroidFileManager;
import app.philm.in.util.AndroidPhilmPreferences;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.FileManager;
import app.philm.in.util.PhilmPreferences;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        includes = {
                ContextProvider.class,
                UtilProvider.class
        }
)
public class PersistenceProvider {

    @Provides @Singleton
    public FileManager provideFileManager(@FilesDirectory File file) {
        return new AndroidFileManager(file);
    }

    @Provides @Singleton
    public DatabaseHelper getDatabaseHelper(@ApplicationContext Context context) {
        return new PhilmSQLiteOpenHelper(context);
    }

    @Provides @Singleton
    public AsyncDatabaseHelper getAsyncDatabaseHelper(
            @ForDatabase BackgroundExecutor executor,
            DatabaseHelper databaseHelper) {
        return new AsyncDatabaseHelperImpl(executor, databaseHelper);
    }

    @Provides @Singleton
    public PhilmPreferences providePhilmPreferences(@ApplicationContext Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new AndroidPhilmPreferences(prefs);
    }

}
