package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Date;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.Container;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.PhilmMovieFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.ViewUtils;
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.RatingBarLayout;
import app.philm.in.view.ViewRecycler;

public class MovieDetailFragment extends PhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener {

    private static final Date DATE = new Date();

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private PhilmMovie mMovie;

    private FlagUrlProvider mFlagUrlProvider;
    private TraktImageHelper mTraktImageHelper;

    private TextView mTitleTextView;
    private TextView mSummaryTextView;
    private ImageView mFanartImageView;
    private ImageView mPosterImageView;
    private PhilmImageView mFlagImageView;

    private RatingBarLayout mRatingBarLayout;

    private ViewRecycler mRelatedViewRecycler;
    private ViewSwitcher mRelatedSwitcher;
    private LinearLayout mRelatedLayout;

    private MovieDetailInfoLayout mReleasedInfoLayout;
    private MovieDetailInfoLayout mRunTimeInfoLayout;
    private MovieDetailInfoLayout mCertificationInfoLayout;
    private MovieDetailInfoLayout mGenresInfoLayout;

    private CheckableImageButton mSeenButton, mWatchlistButton, mCollectionButton;

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
        mFlagUrlProvider = Container.getInstance(getActivity()).getFlagUrlProvider();
        mTraktImageHelper = Container.getInstance(getActivity()).getTraktImageHelper();
        setHasOptionsMenu(true);
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

        mSeenButton = (CheckableImageButton) view.findViewById(R.id.btn_seen);
        mSeenButton.setOnClickListener(this);
        CheatSheet.setup(mSeenButton);

        mWatchlistButton = (CheckableImageButton) view.findViewById(R.id.btn_watchlist);
        mWatchlistButton.setOnClickListener(this);
        CheatSheet.setup(mWatchlistButton);

        mCollectionButton = (CheckableImageButton) view.findViewById(R.id.btn_collection);
        mCollectionButton.setOnClickListener(this);
        CheatSheet.setup(mCollectionButton);

        mRelatedSwitcher = (ViewSwitcher) view.findViewById(R.id.viewswitcher_related);
        mRelatedLayout = (LinearLayout) view.findViewById(R.id.layout_related);
        mRelatedViewRecycler = new ViewRecycler(mRelatedLayout);

        mRunTimeInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_runtime);
        mCertificationInfoLayout =
                (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_certification);
        mGenresInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_genres);
        mReleasedInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_released);

        mFlagImageView = (PhilmImageView) view.findViewById(R.id.imageview_flag);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (hasCallbacks()) {
                    getCallbacks().refresh();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();

        if (movie != null && hasCallbacks()) {
            getCallbacks().onTitleChanged(movie.getTitle());
        }
    }

    @Override
    public void showRelatedMoviesLoadingProgress(final boolean visible) {
        mRelatedSwitcher.setDisplayedChild(visible ? 1 : 0);
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

    @Override
    public String getUiTitle() {
        if (mMovie != null) {
            return mMovie.getTitle();
        }
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        final Container container = Container.getInstance(getActivity());

        if (mFanartImageView.getDrawable() == null) {
            Picasso.with(getActivity())
                    .load(mTraktImageHelper.getFanartUrl(mMovie))
                    .into(mFanartImageView);
        }

        if (mPosterImageView.getDrawable() == null) {
            Picasso.with(getActivity())
                    .load(mTraktImageHelper.getPosterUrl(mMovie, TraktImageHelper.TYPE_SMALL))
                    .into(mPosterImageView);
        }

        if (ViewUtils.isEmpty(mTitleTextView)) {
            mTitleTextView.setText(getString(R.string.movie_title_year, mMovie.getTitle(),
                    mMovie.getYear()));
        }

        if (ViewUtils.isEmpty(mSummaryTextView)) {
            mSummaryTextView.setText(mMovie.getOverview());
        }

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

        final List<PhilmMovie> related = mMovie.getRelated();
        if (related == null || related.size() != mRelatedLayout.getChildCount()) {
            populateRelatedMovies(mRelatedViewRecycler);
        }

        mRunTimeInfoLayout.setContentText(
                getString(R.string.movie_details_runtime_content, mMovie.getRuntime()));
        mCertificationInfoLayout.setContentText(mMovie.getCertification());
        mGenresInfoLayout.setContentText(mMovie.getGenres());

        if (ViewUtils.isEmpty(mReleasedInfoLayout.getContentTextView())) {
            DATE.setTime(mMovie.getReleasedTime());
            mReleasedInfoLayout.setContentText(container.getMediumDateFormat().format(DATE));
        }

        final String countryCode = mMovie.getLocalizedCountryCode();
        if (!TextUtils.isEmpty(countryCode)) {
            mFlagImageView.setVisibility(View.VISIBLE);
            mFlagImageView.loadUrl(mFlagUrlProvider.getCountryFlagUrl(countryCode));
        } else {
            mFlagImageView.setVisibility(View.GONE);
        }
    }

    private void populateRelatedMovies(final ViewRecycler viewRecycler) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "populateRelatedMovies");
        }

        viewRecycler.recycleViews();

        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            final View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showMovieDetail((PhilmMovie) view.getTag());
                    }
                }
            };

            for (PhilmMovie movie : mMovie.getRelated()) {
                View view = viewRecycler.getRecycledView();
                if (view == null) {
                    view = inflater.inflate(R.layout.item_related_movie, mRelatedLayout, false);
                }

                final TextView title = (TextView) view.findViewById(R.id.textview_title);
                title.setText(movie.getTitle());

                final ImageView imageView = (ImageView) view.findViewById(R.id.imageview_poster);
                Picasso.with(getActivity())
                        .load(mTraktImageHelper.getPosterUrl(movie, TraktImageHelper.TYPE_SMALL))
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

                view.setOnClickListener(clickListener);
                view.setTag(movie);

                mRelatedLayout.addView(view);
            }
        }

        viewRecycler.clearRecycledViews();
    }

    private void updateButtonState(CheckableImageButton button, final boolean checked,
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
