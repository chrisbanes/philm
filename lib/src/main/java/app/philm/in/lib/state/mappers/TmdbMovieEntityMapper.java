package app.philm.in.lib.state.mappers;

import com.uwetrottmann.tmdb.entities.Movie;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.state.MoviesState;

@Singleton
public class TmdbMovieEntityMapper extends MovieEntityMapper<Movie> {

    @Inject
    public TmdbMovieEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    public PhilmMovie map(Movie entity) {
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
