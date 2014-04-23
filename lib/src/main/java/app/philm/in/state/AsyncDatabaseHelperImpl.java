package app.philm.in.state;

import com.google.common.base.Preconditions;

import android.support.v4.util.ArrayMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.network.BackgroundCallRunnable;
import app.philm.in.util.BackgroundExecutor;

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
            public List<PhilmMovie> doDatabaseCall() {
                return mDbHelper.getLibrary();
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
            public Void doDatabaseCall() {
                mDbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void put(final PhilmMovie movie) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.put(movie);
                return null;
            }
        });
    }

    @Override
    public void delete(final Collection<PhilmMovie> movies) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                mDbHelper.delete(movies);
                return null;
            }
        });
    }

    @Override
    public void getUserProfile(final String username, final Callback<PhilmUserProfile> callback) {
        mExecutor.execute(new DatabaseBackgroundRunnable<PhilmUserProfile>() {
            @Override
            public PhilmUserProfile doDatabaseCall() {
                return mDbHelper.getUserProfile(username);
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
            public Void doDatabaseCall() {
                mDbHelper.put(profile);
                return null;
            }
        });
    }

    @Override
    public void delete(final PhilmUserProfile profile) {
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
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
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                Map<Long, PhilmMovie> dbItemsMap = new ArrayMap<>();
                for (PhilmMovie movie : mDbHelper.getLibrary()) {
                    dbItemsMap.put(movie.getDbId(), movie);
                }

                // Now lets remove the items from the mapAll, leaving only those not in the library
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
        mExecutor.execute(new DatabaseBackgroundRunnable<Void>() {
            @Override
            public Void doDatabaseCall() {
                Map<Long, PhilmMovie> dbItemsMap = new ArrayMap<>();
                for (PhilmMovie movie : mDbHelper.getWatchlist()) {
                    dbItemsMap.put(movie.getDbId(), movie);
                }

                // Now lets remove the items from the mapAll, leaving only those not in the watchlist
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
    public void getWatchlist(final AsyncDatabaseHelper.Callback<List<PhilmMovie>> callback) {
        mExecutor.execute(new DatabaseBackgroundRunnable<List<PhilmMovie>>() {
            @Override
            public List<PhilmMovie> doDatabaseCall() {
                return mDbHelper.getWatchlist();
            }

            @Override
            public void postExecute(List<PhilmMovie> result) {
                callback.onFinished(result);
            }
        });
    }

    private abstract class DatabaseBackgroundRunnable<R> extends BackgroundCallRunnable<R> {

        @Override
        public final R runAsync() {
            if (mDbHelper.isClosed()) {
                return null;
            }

            return doDatabaseCall();
        }

        public abstract R doDatabaseCall();

    }

}
