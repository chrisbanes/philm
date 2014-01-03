package app.philm.in.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.PhilmMovieFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.PhilmActionButton;
import app.philm.in.view.RatingBarLayout;
import app.philm.in.view.ViewRecycler;

public class MovieDetailFragment extends PhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private PhilmMovie mMovie;

    private TraktImageHelper mTraktImageHelper;

    private TextView mTitleTextView;
    private TextView mSummaryTextView;
    private ImageView mFanartImageView;
    private ImageView mPosterImageView;

    private RatingBarLayout mRatingBarLayout;

    private ViewRecycler mRelatedViewRecycler;
    private LinearLayout mRelatedLayout;

    private PhilmActionButton mSeenButton, mWatchlistButton, mCollectionButton;

    public static MovieDetailFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId),"movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

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
        mRatingBarLayout = (RatingBarLayout) view.findViewById(R.id.rating_bar_layout);

        mSummaryTextView = (TextView) view.findViewById(R.id.textview_summary);
        mSummaryTextView.setOnClickListener(this);

        mSeenButton = (PhilmActionButton) view.findViewById(R.id.btn_seen);
        mSeenButton.setOnClickListener(this);

        mWatchlistButton = (PhilmActionButton) view.findViewById(R.id.btn_watchlist);
        mWatchlistButton.setOnClickListener(this);

        mCollectionButton = (PhilmActionButton) view.findViewById(R.id.btn_collection);
        mCollectionButton.setOnClickListener(this);

        mRelatedLayout = (LinearLayout) view.findViewById(R.id.layout_related);
        mRelatedViewRecycler = new ViewRecycler(mRelatedLayout);
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();
    }

    @Override
    public void setRateCircleEnabled(boolean enabled) {
        mRatingBarLayout.setRatingCircleEnabled(enabled);
    }

    @Override
    public void setCollectionButtonEnabled(boolean enabled) {
        mCollectionButton.setEnabled(enabled);
    }

    @Override
    public void setWatchlistButtonEnabled(boolean enabled) {
        mWatchlistButton.setEnabled(enabled);
    }

    @Override
    public void setToggleWatchedButtonEnabled(boolean enabled) {
        mSeenButton.setEnabled(enabled);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.DETAIL;
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

        Picasso.with(getActivity())
                .load(mTraktImageHelper.getPosterUrl(mMovie))
                .into(mPosterImageView);

        mTitleTextView.setText(getString(R.string.movie_title_year, mMovie.getTitle(),
                mMovie.getYear()));

        mSummaryTextView.setText(mMovie.getOverview());

        updateButtonState(mSeenButton, mMovie.isWatched(), R.string.action_mark_seen,
                R.string.action_mark_unseen);
        updateButtonState(mWatchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                R.string.action_remove_watchlist);
        updateButtonState(mCollectionButton, mMovie.inCollection(), R.string.action_add_collection,
                R.string.action_remove_collection);

        if (mMovie.getUserRatingAdvanced() != PhilmMovie.NOT_SET) {
            mRatingBarLayout.showUserRating(mMovie.getUserRatingAdvanced());
        } else {
            mRatingBarLayout.showRatePrompt();
        }
        mRatingBarLayout.setRatingGlobalPercentage(mMovie.getRatingPercent());
        mRatingBarLayout.setRatingGlobalVotes(mMovie.getRatingVotes());
        mRatingBarLayout.setRatingCircleClickListener(this);

        populateRelatedMovies(mRelatedViewRecycler);
    }

    private void populateRelatedMovies(final ViewRecycler viewRecycler) {
        viewRecycler.recycleViews();

        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            for (PhilmMovie movie : mMovie.getRelated()) {
                View view = viewRecycler.getRecycledView();
                if (view == null) {
                    view = inflater.inflate(R.layout.item_related_movie, mRelatedLayout, false);
                }

                final TextView title = (TextView) view.findViewById(R.id.textview_title);
                title.setText(movie.getTitle());

                final ImageView imageView = (ImageView) view.findViewById(R.id.imageview_poster);
                Picasso.with(getActivity())
                        .load(mTraktImageHelper.getPosterUrl(movie))
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                title.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                title.setVisibility(View.VISIBLE);
                            }
                        });

                mRelatedLayout.addView(view);
            }
        }

        viewRecycler.clearRecycledViews();
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

    @Override
    public final void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_seen:
                if (hasCallbacks()) {
                    getCallbacks().toggleMovieSeen(mMovie);
                }
                break;
            case R.id.btn_watchlist:
                if (hasCallbacks()) {
                    getCallbacks().toggleInWatchlist(mMovie);
                }
                break;
            case R.id.btn_collection:
                if (hasCallbacks()) {
                    getCallbacks().toggleInCollection(mMovie);
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
            case R.id.rcv_rating:
                if (hasCallbacks()) {
                    getCallbacks().showRateMovie(mMovie);
                }
                break;
        }
    }
}
