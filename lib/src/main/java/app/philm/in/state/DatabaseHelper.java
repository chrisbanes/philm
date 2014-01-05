package app.philm.in.state;

import java.util.Collection;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;

public interface DatabaseHelper {

    List<PhilmMovie> getLibrary();

    List<PhilmMovie> getWatchlist();

    void put(PhilmMovie movie);

    void put(Collection<PhilmMovie> movies);

    void delete(Collection<PhilmMovie> movies);

    PhilmUserProfile getUserProfile(String username);

    void put(PhilmUserProfile profile);

    void delete(PhilmUserProfile profile);

    void deleteAllPhilmMovies();

    void close();

    boolean isClosed();
}
