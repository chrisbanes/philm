package app.philm.in.fragments;


import app.philm.in.fragments.base.MovieGridFragment;
import app.philm.in.lib.controllers.MovieController;

public class TrendingMoviesFragment extends MovieGridFragment {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.TRENDING;
    }

}
