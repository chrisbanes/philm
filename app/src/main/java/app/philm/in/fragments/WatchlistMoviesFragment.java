package app.philm.in.fragments;


import app.philm.in.lib.controllers.MovieController;
import app.philm.in.fragments.base.MovieListFragment;

public class WatchlistMoviesFragment extends MovieListFragment {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.WATCHLIST;
    }

}
