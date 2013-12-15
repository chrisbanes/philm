package app.philm.in.trakt;

public class Trakt extends com.jakewharton.trakt.Trakt {

    public MoviesService moviesService() {
        return buildRestAdapter().create(MoviesService.class);
    }
}
