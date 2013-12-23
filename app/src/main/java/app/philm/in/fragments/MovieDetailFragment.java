package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.picasso.Picasso;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkError;
import app.philm.in.trakt.TraktImageHelper;

public class MovieDetailFragment extends Fragment implements MovieController.MovieDetailUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";
    private static final String KEY_QUERY_TYPE = "query_type";

    private MovieController.MovieUiCallbacks mCallbacks;
    private Movie mMovie;

    private TraktImageHelper mTraktImageHelper;

    private TextView mTitleTextView;
    private ImageView mFanartImageView;

    public static MovieDetailFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId),"movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);
        bundle.putInt(KEY_QUERY_TYPE, MovieController.MovieQueryType.DETAIL.ordinal());

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTraktImageHelper = new TraktImageHelper(getResources());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFanartImageView = (ImageView) view.findViewById(R.id.imageview_fanart);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void setMovie(Movie movie) {
        mMovie = movie;
        populateUi();
    }

    @Override
    public void showError(NetworkError error) {

    }

    @Override
    public void showLoadingProgress(boolean visible) {

    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        final int queryType = getArguments().getInt(KEY_QUERY_TYPE);
        return MovieController.MovieQueryType.values()[queryType];
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_MOVIE_ID);
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        Picasso.with(getActivity())
                .load(mTraktImageHelper.getFanartUrl(mMovie))
                .into(mFanartImageView);

        mTitleTextView.setText(getString(R.string.movie_title_year, mMovie.title, mMovie.year));
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }
}
