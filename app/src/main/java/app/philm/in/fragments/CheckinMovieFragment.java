package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import app.philm.in.R;
import app.philm.in.fragments.base.BasePhilmMovieDialogFragment;
import app.philm.in.lib.controllers.MovieController;
import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.lib.network.NetworkError;

public class CheckinMovieFragment extends BasePhilmMovieDialogFragment
        implements DialogInterface.OnClickListener, MovieController.MovieCheckinUi, CompoundButton.OnCheckedChangeListener{

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private PhilmMovie mMovie;
    private Switch mFacebookButton, mTwitterButton, mPathButton, mTumblrButton;

    private View mSocialHelperView;
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

        mFacebookButton = (Switch) layout.findViewById(R.id.btn_facebook);
        mFacebookButton.setOnCheckedChangeListener(this);

        mTwitterButton = (Switch) layout.findViewById(R.id.btn_twitter);
        mTwitterButton.setOnCheckedChangeListener(this);

        mPathButton = (Switch) layout.findViewById(R.id.btn_path);
        mPathButton.setOnCheckedChangeListener(this);

        mTumblrButton = (Switch) layout.findViewById(R.id.btn_tumblr);
        mTumblrButton.setOnCheckedChangeListener(this);

        mSocialHelperView = layout.findViewById(R.id.textview_social_help);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.movie_checkin);
        builder.setIcon(R.drawable.ic_btn_checkin_normal);
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
                    getCallbacks().checkin(
                            mMovie,
                            String.valueOf(mMessageEditText.getText()),
                            mFacebookButton.isShown() && mFacebookButton.isChecked(),
                            mTwitterButton.isShown() && mTwitterButton.isChecked(),
                            mPathButton.isShown() && mPathButton.isChecked(),
                            mTumblrButton.isShown() && mTumblrButton.isChecked()
                    );
                }
                break;
        }
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
    }

    @Override
    public void setShareText(String shareText) {
        mMessageEditText.setText(shareText);
    }

    @Override
    public void showFacebookShare(boolean show) {
        mFacebookButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTwitterShare(boolean show) {
        mTwitterButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showPathShare(boolean show) {
        mPathButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTumblrShare(boolean show) {
        mTumblrButton.setVisibility(show ? View.VISIBLE : View.GONE);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mFacebookButton.isChecked() || mTwitterButton.isChecked() ||
                mTumblrButton.isChecked() || mPathButton.isChecked()) {
            mSocialHelperView.setVisibility(View.VISIBLE);
        } else {
            mSocialHelperView.setVisibility(View.GONE);
        }
    }
}
