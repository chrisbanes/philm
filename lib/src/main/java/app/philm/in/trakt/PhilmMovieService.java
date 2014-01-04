package app.philm.in.trakt;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

import retrofit.http.EncodedPath;
import retrofit.http.GET;

public interface PhilmMovieService {

    @GET("/movie/related.json/{apikey}/{id}")
    List<Movie> related(
            @EncodedPath("id") String id
    );

    @GET("/movie/related.json/{apikey}/{id}/hidewatched")
    List<Movie> relatedHideWatched(
            @EncodedPath("id") String id
    );

}
