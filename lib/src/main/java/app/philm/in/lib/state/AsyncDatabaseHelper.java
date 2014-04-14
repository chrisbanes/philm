package app.philm.in.lib.state;

import java.util.Collection;
import java.util.List;

import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.model.PhilmUserProfile;

public interface AsyncDatabaseHelper {

    public void mergeLibrary(List<PhilmMovie> library);

    public void mergeWatchlist(List<PhilmMovie> watchlist);

    public void getWatchlist(Callback<List<PhilmMovie>> callback);

    public void getLibrary(Callback<List<PhilmMovie>> callback);

    public void put(Collection<PhilmMovie> movies);

    public void put(PhilmMovie movie);

    public void delete(Collection<PhilmMovie> movies);

    public void getUserProfile(String username, Callback<PhilmUserProfile> callback);

    public void put(PhilmUserProfile profile);

    public void delete(PhilmUserProfile profile);

    public void close();

    public interface Callback<T> {
        public void onFinished(T result);
    }

}
