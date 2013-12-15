package app.philm.in.fragments;

import com.jakewharton.trakt.entities.Movie;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import app.philm.in.adapters.MovieGridAdapter;
import app.philm.in.controllers.MovieController;

public class CollectionListFragment extends ListFragment implements MovieController.MovieUi {

    private MovieController.MovieUiCallbacks mCallbacks;

    private MovieGridAdapter mMovieGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieGridAdapter = new MovieGridAdapter(getActivity());
        setListAdapter(mMovieGridAdapter);

        setListShown(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MovieController.MovieControllerProvider) getActivity())
                .getMovieController().detachUi(this);
    }

    @Override
    public void onPause() {
        ((MovieController.MovieControllerProvider) getActivity())
                .getMovieController().detachUi(this);
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setCollection(List<Movie> collection) {
        mMovieGridAdapter.setItems(collection);
        setListShown(true);
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

}
