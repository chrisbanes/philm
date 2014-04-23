package app.philm.in.state.mappers;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;
import app.philm.in.util.TextUtils;

abstract class MovieEntityMapper<T> extends BaseEntityMapper<T, PhilmMovie> {

    public MovieEntityMapper(MoviesState state) {
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
