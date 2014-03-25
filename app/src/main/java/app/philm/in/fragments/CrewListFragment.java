package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.text.TextUtils;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseMovieCreditListFragment;

public class CrewListFragment extends BaseMovieCreditListFragment {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    public static CrewListFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        CrewListFragment fragment = new CrewListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.CREW;
    }
}
