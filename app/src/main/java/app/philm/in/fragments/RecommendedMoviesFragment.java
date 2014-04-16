package app.philm.in.fragments;


import app.philm.in.fragments.base.MovieGridFragment;
import app.philm.in.lib.controllers.MovieController;

public class RecommendedMoviesFragment extends MovieGridFragment implements MovieController.SubUi {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.RECOMMENDED;
    }

}
