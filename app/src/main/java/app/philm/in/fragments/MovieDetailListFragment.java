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
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.ColorSchemable;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.ViewRecycler;

public class MovieDetailListFragment extends BasePhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener, ColorSchemable {

    private static final Date DATE = new Date();
    private static final float BOTTOM_INSET_ALPHA = 0.75f;

    private static final String LOG_TAG = MovieDetailListFragment.class.getSimpleName();

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";
    private static final String KEY_SCROLLVIEW_POSITION = "scroll_position";

    private final ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader> mYoutubeLoaders
            = new ArrayMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();

    @Inject ImageHelper mImageHelper;
    @Inject FlagUrlProvider mFlagUrlProvider;
    @Inject DateFormat mMediumDateFormatter;

    private PhilmMovie mMovie;

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
        mListView = (ListView) view.findViewById(android.R.id.list);
        mDetailAdapter = new DetailAdapter();
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
        //mParallaxScrollView.setInsets(insets);
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        final ArrayList<DetailItemType> items = new ArrayList<DetailItemType>();

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

        mDetailAdapter.setItems(items);
    }

    private void loadFlagImage(final String countryCode, final MovieDetailInfoLayout infoLayout) {
        final String flagUrl = mFlagUrlProvider.getCountryFlagUrl(countryCode);
        final int width = getResources().getDimensionPixelSize(R.dimen.movie_detail_flag_width);
        final int height = getResources().getDimensionPixelSize(R.dimen.movie_detail_flag_height);

        Picasso.with(getActivity())
                .load(mImageHelper.getResizedUrl(flagUrl, width, height, "gif"))
                .into(infoLayout);
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
        //mUiState.reset();
    }

    //@Override
    //public void onContentViewScrolled(float percent) {
        //setTopInsetAlpha(percent);
        //setActionBarTitleEnabled(percent >= 0.7f);
    //}

    private void setActionBarTitleEnabled(boolean enabled) {
        Activity activity = getActivity();
        if (activity != null) {
            final ActionBar ab = activity.getActionBar();
            if (ab != null) {
                ab.setDisplayShowTitleEnabled(enabled);
            }
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

//    private class ImageUiState {
//
//        private String mPosterPath;
//        private String mBackdropPath;
//
//        public void loadPoster() {
//            if (!Objects.equal(mMovie.getPosterUrl(), mPosterPath)) {
//                mPosterPath = mMovie.getPosterUrl();
//
//                mPosterImageView.loadPosterUrl(mMovie, new PhilmImageView.Listener() {
//                    @Override
//                    public void onSuccess(Bitmap bitmap) {
//                        mPosterImageView.setVisibility(View.VISIBLE);
//
//                        if (mMovie.getColorScheme() == null) {
//                            new ColorCalculatorTask().executeOnExecutor(
//                                    AsyncTask.THREAD_POOL_EXECUTOR, bitmap);
//                        }
//                    }
//
//                    @Override
//                    public void onError() {
//                        mPosterImageView.setVisibility(View.GONE);
//                    }
//                });
//            }
//        }
//
//        public void loadBackdrop() {
//            if (!Objects.equal(mMovie.getBackdropUrl(), mBackdropPath)) {
//                mBackdropPath = mMovie.getBackdropUrl();
//                mFanartImageView.loadBackdropUrl(mMovie);
//            }
//        }
//
//        void reset() {
//            mPosterPath = null;
//            mBackdropPath = null;
//        }
//
//    }

    private enum DetailItemType {
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
            return Math.max(1, getCount());
        }

        @Override
        public int getItemViewType(int position) {
            return position;
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
                case SUMMARY:
                    bindSummaryView(view);
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

        private void bindSummaryView(final View view) {
            TextView summary = (TextView) view;
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

            RelatedMoviesAdapter adapter = new RelatedMoviesAdapter(getActivity().getLayoutInflater());

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    (MovieDetailCardLayout) view,
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

            if (adapter.getCount() > 0) {
                final int numItems = layout.getWidth() /
                        getResources().getDimensionPixelSize(R.dimen.movie_detail_multi_item_width);

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

            MovieCastAdapter adapter = new MovieCastAdapter(getActivity().getLayoutInflater());

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    (MovieDetailCardLayout) view,
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

            MovieTrailersAdapter adapter = new MovieTrailersAdapter(getActivity().getLayoutInflater());

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    (MovieDetailCardLayout) view,
                    seeMoreClickListener,
                    adapter);
        }

    }
}
