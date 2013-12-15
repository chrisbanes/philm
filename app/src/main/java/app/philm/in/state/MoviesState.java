package app.philm.in.state;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

public interface MoviesState extends BaseState {

    public List<Movie> getLibrary();

    public void setLibrary(List<Movie> library);

    public boolean hasLibrary();

    public static class LibraryChangedEvent {}

}
