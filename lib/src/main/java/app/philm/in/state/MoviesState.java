package app.philm.in.state;

import com.google.common.base.Preconditions;

import com.uwetrottmann.tmdb.entities.Credits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmCrew;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.TmdbConfiguration;
import app.philm.in.model.WatchingMovie;
import app.philm.in.util.TextUtils;

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

    public void setSearchResult(SearchPaginatedResult result);

    public SearchPaginatedResult getSearchResult();

    public Set<MovieController.MovieFilter> getFilters();

    public TmdbConfiguration getTmdbConfiguration();

    public void setTmdbConfiguration(TmdbConfiguration configuration);

    public void setWatchingMovie(WatchingMovie movie);

    public WatchingMovie getWatchingMovie();

    public Map<String, PhilmCrew> getCrew();

    public Map<String, PhilmCast> getCast();

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

    public static class UiCausedEvent<T> extends BaseArgumentEvent<T> {
        public final int callingId;

        public UiCausedEvent(int callingId, T item) {
            super(item);
            this.callingId = callingId;
        }
    }

    public static class MovieInformationUpdatedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieInformationUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieReleasesUpdatedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieReleasesUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieRelatedItemsUpdatedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieRelatedItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieTrailersItemsUpdatedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieTrailersItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieCastItemsUpdatedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieCastItemsUpdatedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieUserRatingChangedEvent extends UiCausedEvent<PhilmMovie> {
        public MovieUserRatingChangedEvent(int callingId, PhilmMovie item) {
            super(callingId, item);
        }
    }

    public static class MovieFlagsUpdatedEvent extends UiCausedEvent<List<PhilmMovie>> {
        public MovieFlagsUpdatedEvent(int callingId, List<PhilmMovie> item) {
            super(callingId, item);
        }
    }

    public class MoviePaginatedResult extends PaginatedResult<List<PhilmMovie>> {
    }

    public class SearchPaginatedResult extends MoviePaginatedResult {
        public String query;
    }

    static abstract class BaseEntityMapper<T, R> {
        final MoviesState mMoviesState;

        BaseEntityMapper(MoviesState state) {
            mMoviesState = Preconditions.checkNotNull(state, "state cannot be null");
        }

        public abstract R map(T entity);

        public List<R> map(List<T> entities) {
            final ArrayList<R> movies = new ArrayList<R>(entities.size());
            for (T entity : entities) {
                movies.add(map(entity));
            }
            return movies;
        }

        abstract R getEntity(String id);

        abstract void putEntity(R entity);
    }

    static abstract class MovieEntityMapper<T> extends BaseEntityMapper<T, PhilmMovie> {

        MovieEntityMapper(MoviesState state) {
            super(state);
        }

        @Override
        PhilmMovie getEntity(String id) {
            if (mMoviesState.getImdbIdMovies().containsKey(id)) {
                return mMoviesState.getImdbIdMovies().get(id);
            } else if (mMoviesState.getTmdbIdMovies().containsKey(id)) {
                return mMoviesState.getTmdbIdMovies().get(id);
            }
            return null;
        }

        @Override
        void putEntity(PhilmMovie movie) {
            if (!TextUtils.isEmpty(movie.getImdbId())) {
                mMoviesState.getImdbIdMovies().put(movie.getImdbId(), movie);
            }
            if (movie.getTmdbId() != null) {
                mMoviesState.getTmdbIdMovies().put(String.valueOf(movie.getTmdbId()), movie);
            }
        }

    }

    public static class TraktMovieEntityMapper extends
            MovieEntityMapper<com.jakewharton.trakt.entities.Movie> {

        @Inject
        TraktMovieEntityMapper(MoviesState state) {
            super(state);
        }

        @Override
        public PhilmMovie map(com.jakewharton.trakt.entities.Movie entity) {
            PhilmMovie movie = getEntity(entity.imdb_id);

            if (movie == null && entity.tmdbId != null) {
                movie = getEntity(entity.tmdbId);
            }

            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putEntity(movie);

            return movie;
        }
    }

    public static class TmdbMovieEntityMapper extends
            MovieEntityMapper<com.uwetrottmann.tmdb.entities.Movie> {

        @Inject
        TmdbMovieEntityMapper(MoviesState state) {
            super(state);
        }

        @Override
        public PhilmMovie map(com.uwetrottmann.tmdb.entities.Movie entity) {
            PhilmMovie movie = getEntity(String.valueOf(entity.id));

            if (movie == null && entity.imdb_id != null) {
                movie = getEntity(entity.imdb_id);
            }

            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putEntity(movie);

            return movie;
        }
    }

    public static class TmdbCastEntityMapper
            extends BaseEntityMapper<Credits.CastMember, PhilmCast> {

        @Inject
        TmdbCastEntityMapper(MoviesState state) {
            super(state);
        }

        @Override
        public PhilmCast map(Credits.CastMember entity) {
            PhilmCast item = getEntity(String.valueOf(entity.id));

            if (item == null) {
                // No item, so create one
                item = new PhilmCast();
            }

            // We already have a movie, so just update it wrapped value
            item.setFromTmdb(entity);
            putEntity(item);

            return item;
        }

        @Override
        PhilmCast getEntity(String id) {
            return mMoviesState.getCast().get(id);
        }

        @Override
        void putEntity(PhilmCast entity) {
            mMoviesState.getCast().put(String.valueOf(entity.getTmdbId()), entity);
        }
    }

    public static class TmdbCrewEntityMapper
            extends BaseEntityMapper<Credits.CrewMember, PhilmCrew> {

        @Inject
        TmdbCrewEntityMapper(MoviesState state) {
            super(state);
        }

        @Override
        public PhilmCrew map(Credits.CrewMember entity) {
            PhilmCrew item = getEntity(String.valueOf(entity.id));

            if (item == null) {
                // No item, so create one
                item = new PhilmCrew();
            }

            // We already have a movie, so just update it wrapped value
            item.setFromTmdb(entity);
            putEntity(item);

            return item;
        }

        @Override
        PhilmCrew getEntity(String id) {
            return mMoviesState.getCrew().get(id);
        }

        @Override
        void putEntity(PhilmCrew entity) {
            mMoviesState.getCrew().put(String.valueOf(entity.getTmdbId()), entity);
        }
    }

}
