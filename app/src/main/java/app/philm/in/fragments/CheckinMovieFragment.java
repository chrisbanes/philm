package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieDialogFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;

public class CheckinMovieFragment extends BasePhilmMovieDialogFragment
        implements DialogInterface.OnClickListener, MovieController.MovieCheckinUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private PhilmMovie mMovie;

    private EditText mMessageEditText;


    public static CheckinMovieFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        CheckinMovieFragment fragment = new CheckinMovieFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public String getUiTitle() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_checkin_movie, null);

        mMessageEditText = (EditText) layout.findViewById(R.id.edit_message);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        builder.setPositiveButton(R.string.movie_checkin, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, final int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:
                if (hasCallbacks()) {
                    getCallbacks().checkin(mMovie, String.valueOf(mMessageEditText.getText()));
                }
                break;
        }
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
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
    public boolean isModal() {
        return true;
    }
}
