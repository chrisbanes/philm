package app.philm.in.trakt;

import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.services.UserService;

import java.util.List;

import retrofit.http.EncodedPath;
import retrofit.http.GET;

public interface PhilmUserService extends UserService {

    @GET("/user/library/movies/all.json/{apikey}/{username}/min")
    List<Movie> libraryMoviesAllMinimum(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/all.json/{apikey}/{username}")
    List<Movie> libraryMoviesAll(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/all.json/{apikey}/{username}/extended")
    List<Movie> libraryMoviesAllExtended(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/collection.json/{apikey}/{username}/min")
    List<Movie> libraryMoviesCollectionMinimum(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/collection.json/{apikey}/{username}")
    List<Movie> libraryMoviesCollection(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/collection.json/{apikey}/{username}/extended")
    List<Movie> libraryMoviesCollectionExtended(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/watched.json/{apikey}/{username}/min")
    List<Movie> libraryMoviesWatchedMinimum(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/watched.json/{apikey}/{username}")
    List<Movie> libraryMoviesWatched(
            @EncodedPath("username") String username
    );

    @GET("/user/library/movies/watched.json/{apikey}/{username}/extended")
    List<Movie> libraryMoviesWatchedExtended(
            @EncodedPath("username") String username
    );

}
