package app.philm.in.state;

import java.util.List;
import java.util.Map;
import java.util.Set;

import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmMovie;

public interface MoviesState extends BaseState {

    public Map<String, PhilmMovie> getMovies();

    public List<PhilmMovie> getLibrary();

    public void setLibrary(List<PhilmMovie> library);

    public List<PhilmMovie> getTrending();

    public void setTrending(List<PhilmMovie> trending);

    public List<PhilmMovie> getWatchlist();

    public void setWatchlist(List<PhilmMovie> watchlist);

    public Set<MovieController.Filter> getFilters();

    public static class LibraryChangedEvent {}

    public static class TrendingChangedEvent {}

    public static class WatchlistChangedEvent {}

}
