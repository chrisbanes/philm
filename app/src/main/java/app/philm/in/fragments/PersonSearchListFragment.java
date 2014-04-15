package app.philm.in.fragments;

import app.philm.in.fragments.base.PersonListFragment;
import app.philm.in.lib.controllers.MovieController;

public class PersonSearchListFragment extends PersonListFragment
        implements MovieController.SearchPersonUi {

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.SEARCH_PEOPLE;
    }
}
