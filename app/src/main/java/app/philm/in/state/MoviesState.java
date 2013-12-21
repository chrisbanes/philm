package app.philm.in.state;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;
import java.util.Set;

import app.philm.in.controllers.MovieController;

public interface MoviesState extends BaseState {

    public List<Movie> getLibrary();

    public void setLibrary(List<Movie> library);

    public boolean hasLibrary();

    public List<Movie> getTrending();

    public void setTrending(List<Movie> trending);

    public boolean hasTrending();

    public Set<MovieController.Filter> getFilters();

    public static class LibraryChangedEvent {}

    public static class TrendingChangedEvent {}

}
