package app.philm.in.state;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.model.PhilmMovie;

public class PhilmSQLiteOpenHelper extends SQLiteOpenHelper implements DatabaseHelper {

    private static final String DATABASE_NAME = "philm.db";
    private static final int DATABASE_VERSION = 3;

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
        Iterable<PhilmMovie> itr = cupboard().withDatabase(getReadableDatabase())
                .query(PhilmMovie.class)
                .withSelection("inCollection = ? OR watched = ?", "true", "true")
                .orderBy("sortTitle")
                .query();

        ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>();
        for (PhilmMovie movie : itr) {
            movies.add(movie);
        }
        return movies;
    }

    @Override
    public void put(PhilmMovie movie) {
        cupboard().withDatabase(getWritableDatabase()).put(movie);
    }

    @Override
    public void put(List<PhilmMovie> movies) {
        for (PhilmMovie movie : movies) {
            cupboard().withDatabase(getWritableDatabase()).put(movie);
        }
    }
}
