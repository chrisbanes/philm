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

package app.philm.in.state;

import com.google.common.base.Preconditions;

import android.support.v4.util.ArrayMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.network.BackgroundCallRunnable;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.PhilmCollections;

public class AsyncDatabaseHelperImpl implements AsyncDatabaseHelper {

    private final BackgroundExecutor mExecutor;
    private final DatabaseHelper mDbHelper;

    public AsyncDatabaseHelperImpl(BackgroundExecutor executor, DatabaseHelper dbHelper) {
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
    }

    @Override
    public void getLibrary(final AsyncDatabaseHelper.Callback<List<PhilmMovie>> callback) {
        mExecutor.execute(new DatabaseBackgroundRunnable<List<PhilmMovie>>() {
            @Override
            public List<PhilmMovie> doDatabaseCall(DatabaseHelper dbHelper) {
                List<PhilmMovie> library = dbHelper.getLibrary();
                if (library != null) {
                    Collections.sort(library, PhilmMovie.COMPARATOR_SORT_TITLE);
                }
                return library;
            }

            @Override
            public void postExecute(List<PhilmMovie> result) {
                callback.onFinished(result);
            }
        });
    }

    @Override
    public void put(final Collection<PhilmMovie> movies) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void put(final PhilmMovie movie) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.put(movie);
                return null;
            }
        });
    }

    @Override
    public void delete(final Collection<PhilmMovie> movies) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void getUserProfile(final String username, final Callback<PhilmUserProfile> callback) {
        mExecutor.execute(new DatabaseBackgroundRunnable<PhilmUserProfile>() {
            @Override
            public PhilmUserProfile doDatabaseCall(DatabaseHelper dbHelper) {
                return dbHelper.getUserProfile(username);
            }

            @Override
            public void postExecute(PhilmUserProfile result) {
                callback.onFinished(result);
            }
        });
    }

    @Override
    public void put(final PhilmUserProfile profile) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.put(profile);
                return null;
            }
        });
    }

    @Override
    public void delete(final PhilmUserProfile profile) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.delete(profile);
                return null;
            }
        });
    }

    @Override
    public void close() {
        mDbHelper.close();
    }

    @Override
    public void mergeLibrary(final List<PhilmMovie> library) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                merge(dbHelper, dbHelper.getLibrary(), library);

                return null;
            }
        });
    }

    @Override
    public void mergeWatchlist(final List<PhilmMovie> watchlist) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                merge(dbHelper, dbHelper.getWatchlist(), watchlist);
                return null;
            }
        });
    }

    @Override
    public void getWatchlist(final AsyncDatabaseHelper.Callback<List<PhilmMovie>> callback) {
        mExecutor.execute(new DatabaseBackgroundRunnable<List<PhilmMovie>>() {
            @Override
            public List<PhilmMovie> doDatabaseCall(DatabaseHelper dbHelper) {
                List<PhilmMovie> watchlist = dbHelper.getWatchlist();
                return watchlist;
            }

            @Override
            public void postExecute(List<PhilmMovie> result) {
                callback.onFinished(result);
            }
        });
    }

    @Override
    public void deleteAllPhilmMovies() {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall(DatabaseHelper dbHelper) {
                dbHelper.deleteAllPhilmMovies();
                return null;
            }
        });
    }

    private abstract class DatabaseBackgroundRunnable<R> extends BackgroundCallRunnable<R> {

        @Override
        public final R runAsync() {
            final DatabaseHelper dbHelper = mDbHelper;

            if (dbHelper.isClosed()) {
                return null;
            }

            return doDatabaseCall(dbHelper);
        }

        public abstract R doDatabaseCall(DatabaseHelper dbHelper);

    }

    private static void merge(DatabaseHelper dbHelper,
                              List<PhilmMovie> databaseItems,
                              List<PhilmMovie> newItems) {
        if (!PhilmCollections.isEmpty(databaseItems)) {
            Map<Long, PhilmMovie> dbItemsMap = new ArrayMap<>();
            for (PhilmMovie movie : databaseItems) {
                dbItemsMap.put(movie.getDbId(), movie);
            }

            // Now lets remove the items from the mapAll, leaving only those
            // not in the watchlist
            for (PhilmMovie movie : newItems) {
                dbItemsMap.remove(movie.getDbId());
            }

            // Anything left in the dbItemsMap needs removing from the db
            if (!dbItemsMap.isEmpty()) {
                dbHelper.delete(dbItemsMap.values());
            }
        }

        // Now persist the correct list
        dbHelper.put(newItems);
    }
}
