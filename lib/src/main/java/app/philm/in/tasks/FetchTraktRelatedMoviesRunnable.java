package app.philm.in.tasks;

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

public class FetchTraktRelatedMoviesRunnable extends BaseMovieRunnable<List<Movie>> {
    private final String mId;

    public FetchTraktRelatedMoviesRunnable(String id) {
        mId = Preconditions.checkNotNull(id, "id cannot be null");
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return mLazyTraktClient.get().movieService().related(mId);
    }

    @Override
    public void onSuccessfulResult(List<Movie> result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);
        movie.setRelated(mLazyTraktMovieEntityMapper.get().map(result));

        if (hasCallback()) {
            getCallback().populateUis();
        }
    }
}