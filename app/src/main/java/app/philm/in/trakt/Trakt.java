package app.philm.in.trakt;

public class Trakt extends com.jakewharton.trakt.Trakt {

    public MoviesService moviesService() {
        return buildRestAdapter().create(MoviesService.class);
    }

    public PhilmUserService philmUserService() {
        return buildRestAdapter().create(PhilmUserService.class);
    }

}
