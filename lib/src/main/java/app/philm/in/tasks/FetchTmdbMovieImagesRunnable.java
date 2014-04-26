package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Image;
import com.uwetrottmann.tmdb.entities.Images;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieImagesRunnable extends BaseMovieRunnable<Images> {

    private final int mId;

    public FetchTmdbMovieImagesRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Images doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().images(mId);
    }

    @Override
    public void onSuccess(Images result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            if (!PhilmCollections.isEmpty(result.backdrops)) {
                List<PhilmMovie.BackdropImage> backdrops = new ArrayList<>();
                for (Image image : result.backdrops) {
                    backdrops.add(new PhilmMovie.BackdropImage(image));
                }
                movie.setBackdropImages(backdrops);
            }

            getEventBus().post(new MoviesState.MovieImagesUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

}