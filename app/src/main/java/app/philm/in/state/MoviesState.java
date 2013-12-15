package app.philm.in.state;

import com.jakewharton.trakt.entities.Movie;

import java.util.List;

public interface MoviesState extends BaseState {

    public List<Movie> getCollection();

    public void setCollection(List<Movie> collection);

    public boolean hasCollection();



    public static class CollectionChangedEvent {}


}
