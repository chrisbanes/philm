package app.philm.in.state;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.model.PhilmMovie;
import nl.qbusict.cupboard.QueryResultIterable;

public class PhilmSQLiteOpenHelper extends SQLiteOpenHelper implements DatabaseHelper {

    private static String LOG_TAG = PhilmSQLiteOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "philm.db";
    private static final int DATABASE_VERSION = 5;

    static {
        // register our models
        cupboard().register(PhilmMovie.class);
    }

    private SQLiteDatabase mOpenDatabase;

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
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }

    @Override
    public List<PhilmMovie> getLibrary() {
        ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>();
        QueryResultIterable<PhilmMovie> itr = null;

        try {
            itr = cupboard().withDatabase(getReadableDatabase()).query(PhilmMovie.class)
                    .withSelection("inCollection = ? OR watched = ?", "1", "1")
                    .orderBy("sortTitle")
                    .query();

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
        ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>();
        QueryResultIterable<PhilmMovie> itr = null;

        try {
            itr = cupboard().withDatabase(getReadableDatabase()).query(PhilmMovie.class)
                    .withSelection("inWatchlist = ?", "1")
                    .orderBy("releasedTime")
                    .query();

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
        cupboard().withDatabase(getWritableDatabase()).put(movie);

        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "put(PhilmMovie). ID: " + movie.getDbId());
        }
    }

    @Override
    public void put(Collection<PhilmMovie> movies) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (PhilmMovie movie : movies) {
                cupboard().withDatabase(db).put(movie);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void delete(Collection<PhilmMovie> movies) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (PhilmMovie movie : movies) {
                cupboard().withDatabase(db).delete(movie);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
