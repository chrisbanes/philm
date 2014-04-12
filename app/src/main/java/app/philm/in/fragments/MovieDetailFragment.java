package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseDetailFragment;
import app.philm.in.model.ColorScheme;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.model.PhilmTrailer;
import app.philm.in.util.ColorValueAnimator;
import app.philm.in.util.DominantColorCalculator;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.IntUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.BackdropImageView;
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.RatingBarLayout;

public class MovieDetailFragment extends BaseDetailFragment
        implements MovieController.MovieDetailUi, View.OnClickListener,
        AbsListView.OnScrollListener {

    private static final Date DATE = new Date();

    private static final float PARALLAX_FRICTION = 0.5f;

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

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

    @Inject ImageHelper mImageHelper;
    @Inject FlagUrlProvider mFlagUrlProvider;
    @Inject DateFormat mMediumDateFormatter;

    private PhilmMovie mMovie;

    private PhilmImageView mBigPosterImageView;
    private BackdropImageView mBackdropImageView;
    private int mBackdropOriginalHeight;

    private boolean mFadeActionBar;

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

        mFadeActionBar = getResources().getBoolean(R.bool.movie_detail_fade_action_bar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBackdropImageView = (BackdropImageView) view.findViewById(R.id.imageview_fanart);
        if (mBackdropImageView != null) {
            mBackdropOriginalHeight = mBackdropImageView.getLayoutParams().height;
        }

        mBigPosterImageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);

        getListView().setOnScrollListener(this);
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        setActionBarTitleEnabled(true);
        super.onPause();
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();

        if (mMovie != null && mMovie.getColorScheme() != null) {
            onColorSchemeChanged();
        }

        if (movie != null && hasCallbacks()) {
            getCallbacks().onTitleChanged();
        }
    }

    @Override
    public void setCheckinVisible(boolean visible) {
        getListAdapter().setCheckinButtonVisible(visible);
    }

    @Override
    public void setCancelCheckinVisible(boolean visible) {
        getListAdapter().setCancelCheckinButtonVisible(visible);
    }

    @Override
    public void setRateCircleEnabled(final boolean enabled) {
        getListAdapter().setRateCircleEnabled(enabled);
    }

    @Override
    public void setCollectionButtonEnabled(final boolean enabled) {
        getListAdapter().setCollectionButtonEnabled(enabled);
    }

    @Override
    public void setWatchlistButtonEnabled(final boolean enabled) {
        getListAdapter().setWatchlistButtonEnabled(enabled);
    }

    @Override
    public void setToggleWatchedButtonEnabled(final boolean enabled) {
        getListAdapter().setToggleWatchedButtonEnabled(enabled);
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
    protected DetailAdapter getListAdapter() {
        return (DetailAdapter) super.getListAdapter();
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
    public void populateInsets(Rect insets) {
        super.populateInsets(insets);

        if (mBackdropImageView != null) {
            final int targetBackdropHeight = mBackdropOriginalHeight + insets.top;
            if (mBackdropImageView.getLayoutParams().height != targetBackdropHeight) {
                mBackdropImageView.getLayoutParams().height = targetBackdropHeight;
                mBackdropImageView.requestLayout();
            }
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
            case R.id.btn_checkin:
                if (hasCallbacks()) {
                    getCallbacks().requestCheckin(mMovie);
                }
                break;
            case R.id.btn_cancel_checkin:
                if (hasCallbacks()) {
                    getCallbacks().requestCancelCurrentCheckin();
                }
                break;
            case R.id.rcv_rating:
                if (hasCallbacks()) {
                    getCallbacks().showRateMovie(mMovie);
                }
                break;
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
            final int y = absListView.getPaddingTop() - firstView.getTop();
            final float percent = y / (float) firstView.getHeight();

            if (mFadeActionBar) {
                setTopInsetAlpha(percent);
                setActionBarTitleEnabled(percent >= 0.8f);
            }

            if (mBackdropImageView != null) {
                mBackdropImageView.setVisibility(View.VISIBLE);
                mBackdropImageView.offsetBackdrop(Math.round(-y * PARALLAX_FRICTION));
            }
        } else {
            if (mFadeActionBar) {
                setTopInsetAlpha(1f);
            }

            if (mBackdropImageView != null) {
                mBackdropImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected DetailAdapter createListAdapter() {
        return new DetailAdapter();
    }

    void onColorSchemeChanged() {
        getListAdapter().onColorSchemeChanged();
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        final ArrayList<DetailItemType> items = new ArrayList<>();

        if (mBigPosterImageView == null && mBackdropImageView != null) {
            if (!TextUtils.isEmpty(mMovie.getBackdropUrl())) {
                items.add(DetailItemType.BACKDROP_SPACING);
                mBackdropImageView.setVisibility(View.VISIBLE);
                mBackdropImageView.loadBackdrop(mMovie);
            } else {
                mBackdropImageView.setVisibility(View.GONE);
            }
        }

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

        if (!PhilmCollections.isEmpty(mMovie.getCast())) {
            items.add(DetailItemType.CAST);
        }

        if (!PhilmCollections.isEmpty(mMovie.getCrew())) {
            items.add(DetailItemType.CREW);
        }

        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
            items.add(DetailItemType.RELATED);
        }

        if (mBigPosterImageView != null) {
            mBigPosterImageView.loadPoster(mMovie, mPosterListener);
        }

        getListAdapter().setItems(items);
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

    private enum DetailItemType implements DetailType {
        BACKDROP_SPACING(R.layout.item_movie_backdrop_spacing),
        TITLE(R.layout.item_movie_detail_title),
        BUTTONS(R.layout.item_movie_detail_buttons),
        DETAILS(R.layout.item_movie_detail_details),
        RATING(R.layout.item_movie_detail_rating),
        SUMMARY(R.layout.item_movie_detail_summary),
        TRAILERS(R.layout.item_movie_detail_trailers),
        RELATED(R.layout.item_movie_detail_generic_card),
        CAST(R.layout.item_movie_detail_generic_card),
        CREW(R.layout.item_movie_detail_generic_card);

        private final int mLayoutId;

        private DetailItemType(int layoutId) {
            mLayoutId = layoutId;
        }

        @Override
        public int getLayoutId() {
            return mLayoutId;
        }

        @Override
        public int getViewType() {
            switch (this) {
                case RELATED:
                case CAST:
                case CREW:
                    return RELATED.ordinal();
                default:
                    return ordinal();
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
                view = mInflater.inflate(getLayoutId(), viewGroup, false);
            }

            final PhilmMovie movie = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(movie.getTitle());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadPoster(movie);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(movie);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_grid_item_1line;
        }
    }

    private class MovieCastAdapter extends BaseMovieCastAdapter {
        MovieCastAdapter(LayoutInflater inflater) {
            super(inflater, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        PhilmMovieCredit cast = (PhilmMovieCredit) view.getTag();
                        if (cast != null && cast.getPerson() != null) {
                            getCallbacks().showPersonDetail(cast.getPerson());
                        }
                    }
                }
            });
        }

        @Override
        public int getCount() {
            return mMovie != null ? PhilmCollections.size(mMovie.getCast()) : 0;
        }

        @Override
        public PhilmMovieCredit getItem(int position) {
            return mMovie.getCast().get(position);
        }
    }

    private class MovieCrewAdapter extends BaseMovieCastAdapter {
        MovieCrewAdapter(LayoutInflater inflater) {
            super(inflater, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        PhilmMovieCredit cast = (PhilmMovieCredit) view.getTag();
                        if (cast != null && cast.getPerson() != null) {
                            getCallbacks().showPersonDetail(cast.getPerson());
                        }
                    }
                }
            });
        }

        @Override
        public int getCount() {
            return mMovie != null ? PhilmCollections.size(mMovie.getCrew()) : 0;
        }

        @Override
        public PhilmMovieCredit getItem(int position) {
            return mMovie.getCrew().get(position);
        }
    }

    private abstract class BaseMovieCastAdapter extends BaseAdapter {
        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        BaseMovieCastAdapter(LayoutInflater inflater, View.OnClickListener itemOnClickListener) {
            mInflater = inflater;
            mItemOnClickListener = itemOnClickListener;
        }

        @Override
        public abstract PhilmMovieCredit getItem(int position);

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(getLayoutId(), viewGroup, false);
            }

            final PhilmMovieCredit credit = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(credit.getPerson().getName());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadProfile(credit.getPerson());

            TextView subTitle = (TextView) view.findViewById(R.id.textview_subtitle);
            if (!TextUtils.isEmpty(credit.getJob())) {
                subTitle.setText(credit.getJob());
                subTitle.setVisibility(View.VISIBLE);
            } else {
                subTitle.setVisibility(View.GONE);
            }

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(credit);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_grid_item_2line;
        }
    }

    private class MovieTrailersAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final View.OnClickListener mOnClickListener;

        MovieTrailersAdapter(LayoutInflater inflater) {
            mInflater = inflater;

            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PhilmTrailer trailer = (PhilmTrailer) view.getTag();
                    if (trailer != null && hasCallbacks()) {
                        getCallbacks().playTrailer(trailer);
                    }
                }
            };
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

                    final PhilmImageView imageView = (PhilmImageView)
                            view.findViewById(R.id.imageview_thumbnail);
                    imageView.loadTrailer(trailer);

                    final TextView title = (TextView) view.findViewById(R.id.textview_title);
                    title.setText(trailer.getName());
            }

            view.setOnClickListener(mOnClickListener);
            view.setTag(trailer);

            return view;
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
        protected void onPostExecute(DominantColorCalculator colorCalculator) {
            if (colorCalculator != null) {
                final ColorScheme scheme = colorCalculator.getColorScheme();
                if (scheme != null && mMovie != null) {
                    mMovie.setColorScheme(scheme);

                    if (getActivity() != null) {
                        onColorSchemeChanged();
                    }
                }
            }
        }
    }

    private class DetailAdapter extends BaseDetailAdapter<DetailItemType> {

        private boolean mRatingCircleEnabled;
        private boolean mCollectionButtonEnabled;
        private boolean mWatchlistButtonEnabled;
        private boolean mWatchedButtonEnabled;
        private boolean mCheckinButtonVisible;
        private boolean mCancelCheckinButtonVisible;

        @Override
        public int getViewTypeCount() {
            return DetailItemType.values().length;
        }

        public void setCheckinButtonVisible(boolean visible) {
            mCheckinButtonVisible = visible;
            rebindView(DetailItemType.BUTTONS);
        }

        public void setCancelCheckinButtonVisible(boolean visible) {
            mCancelCheckinButtonVisible = visible;
            rebindView(DetailItemType.BUTTONS);
        }

        public void setRateCircleEnabled(boolean enabled) {
            mRatingCircleEnabled = enabled;
            rebindView(DetailItemType.RATING);
        }

        public void setCollectionButtonEnabled(boolean enabled) {
            mCollectionButtonEnabled = enabled;
            rebindView(DetailItemType.BUTTONS);
        }

        public void setWatchlistButtonEnabled(boolean enabled) {
            mWatchlistButtonEnabled = enabled;
            rebindView(DetailItemType.BUTTONS);
        }

        public void setToggleWatchedButtonEnabled(boolean enabled) {
            mWatchedButtonEnabled = enabled;
            rebindView(DetailItemType.BUTTONS);
        }

        public void onColorSchemeChanged() {
            rebindView(DetailItemType.TITLE);
            rebindView(DetailItemType.RATING);
        }

        private void bindButtons(final View view) {
            CheckableImageButton seenButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_seen);
            seenButton.setEnabled(mWatchedButtonEnabled);
            seenButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(seenButton);
            updateButtonState(seenButton, mMovie.isWatched(), R.string.action_mark_seen,
                    R.string.action_mark_unseen);

            CheckableImageButton watchlistButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_watchlist);
            watchlistButton.setEnabled(mWatchlistButtonEnabled);
            watchlistButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(watchlistButton);
            updateButtonState(watchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                    R.string.action_remove_watchlist);

            CheckableImageButton collectionButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_collection);
            collectionButton.setEnabled(mCollectionButtonEnabled);
            collectionButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(collectionButton);
            updateButtonState(collectionButton, mMovie.inCollection(),
                    R.string.action_add_collection,
                    R.string.action_remove_collection);

            ImageButton checkinButton = (ImageButton) view.findViewById(R.id.btn_checkin);
            checkinButton.setOnClickListener(MovieDetailFragment.this);
            checkinButton.setVisibility(mCheckinButtonVisible ? View.VISIBLE : View.GONE);
            CheatSheet.setup(checkinButton);

            ImageButton cancelCheckinButton = (ImageButton) view
                    .findViewById(R.id.btn_cancel_checkin);
            cancelCheckinButton.setOnClickListener(MovieDetailFragment.this);
            cancelCheckinButton.setVisibility(mCancelCheckinButtonVisible
                    ? View.VISIBLE : View.GONE);
            CheatSheet.setup(cancelCheckinButton);
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

        private void bindCrew(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindCrew");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showCrewList(mMovie);
                    }
                }
            };

            MovieCrewAdapter adapter = new MovieCrewAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.crew_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
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

                final String countryCode = mMovie.getReleasedCountryCode();
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
            ratingBarLayout.setRatingCircleEnabled(mRatingCircleEnabled);

            if (mMovie.getUserRatingAdvanced() != PhilmMovie.NOT_SET) {
                ratingBarLayout.showUserRating(mMovie.getUserRatingAdvanced());
            } else {
                ratingBarLayout.showRatePrompt();
            }
            ratingBarLayout.setRatingGlobalPercentage(mMovie.getAverageRatingPercent());
            ratingBarLayout.setRatingGlobalVotes(mMovie.getAverageRatingVotes());
            ratingBarLayout.setRatingCircleClickListener(MovieDetailFragment.this);

            if (mMovie.getColorScheme() != null) {
                ratingBarLayout.setColorScheme(mMovie.getColorScheme());
            }
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

        private void bindSummary(final View view) {
            TextView summary = (TextView) view.findViewById(R.id.textview_summary);
            summary.setText(mMovie.getOverview());
        }

        private void bindTitle(final View view) {
            final TextView titleTextView = (TextView) view.findViewById(R.id.textview_title);
            titleTextView.setText(getString(R.string.movie_title_year,
                    mMovie.getTitle(), mMovie.getYear()));

            final TextView taglineTextView = (TextView) view.findViewById(R.id.textview_tagline);
            taglineTextView.setText(mMovie.getTagline());

            PhilmImageView posterImageView = (PhilmImageView)
                    view.findViewById(R.id.imageview_poster);

            if (mBigPosterImageView == null) {
                posterImageView.setVisibility(View.VISIBLE);
                posterImageView.loadPoster(mMovie, mPosterListener);
            } else {
                // Hide small poster if there's a big poster imageview
                posterImageView.setVisibility(View.GONE);
            }

            final ColorScheme scheme = mMovie.getColorScheme();
            if (scheme != null) {

                final int bgColor = (view.getBackground() instanceof ColorDrawable)
                        ? ((ColorDrawable) view.getBackground()).getColor()
                        : Color.WHITE;
                final int titleColor = titleTextView.getCurrentTextColor();
                final int taglineColor = taglineTextView.getCurrentTextColor();

                ColorValueAnimator.start(view,
                        IntUtils.toArray(bgColor, titleColor, taglineColor),
                        IntUtils.toArray(scheme.primaryAccent, scheme.primaryText, scheme.secondaryText),
                        175,
                        new ColorValueAnimator.OnColorSetListener() {
                            @Override
                            public void onUpdateColor(int[] newColors) {
                                view.setBackgroundColor(newColors[0]);
                                titleTextView.setTextColor(newColors[1]);
                                taglineTextView.setTextColor(newColors[2]);
                            }
                        }
                );
            }
        }

        private void bindTrailers(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindTrailers");
            }

            MovieTrailersAdapter adapter = new MovieTrailersAdapter(
                    LayoutInflater.from(getActivity()));

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    (MovieDetailCardLayout) view,
                    null,
                    adapter);
        }

        @Override
        protected void bindView(final DetailItemType item, final View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindView. Item: " + item.name());
            }

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
                case CREW:
                    bindCrew(view);
                    break;
            }

            view.setTag(item);
        }

        private void loadFlagImage(final String countryCode, final MovieDetailInfoLayout layout) {
            final String flagUrl = mFlagUrlProvider.getCountryFlagUrl(countryCode);
            final int width = getResources()
                    .getDimensionPixelSize(R.dimen.movie_detail_flag_width);
            final int height = getResources()
                    .getDimensionPixelSize(R.dimen.movie_detail_flag_height);

            final String url = ImageHelper.getResizedUrl(flagUrl, width, height);

            Picasso.with(getActivity())
                    .load(url)
                    .into(layout);
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
    }
}
