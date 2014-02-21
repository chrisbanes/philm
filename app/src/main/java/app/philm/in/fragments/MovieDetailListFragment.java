package app.philm.in.fragments;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
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
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.ColorSchemable;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.RatingBarLayout;
import app.philm.in.view.ViewRecycler;

public class MovieDetailListFragment extends BasePhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener, ColorSchemable,
        AbsListView.OnScrollListener {

    private static final Date DATE = new Date();

    private static final float BOTTOM_INSET_ALPHA = 0.75f;
    private static final float PARALLAX_FRICTION = 0.5f;

    private static final String LOG_TAG = MovieDetailListFragment.class.getSimpleName();

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";
    private static final String KEY_SCROLLVIEW_POSITION = "scroll_position";

    private final ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader> mYoutubeLoaders
            = new ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();

    @Inject ImageHelper mImageHelper;
    @Inject FlagUrlProvider mFlagUrlProvider;
    @Inject DateFormat mMediumDateFormatter;

    private PhilmMovie mMovie;

    private PhilmImageView mBackdropImageView;
    private ListView mListView;
    private DetailAdapter mDetailAdapter;

    public static MovieDetailListFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        MovieDetailListFragment fragment = new MovieDetailListFragment();
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
        return inflater.inflate(R.layout.fragment_movie_detail_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBackdropImageView = (PhilmImageView) view.findViewById(R.id.imageview_fanart);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mDetailAdapter = new DetailAdapter();

        mListView.setOnScrollListener(this);
        mListView.setAdapter(mDetailAdapter);

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
        //mParallaxScrollView.scrollScrollViewTo(mScrollViewY);
        setBottomInsetAlpha(BOTTOM_INSET_ALPHA);
    }

    @Override
    public void onPause() {
        //mScrollViewY = mParallaxScrollView.getScrollViewScrollY();
        setActionBarTitleEnabled(true);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putInt(KEY_SCROLLVIEW_POSITION, mScrollViewY);
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
    public void setRateCircleEnabled(boolean enabled) {
        //mRatingBarLayout.setRatingCircleEnabled(enabled);
    }

    @Override
    public void setCollectionButtonEnabled(boolean enabled) {
        //mCollectionButton.setEnabled(enabled);
    }

    @Override
    public void setWatchlistButtonEnabled(boolean enabled) {
        //mWatchlistButton.setEnabled(enabled);
    }

    @Override
    public void setToggleWatchedButtonEnabled(boolean enabled) {
        //mSeenButton.setEnabled(enabled);
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
        mListView.setPadding(insets.left, insets.top, insets.right, insets.bottom);

        mBackdropImageView.getLayoutParams().height += insets.top;
        mBackdropImageView.requestLayout();
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        final ArrayList<DetailItemType> items = new ArrayList<DetailItemType>();

        items.add(DetailItemType.TITLE);
        items.add(DetailItemType.BUTTONS);

        if (!TextUtils.isEmpty(mMovie.getOverview())) {
            items.add(DetailItemType.SUMMARY);
        }

        items.add(DetailItemType.RATING);
        items.add(DetailItemType.DETAILS);

        if (!PhilmCollections.isEmpty(mMovie.getTrailers())) {
            items.add(DetailItemType.TRAILERS);
        }

        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
            items.add(DetailItemType.RELATED);
        }

        if (!PhilmCollections.isEmpty(mMovie.getCast())) {
            items.add(DetailItemType.CAST);
        }

        mBackdropImageView.loadBackdropUrl(mMovie);

        mDetailAdapter.setItems(items);
    }

    @Override
    public void setColorScheme(ColorScheme colorScheme, boolean animate) {
        Preconditions.checkNotNull(colorScheme, "colorScheme cannot be null");
        //mRatingBarLayout.setColorScheme(colorScheme, animate);
        //mPosterImageView.setBackgroundColor(colorScheme.primaryAccent);
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
                final TextView summaryView = (TextView) view;
                final int defaultMaxLines = getResources()
                        .getInteger(R.integer.default_summary_maxlines);
                if (summaryView.getLineCount() == defaultMaxLines) {
                    summaryView.setMaxLines(Integer.MAX_VALUE);
                } else if (summaryView.getLineCount() > defaultMaxLines) {
                    summaryView.setMaxLines(defaultMaxLines);
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
    }

    private void setActionBarTitleEnabled(boolean enabled) {
        Activity activity = getActivity();
        if (activity != null) {
            final ActionBar ab = activity.getActionBar();
            if (ab != null) {
                ab.setDisplayShowTitleEnabled(enabled);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        // NO-OP
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if (visibleItemCount > 0 && firstVisibleItem == 0) {
            final View firstView = absListView.getChildAt(0);
            final int y = mListView.getPaddingTop() - firstView.getTop();
            final float percent = y / (float) firstView.getHeight();

            setTopInsetAlpha(percent);
            setActionBarTitleEnabled(percent >= 0.8f);

            mBackdropImageView.setVisibility(View.VISIBLE);
            final int newTop = Math.round(-y * PARALLAX_FRICTION);
            mBackdropImageView.offsetTopAndBottom(newTop - mBackdropImageView.getTop());

        } else {
            setTopInsetAlpha(1f);
            mBackdropImageView.setVisibility(View.INVISIBLE);
        }
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
            Intent intent = YouTubeIntents.createPlayVideoIntent(getActivity(),
                    (String) view.getTag());
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

    private enum DetailItemType {
        TITLE(R.layout.item_movie_detail_title),
        BUTTONS(R.layout.item_movie_detail_buttons),
        DETAILS(R.layout.item_movie_detail_details),
        RATING(R.layout.item_movie_detail_rating),
        SUMMARY(R.layout.item_movie_detail_summary),
        TRAILERS(R.layout.item_movie_detail_trailers),
        RELATED(R.layout.item_movie_detail_generic_card),
        CAST(R.layout.item_movie_detail_generic_card);

        private final int mLayoutId;

        private DetailItemType(int layoutId) {
            mLayoutId = layoutId;
        }

        int getLayoutId() {
            return mLayoutId;
        }
    }

    private class DetailAdapter extends BaseAdapter {

        private List<DetailItemType> mItems;

        public void setItems(List<DetailItemType> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public DetailItemType getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).ordinal();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getViewTypeCount() {
            return DetailItemType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).ordinal();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final DetailItemType item = getItem(position);
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "getView. Pos: " + position + ". Item: " + item.name());
            }

            if (view == null) {
                final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                view = inflater.inflate(item.getLayoutId(), viewGroup, false);
            }

            // Now bind to the view
            bindView(item, view);

            return view;
        }

        private void bindView(final DetailItemType item, final View view) {
            switch (item) {
                case TITLE:
                    bindTitle(view);
                    break;
                case BUTTONS:
                    bindButtons(view);
                    break;
                case SUMMARY:
                    bindSummary(view);
                    break;
                case DETAILS:
                    bindDetails(view);
                    break;
                case RATING:
                    bindRating(view);
                    break;
                case RELATED:
                    bindRelated(view);
                    break;
                case TRAILERS:
                    bindTrailers(view);
                    break;
                case CAST:
                    bindCast(view);
                    break;
            }
        }

        private void bindButtons(final View view) {
            CheckableImageButton seenButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_seen);
            seenButton.setOnClickListener(MovieDetailListFragment.this);
            CheatSheet.setup(seenButton);

            CheckableImageButton watchlistButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_watchlist);
            watchlistButton.setOnClickListener(MovieDetailListFragment.this);
            CheatSheet.setup(watchlistButton);

            CheckableImageButton collectionButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_collection);
            collectionButton.setOnClickListener(MovieDetailListFragment.this);
            CheatSheet.setup(collectionButton);

            updateButtonState(seenButton, mMovie.isWatched(), R.string.action_mark_seen,
                    R.string.action_mark_unseen);
            updateButtonState(watchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                    R.string.action_remove_watchlist);
            updateButtonState(collectionButton, mMovie.inCollection(), R.string.action_add_collection,
                    R.string.action_remove_collection);
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

        private void bindSummary(final View view) {
            TextView summary = (TextView) view.findViewById(R.id.textview_summary);
            summary.setText(mMovie.getOverview());
            summary.setOnClickListener(MovieDetailListFragment.this);
        }

        private void bindRelated(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindRelated");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showRelatedMovies(mMovie);
                    }
                }
            };

            RelatedMoviesAdapter adapter = new RelatedMoviesAdapter(
                    LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.related_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }

        private void populateDetailGrid(
                final ViewGroup layout,
                final MovieDetailCardLayout cardLayout,
                final View.OnClickListener seeMoreClickListener,
                final BaseAdapter adapter) {

            if (layout.getWidth() == 0) {
                layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        layout.post(new Runnable() {
                            @Override
                            public void run() {
                                populateDetailGrid(layout, cardLayout, seeMoreClickListener, adapter);
                            }
                        });
                        layout.removeOnLayoutChangeListener(this);
                    }
                });
                return;
            }

            final ViewRecycler viewRecycler = new ViewRecycler(layout);
            viewRecycler.recycleViews();

            if (!adapter.isEmpty()) {
                final int numItems = layout.getWidth() / getResources()
                        .getDimensionPixelSize(R.dimen.movie_detail_multi_item_width);

                for (int i = 0; i < Math.min(numItems, adapter.getCount()); i++) {
                    View view = adapter.getView(i, viewRecycler.getRecycledView(), layout);
                    layout.addView(view);
                }

                final boolean showSeeMore = numItems < adapter.getCount();
                cardLayout.setSeeMoreVisibility(showSeeMore);
                cardLayout.setSeeMoreOnClickListener(showSeeMore ? seeMoreClickListener : null);
            }

            viewRecycler.clearRecycledViews();
        }

        private void bindCast(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindCast");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showCastList(mMovie);
                    }
                }
            };

            MovieCastAdapter adapter = new MovieCastAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.cast_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }

        private void bindTrailers(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindTrailers");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                if (hasCallbacks()) {
//                    getCallbacks().showCastList(mMovie);
//                }
                }
            };

            MovieTrailersAdapter adapter = new MovieTrailersAdapter(
                    LayoutInflater.from(getActivity()));

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    (MovieDetailCardLayout) view,
                    seeMoreClickListener,
                    adapter);
        }

        private void bindDetails(View view) {
            MovieDetailInfoLayout runtimeLayout = (MovieDetailInfoLayout) view
                    .findViewById(R.id.layout_info_runtime);
            MovieDetailInfoLayout certificationLayout = (MovieDetailInfoLayout)
                    view.findViewById(R.id.layout_info_certification);
            MovieDetailInfoLayout genreLayout = (MovieDetailInfoLayout) view
                    .findViewById(R.id.layout_info_genres);
            MovieDetailInfoLayout releasedInfoLayout = (MovieDetailInfoLayout) view
                    .findViewById(R.id.layout_info_released);
            MovieDetailInfoLayout languageLayout = (MovieDetailInfoLayout) view
                    .findViewById(R.id.layout_info_language);

            if (mMovie.getRuntime() > 0) {
                runtimeLayout.setContentText(
                        getString(R.string.movie_details_runtime_content, mMovie.getRuntime()));
                runtimeLayout.setVisibility(View.VISIBLE);
            } else {
                runtimeLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getCertification())) {
                certificationLayout.setContentText(mMovie.getCertification());
                certificationLayout.setVisibility(View.VISIBLE);
            } else {
                certificationLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getGenres())) {
                genreLayout.setContentText(mMovie.getGenres());
                genreLayout.setVisibility(View.VISIBLE);
            } else {
                genreLayout.setVisibility(View.GONE);
            }

            if (mMovie.getReleasedTime() > 0) {
                DATE.setTime(mMovie.getReleasedTime());
                releasedInfoLayout.setContentText(mMediumDateFormatter.format(DATE));
                releasedInfoLayout.setVisibility(View.VISIBLE);

                final String countryCode = mMovie.getReleaseCountryCode();
                if (!TextUtils.isEmpty(countryCode)) {
                    loadFlagImage(countryCode, releasedInfoLayout);
                }
            } else {
                releasedInfoLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMovie.getMainLanguageTitle())) {
                languageLayout.setContentText(mMovie.getMainLanguageTitle());
                languageLayout.setVisibility(View.VISIBLE);
            } else {
                languageLayout.setVisibility(View.GONE);
            }
        }

        private void bindRating(View view) {
            RatingBarLayout ratingBarLayout = (RatingBarLayout) view;

            if (mMovie.getUserRatingAdvanced() != PhilmMovie.NOT_SET) {
                ratingBarLayout.showUserRating(mMovie.getUserRatingAdvanced());
            } else {
                ratingBarLayout.showRatePrompt();
            }
            ratingBarLayout.setRatingGlobalPercentage(mMovie.getAverageRatingPercent());
            ratingBarLayout.setRatingGlobalVotes(mMovie.getAverageRatingVotes());
            ratingBarLayout.setRatingCircleClickListener(MovieDetailListFragment.this);
        }

        private void bindTitle(View view) {
            TextView titleTextView = (TextView) view.findViewById(R.id.textview_title);
            titleTextView.setText(getString(R.string.movie_title_year,
                    mMovie.getTitle(), mMovie.getYear()));

            PhilmImageView posterImageView = (PhilmImageView)
                    view.findViewById(R.id.imageview_poster);
            posterImageView.loadPosterUrl(mMovie, mPosterListener);
        }

        private void loadFlagImage(final String countryCode,
                final MovieDetailInfoLayout infoLayout) {
            final String flagUrl = mFlagUrlProvider.getCountryFlagUrl(countryCode);
            final int width = getResources()
                    .getDimensionPixelSize(R.dimen.movie_detail_flag_width);
            final int height = getResources()
                    .getDimensionPixelSize(R.dimen.movie_detail_flag_height);

            Picasso.with(getActivity())
                    .load(mImageHelper.getResizedUrl(flagUrl, width, height, "gif"))
                    .into(infoLayout);
        }
    }

    private final PhilmImageView.Listener mPosterListener = new PhilmImageView.Listener() {
        @Override
        public void onSuccess(PhilmImageView imageView, Bitmap bitmap) {
            imageView.setVisibility(View.VISIBLE);

            if (mMovie.getColorScheme() == null) {
                new ColorCalculatorTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bitmap);
            }
        }

        @Override
        public void onError(PhilmImageView imageView) {
            imageView.setVisibility(View.GONE);
        }
    };
}
