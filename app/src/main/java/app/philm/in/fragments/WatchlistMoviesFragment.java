package app.philm.in.fragments;


import app.philm.in.fragments.base.MovieListFragment;
import app.philm.in.lib.controllers.MovieController;

public class WatchlistMoviesFragment extends MovieListFragment {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.WATCHLIST;
    }

}
