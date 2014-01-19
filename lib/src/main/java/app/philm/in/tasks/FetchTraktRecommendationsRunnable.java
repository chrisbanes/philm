package app.philm.in.tasks;

import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.services.RecommendationsService;

import java.util.List;

import app.philm.in.network.NetworkError;
import app.philm.in.util.PhilmCollections;
import retrofit.RetrofitError;

public class FetchTraktRecommendationsRunnable extends BaseMovieRunnable<List<Movie>> {

    public FetchTraktRecommendationsRunnable(int callingId) {
        super(callingId);
    }

    @Override
    public List<Movie> doBackgroundCall() throws RetrofitError {
        RecommendationsService.RecommendationsQuery query
                = new RecommendationsService.RecommendationsQuery();
        query.hideCollected(true);
        query.hideWatchlisted(true);

        return getTraktClient().recommendationsService().movies(query);
    }

    @Override
    public void onSuccess(List<Movie> result) {
        if (!PhilmCollections.isEmpty(result)) {
            mMoviesState.setRecommended(getTraktEntityMapper().map(result));
        } else {
            mMoviesState.setRecommended(null);
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TRAKT;
    }
}