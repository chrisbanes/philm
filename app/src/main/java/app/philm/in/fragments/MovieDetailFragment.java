package app.philm.in.fragments;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import app.philm.in.AndroidConstants;
import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieFragment;
import app.philm.in.model.ColorScheme;
import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmTrailer;
import app.philm.in.util.DominantColorCalculator;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.ViewUtils;
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.ColorSchemable;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.ParallaxContentScrollView;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.RatingBarLayout;
import app.philm.in.view.ViewRecycler;

public class MovieDetailFragment extends BasePhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener, ColorSchemable,
        ParallaxContentScrollView.OnContentViewScrollListener {

    private static final Date DATE = new Date();

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";
    private static final String KEY_SCROLLVIEW_POSITION = "scroll_position";

    private final ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader> mYoutubeLoaders
            = new ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();

    @Inject ImageHelper mImageHelper;
    @Inject FlagUrlProvider mFlagUrlProvider;
    @Inject DateFormat mMediumDateFormatter;

    private PhilmMovie mMovie;

    private ParallaxContentScrollView mParallaxScrollView;
    private View mScrollViewContent;

    private TextView mTitleTextView;
    private TextView mSummaryTextView;
    private PhilmImageView mFanartImageView;
    private PhilmImageView mPosterImageView;

    private RatingBarLayout mRatingBarLayout;

    private MovieDetailCardLayout mDetailsCardLayout;
    private MovieDetailCardLayout mRelatedCardLayout;
    private MovieDetailCardLayout mCastCardLayout;
    private MovieDetailCardLayout mTrailersCardLayout;
    private MovieDetailInfoLayout mReleasedInfoLayout;
    private MovieDetailInfoLayout mRunTimeInfoLayout;
    private MovieDetailInfoLayout mCertificationInfoLayout;
    private MovieDetailInfoLayout mGenresInfoLayout;
    private MovieDetailInfoLayout mLanguageInfoLayout;

    private ViewSwitcher mCastSwitcher;
    private LinearLayout mCastLayout;
    private ViewSwitcher mRelatedSwitcher;
    private LinearLayout mRelatedLayout;
    private ViewSwitcher mTrailersSwitcher;
    private LinearLayout mTrailersLayout;

    private CheckableImageButton mSeenButton, mWatchlistButton, mCollectionButton;

    private int mScrollViewY;

    private ImageUiState mUiState;

    public static MovieDetailFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhilmApplication.from(getActivity()).inject(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mParallaxScrollView = (ParallaxContentScrollView) view.findViewById(R.id.content_scrollview);
        mParallaxScrollView.setOnContentViewScrollListener(this);

        mScrollViewContent = view.findViewById(R.id.content);

        mFanartImageView = (PhilmImageView) view.findViewById(R.id.imageview_fanart);
        mPosterImageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
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

        mCastSwitcher = (ViewSwitcher) view.findViewById(R.id.viewswitcher_cast);
        mCastLayout = (LinearLayout) view.findViewById(R.id.layout_cast);

        mTrailersSwitcher = (ViewSwitcher) view.findViewById(R.id.viewswitcher_trailers);
        mTrailersLayout = (LinearLayout) view.findViewById(R.id.layout_trailers);

        mRunTimeInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_runtime);
        mCertificationInfoLayout = (MovieDetailInfoLayout)
                view.findViewById(R.id.layout_info_certification);
        mGenresInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_genres);
        mReleasedInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_released);
        mLanguageInfoLayout = (MovieDetailInfoLayout) view.findViewById(R.id.layout_info_language);

        mDetailsCardLayout = (MovieDetailCardLayout)
                view.findViewById(R.id.movie_detail_card_details);
        mRelatedCardLayout = (MovieDetailCardLayout)
                view.findViewById(R.id.movie_detail_card_related);
        mCastCardLayout = (MovieDetailCardLayout)
                view.findViewById(R.id.movie_detail_card_cast);
        mTrailersCardLayout = (MovieDetailCardLayout)
                view.findViewById(R.id.movie_detail_card_trailers);

        if (savedInstanceState != null) {
            mScrollViewY = savedInstanceState.getInt(KEY_SCROLLVIEW_POSITION);
        }

        super.onViewCreated(view, savedInstanceState);
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
    public void onResume() {
        super.onResume();
        mParallaxScrollView.scrollScrollViewTo(mScrollViewY);
    }

    @Override
    public void onPause() {
        mScrollViewY = mParallaxScrollView.getScrollViewScrollY();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SCROLLVIEW_POSITION, mScrollViewY);
        super.onSaveInstanceState(outState);
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
    public void showMovieCastLoadingProgress(final boolean visible) {
        mCastSwitcher.setDisplayedChild(visible ? 1 : 0);
    }

    @Override
    public void showTrailersLoadingProgress(boolean visible) {
        mTrailersSwitcher.setDisplayedChild(visible ? 1 : 0);
    }

    @Override
    public void setRateCircleEnabled(boolean enabled) {
        mRatingBarLayout.setRatingCircleEnabled(enabled);
    }

    @Override
    public void setMovieTrailersVisibility(boolean visible) {
        mTrailersCardLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMovieCastVisibility(boolean visible) {
        mCastCardLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMovieRelatedVisibility(boolean visible) {
        mRelatedCardLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
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

    @Override
    public void onInsetsChanged(Rect insets) {
        mScrollViewContent.setPadding(0, 0, 0, insets.bottom);
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        if (mUiState == null) {
            mUiState = new ImageUiState();
        }

        mUiState.loadPoster();
        mUiState.loadBackdrop();

        mTitleTextView.setText(getString(R.string.movie_title_year, mMovie.getTitle(),
                mMovie.getYear()));

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
        mRatingBarLayout.setRatingGlobalPercentage(mMovie.getAverageRatingPercent());
        mRatingBarLayout.setRatingGlobalVotes(mMovie.getAverageRatingVotes());
        mRatingBarLayout.setRatingCircleClickListener(this);

        final List<PhilmMovie> related = mMovie.getRelated();
        if (related == null || related.size() != mRelatedLayout.getChildCount()) {
            populateRelatedMovies();
        }

        final List<PhilmCast> cast = mMovie.getCast();
        if (cast == null || cast.size() != mCastLayout.getChildCount()) {
            populateMovieCast();
        }

        final List<PhilmTrailer> trailers = mMovie.getTrailers();
        if (trailers == null || trailers.size() != mTrailersLayout.getChildCount()) {
            populateTrailers();
        }

        if (mMovie.getRuntime() > 0) {
            mRunTimeInfoLayout.setContentText(
                    getString(R.string.movie_details_runtime_content, mMovie.getRuntime()));
            mRunTimeInfoLayout.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mMovie.getCertification())) {
            mCertificationInfoLayout.setContentText(mMovie.getCertification());
            mCertificationInfoLayout.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mMovie.getGenres())) {
            mGenresInfoLayout.setContentText(mMovie.getGenres());
            mGenresInfoLayout.setVisibility(View.VISIBLE);
        }
        if (mMovie.getReleasedTime() > 0) {
            DATE.setTime(mMovie.getReleasedTime());
            mReleasedInfoLayout.setContentText(mMediumDateFormatter.format(DATE));
            mReleasedInfoLayout.setVisibility(View.VISIBLE);

            final String countryCode = mMovie.getReleaseCountryCode();
            if (!TextUtils.isEmpty(countryCode)) {
                loadFlagImage(countryCode, mReleasedInfoLayout);
            }
        }

        if (!TextUtils.isEmpty(mMovie.getMainLanguageTitle())) {
            mLanguageInfoLayout.setContentText(mMovie.getMainLanguageTitle());
            mLanguageInfoLayout.setVisibility(View.VISIBLE);
        }

        if (mMovie.getColorScheme() != null) {
            setColorScheme(mMovie.getColorScheme(), false);
        }
    }

    private void loadFlagImage(final String countryCode, final MovieDetailInfoLayout infoLayout) {
        final String flagUrl = mFlagUrlProvider.getCountryFlagUrl(countryCode);
        final int width = getResources().getDimensionPixelSize(R.dimen.movie_detail_flag_width);
        final int height = getResources().getDimensionPixelSize(R.dimen.movie_detail_flag_height);

        Picasso.with(getActivity())
                .load(mImageHelper.getResizedUrl(flagUrl, width, height, "gif"))
                .into(infoLayout);
    }

    private void populateRelatedMovies() {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "populateRelatedMovies");
        }

        final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCallbacks()) {
                    getCallbacks().showRelatedMovies(mMovie);
                }
            }
        };

        RelatedMoviesAdapter adapter = new RelatedMoviesAdapter(getActivity().getLayoutInflater());

        populateDetailGrid(
                mRelatedLayout,
                mRelatedCardLayout,
                seeMoreClickListener,
                adapter);
    }

    private void populateDetailGrid(final ViewGroup layout,
                                    final MovieDetailCardLayout cardLayout,
                                    final View.OnClickListener seeMoreClickListener,
                                    final BaseAdapter adapter) {

        final ViewRecycler viewRecycler = new ViewRecycler(layout);
        viewRecycler.recycleViews();

        if (adapter.getCount() > 0) {
            final int numItems = getResources().getInteger(R.integer.number_detail_items);

            for (int i = 0; i < Math.min(numItems, adapter.getCount()); i++) {
                View view = viewRecycler.getRecycledView();
                view = adapter.getView(i, view, layout);
                layout.addView(view);
            }

            final boolean showSeeMore = numItems < adapter.getCount();
            cardLayout.setSeeMoreVisibility(showSeeMore);
            cardLayout.setSeeMoreOnClickListener(showSeeMore ? seeMoreClickListener : null);
        }

        viewRecycler.clearRecycledViews();
    }

    private void populateMovieCast() {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "populateMovieCast");
        }

        final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCallbacks()) {
                    getCallbacks().showCastList(mMovie);
                }
            }
        };

        MovieCastAdapter adapter = new MovieCastAdapter(getActivity().getLayoutInflater());

        populateDetailGrid(
                mCastLayout,
                mCastCardLayout,
                seeMoreClickListener,
                adapter);

    }

    private void populateTrailers() {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "populateTrailers");
        }

        final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (hasCallbacks()) {
//                    getCallbacks().showCastList(mMovie);
//                }
            }
        };

        MovieTrailersAdapter adapter = new MovieTrailersAdapter(getActivity().getLayoutInflater());

        populateDetailGrid(
                mTrailersLayout,
                mTrailersCardLayout,
                seeMoreClickListener,
                adapter);

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
    public void setColorScheme(ColorScheme colorScheme, boolean animate) {
        Preconditions.checkNotNull(colorScheme, "colorScheme cannot be null");
        mRatingBarLayout.setColorScheme(colorScheme, animate);
        mPosterImageView.setBackgroundColor(colorScheme.primaryAccent);
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

    private YouTubeThumbnailLoader getYoutubeThumbnailLoader(YouTubeThumbnailView view) {
        return mYoutubeLoaders.get(view);
    }

    private void captureYoutubeThumbnailLoader(YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
        mYoutubeLoaders.put(view, loader);
    }

    private void clearYoutubeLoaders() {
        for (YouTubeThumbnailLoader loader : mYoutubeLoaders.values()) {
            loader.release();
        }
        mYoutubeLoaders.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearYoutubeLoaders();
        mUiState.reset();
    }

    @Override
    public void onContentViewScrolled(float percent) {
        setTopInsetAlpha(percent);
    }

    private class RelatedMoviesAdapter extends BaseAdapter {

        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        RelatedMoviesAdapter(LayoutInflater inflater) {
            mInflater = inflater;

            mItemOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showMovieDetail((PhilmMovie) view.getTag());
                    }
                }
            };
        }

        @Override
        public int getCount() {
            if (mMovie != null && !PhilmCollections.isEmpty(mMovie.getRelated())) {
                return mMovie.getRelated().size();
            } else {
                return 0;
            }
        }

        @Override
        public PhilmMovie getItem(int position) {
            return mMovie.getRelated().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.item_related_movie, viewGroup, false);
            }

            final PhilmMovie movie = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(movie.getTitle());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadPosterUrl(movie);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(movie);

            return view;
        }
    }

    private class MovieCastAdapter extends BaseAdapter {

        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        MovieCastAdapter(LayoutInflater inflater) {
            mInflater = inflater;
            // TODO
            mItemOnClickListener = null;
        }

        @Override
        public int getCount() {
            if (mMovie != null && !PhilmCollections.isEmpty(mMovie.getCast())) {
                return mMovie.getCast().size();
            } else {
                return 0;
            }
        }

        @Override
        public PhilmCast getItem(int position) {
            return mMovie.getCast().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.item_related_movie, viewGroup, false);
            }

            final PhilmCast cast = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(cast.getName());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadProfileUrl(cast);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(cast);

            return view;
        }
    }

    private class MovieTrailersAdapter extends BaseAdapter {

        private final YoutubeViewListener mYoutubeViewListener;
        private final LayoutInflater mInflater;

        MovieTrailersAdapter(LayoutInflater inflater) {
            mInflater = inflater;
            mYoutubeViewListener = new YoutubeViewListener();
        }

        @Override
        public int getCount() {
            if (mMovie != null && !PhilmCollections.isEmpty(mMovie.getTrailers())) {
                return mMovie.getTrailers().size();
            } else {
                return 0;
            }
        }

        @Override
        public PhilmTrailer getItem(int position) {
            return mMovie.getTrailers().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final PhilmTrailer trailer = getItem(position);

            switch (trailer.getSource()) {
                case YOUTUBE:
                    final boolean viewWasInflated = view == null;
                    if (view == null) {
                        view = mInflater.inflate(R.layout.item_movie_trailer_youtube,
                                viewGroup, false);
                    }

                    final YouTubeThumbnailView youtubeView = (YouTubeThumbnailView)
                            view.findViewById(R.id.imageview_youtube_thumbnail);
                    youtubeView.setTag(trailer.getId());

                    final YouTubeThumbnailLoader loader = getYoutubeThumbnailLoader(youtubeView);
                    if (loader == null) {
                        if (viewWasInflated) {
                            youtubeView.initialize(
                                    AndroidConstants.GOOGLE_CLIENT_KEY,
                                    mYoutubeViewListener);
                        }
                    } else {
                        loader.setVideo(trailer.getId());
                    }

                    final TextView title = (TextView) view.findViewById(R.id.textview_title);
                    title.setText(trailer.getName());
            }

            return view;
        }
    }

    private class YoutubeViewListener implements YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener, View.OnClickListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view,
                YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            captureYoutubeThumbnailLoader(view, loader);
            loader.setVideo((String) view.getTag());
        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view,
                YouTubeInitializationResult loader) {
            view.setOnClickListener(null);
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String id) {
            view.setOnClickListener(this);
        }

        @Override
        public void onThumbnailError(
                YouTubeThumbnailView view,
                YouTubeThumbnailLoader.ErrorReason errorReason) {
            view.setOnClickListener(null);
        }

        @Override
        public void onClick(View view) {
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                    getActivity(),
                    AndroidConstants.GOOGLE_CLIENT_KEY,
                    (String) view.getTag(),
                    0, // start time
                    true, // autoplay
                    false // lightbox
            );
            startActivity(intent);
        }
    }

    private class ColorCalculatorTask extends AsyncTask<Bitmap, Void, DominantColorCalculator> {

        @Override
        protected DominantColorCalculator doInBackground(Bitmap... params) {
            final Bitmap bitmap = params[0];
            if (bitmap != null && !bitmap.isRecycled()) {
                return new DominantColorCalculator(bitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(
                DominantColorCalculator colorCalculator) {
            super.onPostExecute(colorCalculator);

            if (colorCalculator != null) {
                final ColorScheme scheme = colorCalculator.getColorScheme();
                if (scheme != null) {
                    if (mMovie != null) {
                        mMovie.setColorScheme(scheme);
                    }
                    setColorScheme(scheme, true);
                }
            }
        }

    }

    private class ImageUiState {

        private String mPosterPath;
        private String mBackdropPath;

        public void loadPoster() {
            if (!Objects.equal(mMovie.getPosterUrl(), mPosterPath)) {
                mPosterPath = mMovie.getPosterUrl();

                mPosterImageView.loadPosterUrl(mMovie, new PhilmImageView.Listener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        mPosterImageView.setVisibility(View.VISIBLE);

                        if (mMovie.getColorScheme() == null) {
                            new ColorCalculatorTask().executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR, bitmap);
                        }
                    }

                    @Override
                    public void onError() {
                        mPosterImageView.setVisibility(View.GONE);
                    }
                });
            }
        }

        public void loadBackdrop() {
            if (!Objects.equal(mMovie.getBackdropUrl(), mBackdropPath)) {
                mBackdropPath = mMovie.getBackdropUrl();
                mFanartImageView.loadBackdropUrl(mMovie);
            }
        }

        void reset() {
            mPosterPath = null;
            mBackdropPath = null;
        }

    }
}
