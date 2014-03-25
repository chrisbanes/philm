package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.text.TextUtils;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseMovieCreditListFragment;

public class CastListFragment extends BaseMovieCreditListFragment {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    public static CastListFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        CastListFragment fragment = new CastListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.CAST;
    }
}
