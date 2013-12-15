package app.philm.in.trakt;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import retrofit.http.EncodedPath;
import retrofit.http.GET;

public interface MoviesService {

    @GET("/movies/trending.json/{apikey}")
    List<Movie> trending();

    @GET("/movies/updated.json/{apikey}/{timestamp}")
    List<Movie> updated(
            @EncodedPath("timestamp") long timestamp
    );

}
