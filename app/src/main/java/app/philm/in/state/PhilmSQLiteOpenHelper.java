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

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import nl.qbusict.cupboard.DatabaseCompartment;
import nl.qbusict.cupboard.QueryResultIterable;

public class PhilmSQLiteOpenHelper extends SQLiteOpenHelper implements DatabaseHelper {

    private static String LOG_TAG = PhilmSQLiteOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "philm.db";
    private static final int DATABASE_VERSION = 29;
    private static final int LAST_DATABASE_NUKE_VERSION = 28;

    private static final Class[] ENTITIES = new Class[]{PhilmMovie.class, PhilmUserProfile.class};

    static {
        // register our models
        for (Class clazz : ENTITIES) {
            cupboard().register(clazz);
        }
    }

    private boolean mIsClosed;

    public PhilmSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();

        // TODO: add indexes and other database tweaks
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < LAST_DATABASE_NUKE_VERSION) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "Nuking Database. Old Version: " + oldVersion);
            }
            cupboard().withDatabase(db).dropAllTables();
            onCreate(db);
        } else {
            // this will upgrade tables, adding columns and new tables.
            // Note that existing columns will not be converted
            cupboard().withDatabase(db).upgradeTables();
        }
    }

    @Override
    public List<PhilmMovie> getLibrary() {
        return queryMovies("traktInCollection = ? OR traktWatched = ?", "1", "1");
    }

    @Override
    public List<PhilmMovie> getWatchlist() {
        return queryMovies("traktInWatchlist = ?", "1");
    }

    @Override
    public void put(PhilmMovie movie) {
        assetNotClosed();

        try {
            cupboard().withDatabase(getWritableDatabase()).put(movie);
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }
    }

    @Override
    public void put(Collection<PhilmMovie> movies) {
        assetNotClosed();
        try {
            cupboard().withDatabase(getWritableDatabase()).put(movies);
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }
    }

    @Override
    public void delete(Collection<PhilmMovie> movies) {
        assetNotClosed();

        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            db.beginTransaction();
            final DatabaseCompartment dbc = cupboard().withDatabase(db);
            for (PhilmMovie movie : movies) {
                dbc.delete(movie);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Crashlytics.logException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    @Override
    public PhilmUserProfile getUserProfile(String username) {
        assetNotClosed();

        try {
            return cupboard().withDatabase(getReadableDatabase())
                    .query(PhilmUserProfile.class)
                    .withSelection("username = ?", username)
                    .get();
        } catch (Exception e) {
            // Crashlytics.logException(e);
            return null;
        }
    }

    @Override
    public void put(PhilmUserProfile profile) {
        assetNotClosed();
        try {
            cupboard().withDatabase(getWritableDatabase()).put(profile);
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }
    }

    @Override
    public void delete(PhilmUserProfile profile) {
        assetNotClosed();
        try {
            cupboard().withDatabase(getWritableDatabase()).delete(profile);
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }
    }

    @Override
    public void deleteAllPhilmMovies() {
        deleteAllPhilmMovies(getWritableDatabase());
    }

    @Override
    public synchronized void close() {
        mIsClosed = true;
        super.close();
    }

    @Override
    public boolean isClosed() {
        return mIsClosed;
    }

    public void deleteAllPhilmMovies(SQLiteDatabase db) {
        assetNotClosed();
        try {
            final int numDeleted = cupboard().withDatabase(db).delete(PhilmMovie.class, null);
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "deleteAllPhilmMovies. Deleted " + numDeleted + " rows.");
            }
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }
    }

    private void assetNotClosed() {
        Preconditions.checkState(!mIsClosed, "Database is closed");
    }

    private List<PhilmMovie> queryMovies(String selection, String... selectionArgs) {
        assetNotClosed();
        QueryResultIterable<PhilmMovie> itr = null;

        try {
            itr = cupboard().withDatabase(getReadableDatabase()).query(PhilmMovie.class)
                    .withSelection(selection, selectionArgs)
                    .query();
        } finally {
            if (itr != null) {
                itr.close();
                itr = null;
            }
        }

        return itr != null ? itr.list() : null;
    }
}
