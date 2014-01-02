package app.philm.in.state;

import java.util.Collection;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;

public interface DatabaseHelper {

    public List<PhilmMovie> getWatchlist();

    public List<PhilmMovie> getLibrary();

    public void put(Collection<PhilmMovie> movies);

    public void put(PhilmMovie movie);

    public void delete(Collection<PhilmMovie> movies);

    public PhilmUserProfile get(String username);

    public void put(PhilmUserProfile profile);

    public void delete(PhilmUserProfile profile);

    public void close();

}
