package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.PhilmActionButton;

public class MovieDetailFragment extends Fragment implements MovieController.MovieDetailUi,
        View.OnClickListener {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";
    private static final String KEY_QUERY_TYPE = "query_type";

    private MovieController.MovieUiCallbacks mCallbacks;
    private PhilmMovie mMovie;

    private TraktImageHelper mTraktImageHelper;

    private TextView mTitleTextView;
    private TextView mSummaryTextView;
    private ImageView mFanartImageView;
    private ImageView mPosterImageView;

    private PhilmActionButton mSeenButton, mWatchlistButton, mCollectionButton;

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
        mPosterImageView = (ImageView) view.findViewById(R.id.imageview_poster);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mSummaryTextView = (TextView) view.findViewById(R.id.textview_summary);
        mSummaryTextView.setOnClickListener(this);

        mSeenButton = (PhilmActionButton) view.findViewById(R.id.btn_seen);
        mSeenButton.setOnClickListener(this);

        mWatchlistButton = (PhilmActionButton) view.findViewById(R.id.btn_watchlist);
        mWatchlistButton.setOnClickListener(this);

        mCollectionButton = (PhilmActionButton) view.findViewById(R.id.btn_collection);
        mCollectionButton.setOnClickListener(this);
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
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();
    }

    @Override
    public void showError(NetworkError error) {
        // TODO: Show something good here
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
                .load(mTraktImageHelper.getFanartUrl(mMovie.getMovie()))
                .into(mFanartImageView);

        Picasso.with(getActivity())
                .load(mTraktImageHelper.getPosterUrl(mMovie.getMovie()))
                .into(mPosterImageView);

        mTitleTextView.setText(getString(R.string.movie_title_year, mMovie.getTitle(),
                mMovie.getYear()));

        mSummaryTextView.setText(mMovie.getMovie().overview);

        updateButtonState(mSeenButton, mMovie.isWatched(), R.string.action_mark_seen,
                R.string.action_mark_unseen);
        updateButtonState(mWatchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                R.string.action_remove_watchlist);
        updateButtonState(mCollectionButton, mMovie.inCollection(), R.string.action_add_collection,
                R.string.action_remove_collection);
    }

    private void updateButtonState(PhilmActionButton button, final boolean checked,
            final int toCheckDesc, final int toUncheckDesc) {
        button.setChecked(checked);
        if (checked) {
            button.setContentDescription(getString(toUncheckDesc));
        } else {
            button.setContentDescription(getString(toCheckDesc));
        }
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }

    @Override
    public final void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_seen:
                if (mCallbacks != null) {
                    mCallbacks.toggleMovieSeen(mMovie);
                }
                break;
            case R.id.btn_watchlist:
                if (mCallbacks != null) {
                    mCallbacks.toggleInWatchlist(mMovie);
                }
                break;
            case R.id.btn_collection:
                if (mCallbacks != null) {
                    mCallbacks.toggleInCollection(mMovie);
                }
                break;
            case R.id.textview_summary:
                final int defaultMaxLines = getResources()
                        .getInteger(R.integer.default_summary_maxlines);
                if (mSummaryTextView.getLineCount() == defaultMaxLines) {
                    mSummaryTextView.setMaxLines(Integer.MAX_VALUE);
                } else if (mSummaryTextView.getLineCount() > defaultMaxLines) {
                    mSummaryTextView.setMaxLines(defaultMaxLines);
                }
                break;
        }
    }
}
