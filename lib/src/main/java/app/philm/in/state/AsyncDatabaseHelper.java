package app.philm.in.state;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.network.BackgroundCallRunnable;

public class AsyncDatabaseHelper implements DatabaseHelper {

    private final ExecutorService mExecutorService;
    private final PhilmSQLiteOpenHelper mDbHelper;

    public AsyncDatabaseHelper(ExecutorService executorService, PhilmSQLiteOpenHelper dbHelper) {
        mExecutorService = Preconditions.checkNotNull(executorService,
                "executorService cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
    }

    @Override
    public void getLibrary(final DatabaseHelper.Callback<List<PhilmMovie>> callback) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<List<PhilmMovie>>() {
            @Override
            public List<PhilmMovie> doDatabaseCall() {
                return mDbHelper.getLibrary();
            }

            @Override
            public void onFinished(List<PhilmMovie> result) {
                callback.onFinished(result);
            }
        });
    }

    @Override
    public void put(final Collection<PhilmMovie> movies) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void put(final PhilmMovie movie) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.put(movie);
                return null;
            }
        });
    }

    @Override
    public void delete(final Collection<PhilmMovie> movies) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void getUserProfile(final String username, final Callback<PhilmUserProfile> callback) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<PhilmUserProfile>() {
            @Override
            public PhilmUserProfile doDatabaseCall() {
                return mDbHelper.getUserProfile(username);
            }

            @Override
            public void onFinished(PhilmUserProfile result) {
                callback.onFinished(result);
            }
        });
    }

    @Override
    public void put(final PhilmUserProfile profile) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.put(profile);
                return null;
            }
        });
    }

    @Override
    public void delete(final PhilmUserProfile profile) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.delete(profile);
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
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                HashMap<Long, PhilmMovie> dbItemsMap = new HashMap<Long, PhilmMovie>();
                for (PhilmMovie movie : mDbHelper.getLibrary()) {
                    dbItemsMap.put(movie.getDbId(), movie);
                }

                // Now lets remove the items from the map, leaving only those not in the library
                for (PhilmMovie movie : library) {
                    dbItemsMap.remove(movie.getDbId());
                }

                // Anything left in the dbItemsMap needs removing from the db
                if (!dbItemsMap.isEmpty()) {
                    mDbHelper.delete(dbItemsMap.values());
                }

                // Now persist the correct list
                mDbHelper.put(library);

                return null;
            }
        });
    }

    @Override
    public void mergeWatchlist(final List<PhilmMovie> watchlist) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                HashMap<Long, PhilmMovie> dbItemsMap = new HashMap<Long, PhilmMovie>();
                for (PhilmMovie movie : mDbHelper.getWatchlist()) {
                    dbItemsMap.put(movie.getDbId(), movie);
                }

                // Now lets remove the items from the map, leaving only those not in the watchlist
                for (PhilmMovie movie : watchlist) {
                    dbItemsMap.remove(movie.getDbId());
                }

                // Anything left in the dbItemsMap needs removing from the db
                if (!dbItemsMap.isEmpty()) {
                    mDbHelper.delete(dbItemsMap.values());
                }

                // Now persist the correct list
                mDbHelper.put(watchlist);

                return null;
            }
        });
    }

    @Override
    public void getWatchlist(final DatabaseHelper.Callback<List<PhilmMovie>> callback) {
        mExecutorService.execute(new DatabaseBackgroundRunnable<List<PhilmMovie>>() {
            @Override
            public List<PhilmMovie> doDatabaseCall() {
                return mDbHelper.getWatchlist();
            }

            @Override
            public void onFinished(List<PhilmMovie> result) {
                callback.onFinished(result);
            }
        });
    }

    private abstract class DatabaseBackgroundRunnable<R> extends BackgroundCallRunnable<R> {

        @Override
        public final R doBackgroundCall() {
            if (mDbHelper.isClosed()) {
                return null;
            }

            return doDatabaseCall();
        }

        public abstract R doDatabaseCall();

    }

}
