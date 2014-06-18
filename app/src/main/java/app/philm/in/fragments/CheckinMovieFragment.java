/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import app.philm.in.controllers.MovieController;
import app.philm.in.drawable.TintingBitmapDrawable;
import app.philm.in.fragments.base.BasePhilmMovieDialogFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;

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
        builder.setIcon(TintingBitmapDrawable.createFromColorResource(getResources(),
                R.drawable.ic_btn_checkin, R.color.primary_accent_color));
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
