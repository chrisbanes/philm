package app.philm.in.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;

public class MovieDetailFragment extends Fragment {

    private static final String KEY_QUERY_TYPE = "query_type";

    public static MovieDetailFragment create(MovieController.MovieQueryType type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_QUERY_TYPE, type.ordinal());

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        return view;
    }
}
