package app.philm.in.state.mappers;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.state.MoviesState;

abstract class BaseEntityMapper<T, R> {
    final MoviesState mMoviesState;

    public BaseEntityMapper(MoviesState state) {
        mMoviesState = Preconditions.checkNotNull(state, "state cannot be null");
    }

    public abstract R map(T entity);

    public List<R> map(List<T> entities) {
        final ArrayList<R> movies = new ArrayList<>(entities.size());
        for (T entity : entities) {
            movies.add(map(entity));
        }
        return movies;
    }

    abstract R getEntity(String id);

    abstract void putEntity(R entity);
}
