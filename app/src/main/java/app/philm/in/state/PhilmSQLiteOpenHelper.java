package app.philm.in.state;

import com.google.common.base.Preconditions;

import com.crashlytics.android.Crashlytics;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class PhilmSQLiteOpenHelper extends SQLiteOpenHelper implements DatabaseHelper {

    private static String LOG_TAG = PhilmSQLiteOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "philm.db";
    private static final int DATABASE_VERSION = 19;

    static {
        // register our models
        cupboard().register(PhilmMovie.class);
        cupboard().register(PhilmUserProfile.class);
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
        if (oldVersion <= 16) {
            deleteAllPhilmMovies(db);
        }

        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
    }

    @Override
    public List<PhilmMovie> getLibrary() {
        assetNotClosed();

        ArrayList<PhilmMovie> movies = null;
        QueryResultIterable<PhilmMovie> itr = null;

        try {
            itr = cupboard().withDatabase(getReadableDatabase()).query(PhilmMovie.class)
                    .withSelection("inCollection = ? OR watched = ?", "1", "1")
                    .orderBy("sortTitle")
                    .query();

            movies = new ArrayList<PhilmMovie>();

            for (PhilmMovie movie : itr) {
                movies.add(movie);
            }
        } finally {
            if (itr != null) {
                itr.close();
            }
        }
        return movies;
    }

    @Override
    public List<PhilmMovie> getWatchlist() {
        assetNotClosed();

        ArrayList<PhilmMovie> movies = null;
        QueryResultIterable<PhilmMovie> itr = null;

        try {
            itr = cupboard().withDatabase(getReadableDatabase()).query(PhilmMovie.class)
                    .withSelection("inWatchlist = ?", "1")
                    .orderBy("releasedTime")
                    .query();

            movies = new ArrayList<PhilmMovie>();

            for (PhilmMovie movie : itr) {
                movies.add(movie);
            }
        } finally {
            if (itr != null) {
                itr.close();
            }
        }
        return movies;
    }

    @Override
    public void put(PhilmMovie movie) {
        assetNotClosed();

        try {
            cupboard().withDatabase(getWritableDatabase()).put(movie);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void put(Collection<PhilmMovie> movies) {
        assetNotClosed();

        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            db.beginTransaction();
            for (PhilmMovie movie : movies) {
                cupboard().withDatabase(db).put(movie);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.logException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    @Override
    public void delete(Collection<PhilmMovie> movies) {
        assetNotClosed();

        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            db.beginTransaction();
            for (PhilmMovie movie : movies) {
                cupboard().withDatabase(db).delete(movie);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.logException(e);
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
            Crashlytics.logException(e);
            return null;
        }
    }

    @Override
    public void put(PhilmUserProfile profile) {
        assetNotClosed();
        try {
            cupboard().withDatabase(getWritableDatabase()).put(profile);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void delete(PhilmUserProfile profile) {
        assetNotClosed();
        try {
            cupboard().withDatabase(getWritableDatabase()).delete(profile);
        } catch (Exception e) {
            Crashlytics.logException(e);
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
            cupboard().withDatabase(db).delete(PhilmMovie.class, null);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void assetNotClosed() {
        Preconditions.checkState(!mIsClosed, "Database is closed");
    }
}
