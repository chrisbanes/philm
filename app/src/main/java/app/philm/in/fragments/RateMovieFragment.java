package app.philm.in.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import app.philm.in.R;

public class RateMovieFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private TextView mRatingDescriptionTextView;
    private RatingBar mRatingBar;

    private String[] mRatingDescriptions;

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
                // TODO: Callback to Controller
                break;
        }
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
}
