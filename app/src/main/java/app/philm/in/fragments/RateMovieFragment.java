package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;

public class RateMovieFragment extends DialogFragment implements DialogInterface.OnClickListener,
        MovieController.MovieDetailUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private TextView mRatingDescriptionTextView;
    private RatingBar mRatingBar;

    private String[] mRatingDescriptions;

    private PhilmMovie mMovie;
    private MovieController.MovieUiCallbacks mCallbacks;

    public static RateMovieFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        RateMovieFragment fragment = new RateMovieFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRatingDescriptions = getResources().getStringArray(R.array.movie_rating_descriptions);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_rate_movie, null);

        mRatingDescriptionTextView = (TextView) layout.findViewById(R.id.textview_rating_desc);

        mRatingBar = (RatingBar) layout.findViewById(R.id.ratingbar_rating);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRatingDescriptionText();
            }
        });
        mRatingBar.setEnabled(mMovie != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        builder.setPositiveButton(R.string.movie_detail_rate, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, final int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mCallbacks != null) {
                    mCallbacks.submitRating(mMovie, getRating());
                }
                break;
        }
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
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        mRatingBar.setEnabled(movie != null);
    }

    @Override
    public void showError(NetworkError error) {
        // TODO: Implement!
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        // TODO: Implement!
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.NONE;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_MOVIE_ID);
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private int getRating() {
        return Math.round(mRatingBar.getRating() * 2f);
    }

    private String getRatingDescription(int rating) {
        return mRatingDescriptions[rating];
    }

    private void updateRatingDescriptionText() {
        mRatingDescriptionTextView.setText(getRatingDescription(getRating()));
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }
}
