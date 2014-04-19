package app.philm.in.lib.state;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import app.philm.in.lib.controllers.MovieController;
import app.philm.in.lib.model.PhilmPerson;
import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.model.TmdbConfiguration;
import app.philm.in.lib.model.WatchingMovie;

public interface MoviesState extends BaseState {

    public Map<String, PhilmMovie> getTmdbIdMovies();

    public Map<String, PhilmMovie> getImdbIdMovies();

    public PhilmMovie getMovie(String id);

    public PhilmMovie getMovie(int id);

    public void putMovie(PhilmMovie movie);

    public List<PhilmMovie> getLibrary();

    public void setLibrary(List<PhilmMovie> library);

    public List<PhilmMovie> getTrending();

    public void setTrending(List<PhilmMovie> trending);

    public MoviePaginatedResult getPopular();

    public void setPopular(MoviePaginatedResult popular);

    public MoviePaginatedResult getNowPlaying();

    public void setNowPlaying(MoviePaginatedResult nowPlaying);

    public MoviePaginatedResult getUpcoming();

    public void setUpcoming(MoviePaginatedResult upcoming);

    public List<PhilmMovie> getWatchlist();

    public void setWatchlist(List<PhilmMovie> watchlist);

    public List<PhilmMovie> getRecommended();

    public void setRecommended(List<PhilmMovie> recommended);

    public void setSearchResult(SearchResult result);

    public SearchResult getSearchResult();

    public Set<MovieController.MovieFilter> getFilters();

    public TmdbConfiguration getTmdbConfiguration();

    public void setTmdbConfiguration(TmdbConfiguration configuration);

    public void setWatchingMovie(WatchingMovie movie);

    public WatchingMovie getWatchingMovie();

    public Map<String, PhilmPerson> getPeople();

    public PhilmPerson getPerson(int id);

    public PhilmPerson getPerson(String id);

    public static class LibraryChangedEvent {}

    public static class PopularChangedEvent {}

    public static class InTheatresChangedEvent {}

    public static class TrendingChangedEvent {}

    public static class WatchlistChangedEvent {}

    public static class SearchResultChangedEvent {}

    public static class UpcomingChangedEvent {}

    public static class RecommendedChangedEvent {}

    public static class TmdbConfigurationChangedEvent {}

    public static class WatchingMovieUpdatedEvent {}

    public static class MovieInformationUpdatedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieInformationUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieReleasesUpdatedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieReleasesUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieRelatedItemsUpdatedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieRelatedItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieTrailersItemsUpdatedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieTrailersItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieCastItemsUpdatedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieCastItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieUserRatingChangedEvent extends BaseArgumentEvent<PhilmMovie> {
        public MovieUserRatingChangedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class PersonChangedEvent extends BaseArgumentEvent<PhilmPerson> {
        public PersonChangedEvent(int callingId, PhilmPerson item) {
            super(callingId, item);
        }
    }

    public static class MovieFlagsUpdatedEvent extends BaseArgumentEvent<List<PhilmMovie>> {
        public MovieFlagsUpdatedEvent(int callingId, List<PhilmMovie> item) {
            super(callingId, item);
        }
    }

    public class MoviePaginatedResult extends PaginatedResult<PhilmMovie> {
    }

    public class PersonPaginatedResult extends PaginatedResult<PhilmPerson> {
    }

    public class SearchResult {
        public final String query;
        public MoviePaginatedResult movies;
        public PersonPaginatedResult people;

        public SearchResult(String query) {
            this.query = Preconditions.checkNotNull(query, "query cannot be null");
        }
    }

}
