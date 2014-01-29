package app.philm.in.tasks;


import com.uwetrottmann.tmdb.entities.Trailer;
import com.uwetrottmann.tmdb.entities.Trailers;

import java.util.ArrayList;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmTrailer;
import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTmdbMovieTrailersRunnable extends BaseMovieRunnable<Trailers> {

    private final int mId;

    public FetchTmdbMovieTrailersRunnable(int callingId, int id) {
        super(callingId);
        mId = id;
    }

    @Override
    public Trailers doBackgroundCall() throws RetrofitError {
        return getTmdbClient().moviesService().trailers(mId);
    }

    @Override
    public void onSuccess(Trailers result) {
        PhilmMovie movie = mMoviesState.getMovie(mId);

        if (movie != null) {
            final ArrayList<PhilmTrailer> trailers = new ArrayList<PhilmTrailer>();

            if (!PhilmCollections.isEmpty(result.youtube)) {
                for (Trailer trailer : result.youtube) {
                    final PhilmTrailer philmTrailer = new PhilmTrailer();
                    philmTrailer.setFromTmdb(PhilmTrailer.Source.YOUTUBE, trailer);
                    trailers.add(philmTrailer);
                }
            }

            // TODO Add support for quicktime
//            if (!PhilmCollections.isEmpty(result.quicktime)) {
//                for (Trailer trailer : result.quicktime) {
//                    final PhilmTrailer philmTrailer = new PhilmTrailer();
//                    philmTrailer.setFromTmdb(PhilmTrailer.Source.QUICKTIME, trailer);
//                    trailers.add(philmTrailer);
//                }
//            }

            movie.setTrailers(trailers);

            getEventBus().post(new MoviesState.MovieTrailersItemsUpdatedEvent(getCallingId(), movie));
        }
    }

    @Override
    public void onError(RetrofitError re) {
        super.onError(re);

        PhilmMovie movie = mMoviesState.getMovie(mId);
        if (movie != null) {
            getEventBus().post(new MoviesState.MovieTrailersItemsUpdatedEvent(
                    getCallingId(), movie));
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        return new BaseState.ShowTrailersLoadingProgressEvent(getCallingId(), show);
    }
}