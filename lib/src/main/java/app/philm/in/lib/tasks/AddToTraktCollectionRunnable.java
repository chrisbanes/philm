package app.philm.in.lib.tasks;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.Response;
import com.jakewharton.trakt.services.MovieService;

import app.philm.in.lib.model.PhilmMovie;
import retrofit.RetrofitError;

public class AddToTraktCollectionRunnable extends BaseTraktActionRunnable {

    public AddToTraktCollectionRunnable(int callingId, String... ids) {
        super(callingId, ids);
    }

    @Override
    public Response doTraktCall(Trakt trakt, MovieService.Movies body) throws RetrofitError {
        return trakt.movieService().library(body);
    }

    @Override
    protected void movieRequiresModifying(PhilmMovie movie) {
        movie.setInCollection(true);
    }
}