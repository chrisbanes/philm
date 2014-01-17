package app.philm.in.tasks;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTraktTrendingRunnable extends BaseMovieRunnable<List<Movie>> {

    public FetchTraktTrendingRunnable() {
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return mLazyTraktClient.get().moviesService().trending();
    }

    @Override
    public void onSuccess(List<Movie> result) {
        if (!PhilmCollections.isEmpty(result)) {
            mMoviesState.setTrending(mLazyTraktMovieEntityMapper.get().map(result));
        } else {
            mMoviesState.setTrending(null);
        }
    }

}