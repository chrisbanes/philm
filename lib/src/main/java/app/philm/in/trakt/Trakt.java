package app.philm.in.trakt;

public class Trakt extends com.jakewharton.trakt.Trakt {

    public PhilmMovieService philmMovieService() {
        return buildRestAdapter().create(PhilmMovieService.class);
    }

    public PhilmMoviesService philmMoviesService() {
        return buildRestAdapter().create(PhilmMoviesService.class);
    }

    public PhilmUserService philmUserService() {
        return buildRestAdapter().create(PhilmUserService.class);
    }

    public PhilmSearchService philmSearchService() {
        return buildRestAdapter().create(PhilmSearchService.class);
    }

}
