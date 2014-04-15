package app.philm.in.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.fragments.base.BasePhilmMovieDialogFragment;
import app.philm.in.lib.controllers.MovieController;
import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.network.NetworkError;

public class CancelCheckinMovieFragment extends BasePhilmMovieDialogFragment
        implements DialogInterface.OnClickListener, MovieController.CancelCheckinUi {

    private TextView mMessageTextView;

    public static CancelCheckinMovieFragment create() {
        CancelCheckinMovieFragment fragment = new CancelCheckinMovieFragment();
        return fragment;
    }

    @Override
    public String getUiTitle() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_cancel_checkin_movie, null);

        mMessageTextView = (TextView) layout.findViewById(R.id.textview_message);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.movie_checkin_cancel);
        builder.setIcon(R.drawable.ic_btn_cancel_checkin);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.no, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, final int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:
                if (hasCallbacks()) {
                    getCallbacks().cancelCurrentCheckin();
                }
                break;
        }
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMessageTextView.setText(getString(R.string.movie_checkin_cancel_message, movie.getTitle()));
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
        return null;
    }

    @Override
    public boolean isModal() {
        return true;
    }
}
