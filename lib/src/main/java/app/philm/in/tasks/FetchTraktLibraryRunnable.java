package app.philm.in.tasks;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmMovie;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTraktLibraryRunnable extends BaseMovieRunnable<List<Movie>> {

    private final String mUsername;

    public FetchTraktLibraryRunnable(MovieController.MovieUi ui, String username) {
        super(ui);
        mUsername = Preconditions.checkNotNull(username, "username cannot be null");
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        return mLazyTraktClient.get().userService().libraryMoviesAll(mUsername);
    }

    @Override
    public void onSuccess(List<Movie> result) {
        if (!PhilmCollections.isEmpty(result)) {
            List<PhilmMovie> movies = mLazyTraktMovieEntityMapper.get().map(result);
            mMoviesState.setLibrary(movies);

            if (isInited()) {
                persistLibraryToDb(movies);
            }
        } else {
            mMoviesState.setLibrary(null);
        }
    }
}