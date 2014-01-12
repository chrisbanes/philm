package app.philm.in.fragments;


import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.MovieGridFragment;

public class WatchlistMoviesFragment extends MovieListFragment {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.WATCHLIST;
    }

}
