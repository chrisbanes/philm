package app.philm.in.state;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.TmdbConfiguration;
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

    public Set<MovieController.Filter> getFilters();

    public TmdbConfiguration getTmdbConfiguration();

    public void setTmdbConfiguration(TmdbConfiguration configuration);

    public static class LibraryChangedEvent {}

    public static class PopularChangedEvent {}

    public static class InTheatresChangedEvent {}

    public static class TrendingChangedEvent {}

    public static class WatchlistChangedEvent {}

    public static class SearchResultChangedEvent {}

    public static class UpcomingChangedEvent {}

    public static class RecommendedChangedEvent {}

    public static class TmdbConfigurationChangedEvent {}

    public class MoviePaginatedResult extends PaginatedResult<List<PhilmMovie>> {
    }

    public class SearchPaginatedResult extends MoviePaginatedResult {
        public String query;
    }


    static abstract class MovieEntityMapper<T> {
        final MoviesState mMoviesState;

        MovieEntityMapper(MoviesState state) {
            mMoviesState = Preconditions.checkNotNull(state, "state cannot be null");
        }

        public abstract PhilmMovie map(T entity);

        public List<PhilmMovie> map(List<T> entities) {
            final ArrayList<PhilmMovie> movies = new ArrayList<PhilmMovie>(entities.size());
            for (T entity : entities) {
                movies.add(map(entity));
            }
            return movies;
        }

        PhilmMovie getMovie(String id) {
            if (mMoviesState.getImdbIdMovies().containsKey(id)) {
                return mMoviesState.getImdbIdMovies().get(id);
            } else if (mMoviesState.getTmdbIdMovies().containsKey(id)) {
                return mMoviesState.getTmdbIdMovies().get(id);
            }
            return null;
        }

        void putMovie(PhilmMovie movie) {
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
            PhilmMovie movie = getMovie(entity.imdb_id);
            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putMovie(movie);

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
            PhilmMovie movie = getMovie(String.valueOf(entity.id));
            if (movie == null) {
                // No movie, so create one
                movie = new PhilmMovie();
            }
            // We already have a movie, so just update it wrapped value
            movie.setFromMovie(entity);
            putMovie(movie);

            return movie;
        }
    }

}
