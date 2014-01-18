package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;
import retrofit.RetrofitError;

public class FetchTraktRelatedMoviesRunnable extends BaseMovieRunnable<List<Movie>> {
    private final String mId;

    public FetchTraktRelatedMoviesRunnable(int callingId, String id) {
        super(callingId);
        mId = Preconditions.checkNotNull(id, "id cannot be null");
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return getTraktClient().movieService().related(mId);
    }

    @Override
    public void onSuccess(List<Movie> result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);
        movie.setRelated(getTraktEntityMapper().map(result));

        getEventBus().post(new MoviesState.MovieRelatedItemsUpdatedEvent(getCallingId(), movie));
    }
}