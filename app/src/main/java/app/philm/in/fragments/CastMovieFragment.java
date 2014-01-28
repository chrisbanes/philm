package app.philm.in.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.MovieCastListFragment;

public class CastMovieFragment extends MovieCastListFragment {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    public static CastMovieFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        CastMovieFragment fragment = new CastMovieFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.CAST;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_MOVIE_ID);
    }

}
