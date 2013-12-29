package app.philm.in.state;

import java.util.List;

import app.philm.in.model.PhilmMovie;

public interface DatabaseHelper {

    public List<PhilmMovie> getLibrary();

    public void put(List<PhilmMovie> movies);

    public void put(PhilmMovie movie);

    public void close();

}
