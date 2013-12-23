package app.philm.in.state;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;
import java.util.Set;

import app.philm.in.controllers.MovieController;

public interface MoviesState extends BaseState {

    public List<Movie> getLibrary();

    public void setLibrary(List<Movie> library);

    public List<Movie> getTrending();

    public void setTrending(List<Movie> trending);

    public List<Movie> getWatchlist();

    public void setWatchlist(List<Movie> watchlist);

    public Set<MovieController.Filter> getFilters();

    public static class LibraryChangedEvent {}

    public static class TrendingChangedEvent {}

    public static class WatchlistChangedEvent {}

}
