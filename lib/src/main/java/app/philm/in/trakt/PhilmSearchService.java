package app.philm.in.trakt;

import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.services.SearchService;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface PhilmSearchService extends SearchService {

    @GET("/search/movies.json/{apikey}/{query}")
    List<Movie> movies(
            @Path("query") String query
    );

    @GET("/search/movies.json/{apikey}/{query}/{limit}")
    List<Movie> movies(
            @Path("query") String query,
            @Path("limit") int limit
    );

}