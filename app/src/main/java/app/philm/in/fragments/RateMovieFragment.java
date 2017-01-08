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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieDialogFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.trakt.TraktUtils;

public class RateMovieFragment extends BasePhilmMovieDialogFragment
        implements DialogInterface.OnClickListener, MovieController.MovieRateUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private TextView mRatingDescriptionTextView;
    private RatingBar mRatingBar;

    private String[] mRatingDescriptions;

    private PhilmMovie mMovie;

    private CheckBox mMarkMovieWatchedCheckbox;

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

        mMarkMovieWatchedCheckbox = (CheckBox) layout.findViewById(R.id.checkbox_mark_watched);

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
                if (hasCallbacks()) {
                    getCallbacks().submitRating(mMovie, TraktUtils.mapIntToRating(getRating()));

                    if (mMarkMovieWatchedCheckbox.getVisibility() == View.VISIBLE
                            && mMarkMovieWatchedCheckbox.isChecked()) {
                        getCallbacks().toggleMovieSeen(mMovie);
                    }
                }
                break;
        }
    }

    @Override
    public void setMarkMovieWatchedCheckboxVisible(boolean visible) {
        mMarkMovieWatchedCheckbox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        mRatingBar.setEnabled(movie != null);

        if (movie != null) {
            mRatingBar.setRating(movie.getUserRatingAdvanced() / 2f);
            updateRatingDescriptionText();
        }
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
