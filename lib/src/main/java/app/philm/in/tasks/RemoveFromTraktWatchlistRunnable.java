package app.philm.in.tasks;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.services.MovieService;

import app.philm.in.model.PhilmMovie;
import retrofit.RetrofitError;

public class RemoveFromTraktWatchlistRunnable extends BaseTraktActionRunnable {

    public RemoveFromTraktWatchlistRunnable(int callingId, String... ids) {
        super(callingId, ids);
    }

    @Override
    public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
        return trakt.movieService().unwatchlist(body);
    }

    @Override
    protected void movieRequiresModifying(PhilmMovie movie) {
        movie.setInWatched(false);
    }
}