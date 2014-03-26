package app.philm.in.state.mappers;

import com.jakewharton.trakt.entities.Movie;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;

@Singleton
public class TraktMovieEntityMapper extends MovieEntityMapper<Movie> {

    @Inject
    public TraktMovieEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    public PhilmMovie map(Movie entity) {
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
