package app.philm.in.fragments;

import app.philm.in.fragments.base.MovieListFragment;
import app.philm.in.lib.controllers.MovieController;

public class MovieSearchListFragment extends MovieListFragment
        implements MovieController.SearchMovieUi {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.SEARCH_MOVIES;
    }
}
