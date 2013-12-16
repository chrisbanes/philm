package app.philm.in.state;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

public interface MoviesState extends BaseState {

    public List<Movie> getLibrary();

    public void setLibrary(List<Movie> library);

    public boolean hasLibrary();

    public List<Movie> getTrending();

    public void setTrending(List<Movie> trending);

    public boolean hasTrending();

    public static class LibraryChangedEvent {}

    public static class TrendingChangedEvent {}

}
