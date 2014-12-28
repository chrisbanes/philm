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

import com.squareup.picasso.Picasso;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import app.philm.in.drawable.TintingBitmapDrawable;
import app.philm.in.fragments.base.BaseDetailFragment;
import app.philm.in.model.ColorScheme;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.util.ActivityTransitions;
import app.philm.in.model.PhilmMovieVideo;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.BackdropImageView;
import app.philm.in.view.CollapsingTitleLayout;
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
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private final PhilmImageView.Listener mPosterListener = new PhilmImageView.Listener() {
        @Override
        public void onSuccess(PhilmImageView imageView, Bitmap bitmap) {
            imageView.setVisibility(View.VISIBLE);

            if (getColorScheme() == null) {
                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        Palette.Swatch primary = palette.getVibrantSwatch();
                        Palette.Swatch secondary = palette.getDarkVibrantSwatch();
                        Palette.Swatch tertiary = palette.getLightVibrantSwatch();

                        if (primary == null) {
                            primary = palette.getMutedSwatch();
                        }
                        if (secondary == null) {
                            secondary = palette.getDarkMutedSwatch();
                        }
                        if (tertiary == null) {
                            tertiary = palette.getLightMutedSwatch();
                        }

                        if (hasCallbacks() && primary != null && secondary != null &&
                                tertiary != null) {
                            final ColorScheme scheme = new ColorScheme(
                                    primary.getRgb(),
                                    secondary.getRgb(),
                                    tertiary.getRgb(),
                                    primary.getTitleTextColor(),
                                    primary.getBodyTextColor());

                            if (mBackdropImageView != null) {
                                mBackdropImageView.setScrimColor(scheme.secondaryAccent);
                            }

                            getCallbacks().updateColorScheme(scheme);
                        }
                    }
                });
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

    private CollapsingTitleLayout mCollapsingTitleLayout;
    private BackdropImageView mBackdropImageView;

    private boolean mFadeActionBar;

    private final ArrayList<DetailItemType> mItems = new ArrayList<>();

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

        mCollapsingTitleLayout = (CollapsingTitleLayout) view.findViewById(R.id.backdrop_toolbar);

        mBackdropImageView = (BackdropImageView) view.findViewById(R.id.imageview_fanart);
        if (mBackdropImageView != null) {
            mBackdropImageView.setOnClickListener(this);
        }

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
        if (hasCallbacks()) {
            getCallbacks().setHeaderScrollValue(1f);
        }

        super.onPause();
    }

    @Override
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();
    }

    @Override
    public void setButtonsEnabled(boolean watched, boolean collection, boolean watchlist,
            boolean checkin, boolean cancelCheckin) {
        getListAdapter().setButtonsEnabled(watched, collection, watchlist, checkin, cancelCheckin);
    }

    @Override
    public void setRateCircleEnabled(final boolean enabled) {
        getListAdapter().setRateCircleEnabled(enabled);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.MOVIE_DETAIL;
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
    public boolean isModal() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemClick. Pos: " + position);

        if (getListAdapter().getItem(position) == DetailItemType.BACKDROP_SPACING) {
            if (hasCallbacks()) {
                getCallbacks().showMovieImages(mMovie);
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
            case R.id.imageview_fanart:
                if (hasCallbacks()) {
                    getCallbacks().showMovieImages(mMovie);
                }
                break;
        }
    }

    @Override
    protected void onBigPosterClicked() {
        if (hasCallbacks()) {
            getCallbacks().showMovieImages(mMovie);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        // NO-OP
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        if (absListView.getItemAtPosition(0) == DetailItemType.BACKDROP_SPACING) {
            final Toolbar toolbar = getToolbar();

            if (visibleItemCount > 0 && firstVisibleItem == 0) {
                final View firstView = absListView.getChildAt(0);

                mCollapsingTitleLayout.setVisibility(View.VISIBLE);

                final int toolbarHeight = toolbar.getHeight();
                final int y = -firstView.getTop();
                final float percent = y / (float) (firstView.getHeight() - toolbar.getHeight());

                if (firstView.getBottom() > toolbarHeight) {
                    mCollapsingTitleLayout.setTranslationY(0);
                    setBackdropOffset(percent);
                } else {
                    mCollapsingTitleLayout.setTranslationY(firstView.getBottom() - toolbarHeight);
                    setBackdropOffset(1f);
                }

                if (mFadeActionBar && hasCallbacks()) {
                    getCallbacks().setHeaderScrollValue(percent);
                }
            } else {
                setBackdropOffset(1f);
                mCollapsingTitleLayout.setVisibility(View.GONE);
            }
            return;
        }

        if (mFadeActionBar && hasCallbacks()) {
            getCallbacks().setHeaderScrollValue(1f);
        }
    }

    private void setBackdropOffset(float offset) {
        if (mCollapsingTitleLayout != null) {
            mCollapsingTitleLayout.setScrollOffset(offset);
        }
        if (mBackdropImageView != null) {
            mBackdropImageView.setScrollOffset(
                    (int) ((getToolbar().getHeight() - mBackdropImageView.getHeight()) * offset));
        }
    }

    @Override
    protected DetailAdapter createListAdapter() {
        return new DetailAdapter();
    }

    @Override
    protected void onColorSchemeChanged(ColorScheme colorScheme) {
        DetailAdapter adapter = getListAdapter();
        if (adapter != null) {
            adapter.onColorSchemeChanged();
        }
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        mItems.clear();

        if (!hasBigPosterView() && mBackdropImageView != null) {
            mItems.add(DetailItemType.BACKDROP_SPACING);
            if (mMovie.hasBackdropUrl()) {
                mBackdropImageView.loadBackdrop(mMovie);
            }
        }

        if (mCollapsingTitleLayout != null) {
            mCollapsingTitleLayout.setTitle(mMovie.getTitle());
        }

        mItems.add(DetailItemType.TITLE);
        mItems.add(DetailItemType.BUTTONS);

        if (!TextUtils.isEmpty(mMovie.getOverview())) {
            mItems.add(DetailItemType.SUMMARY);
        }

        mItems.add(DetailItemType.RATING);
        mItems.add(DetailItemType.DETAILS);

        if (!PhilmCollections.isEmpty(mMovie.getTrailers())) {
            mItems.add(DetailItemType.TRAILERS);
        }

        if (!PhilmCollections.isEmpty(mMovie.getCast())) {
            mItems.add(DetailItemType.CAST);
        }

        if (!PhilmCollections.isEmpty(mMovie.getCrew())) {
            mItems.add(DetailItemType.CREW);
        }

        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
            mItems.add(DetailItemType.RELATED);
        }

        if (hasBigPosterView()) {
            getBigPosterView().loadPoster(mMovie, mPosterListener);
        }

        getListAdapter().setItems(mItems);
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

        @Override
        public boolean isEnabled() {
            return this == BACKDROP_SPACING;
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
                        getCallbacks().showMovieDetail((PhilmMovie) view.getTag(),
                                ActivityTransitions.scaleUpAnimation(view));
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
            if (movie.getYear() > 0) {
                title.setText(getString(R.string.movie_title_year,
                        movie.getTitle(), movie.getYear()));
            } else {
                title.setText(movie.getTitle());
            }

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.setAvatarMode(false);
            imageView.loadPoster(movie);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(movie);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_list_1line;
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
                            getCallbacks().showPersonDetail(cast.getPerson(),
                                    ActivityTransitions.scaleUpAnimation(view));
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
                            getCallbacks().showPersonDetail(cast.getPerson(),
                                    ActivityTransitions.scaleUpAnimation(view));
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
            imageView.setAvatarMode(true);
            imageView.loadProfile(credit.getPerson());

            TextView subTitle = (TextView) view.findViewById(R.id.textview_subtitle_1);
            if (subTitle != null) {
                if (!TextUtils.isEmpty(credit.getJob())) {
                    subTitle.setText(credit.getJob());
                    subTitle.setVisibility(View.VISIBLE);
                } else {
                    subTitle.setVisibility(View.GONE);
                }
            }

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(credit);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_list_2line;
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
                    PhilmMovieVideo trailer = (PhilmMovieVideo) view.getTag();
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
        public PhilmMovieVideo getItem(int position) {
            return mMovie.getTrailers().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.item_movie_trailer, viewGroup, false);
            }

            final PhilmMovieVideo trailer = getItem(position);

            final PhilmImageView imageView = (PhilmImageView)
                    view.findViewById(R.id.imageview_thumbnail);
            imageView.loadTrailer(trailer);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(trailer.getName());

            view.setOnClickListener(mOnClickListener);
            view.setTag(trailer);

            return view;
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

        void setButtonsEnabled(boolean watched, boolean collection, boolean watchlist,
                boolean checkin, boolean cancelCheckin) {
            mWatchedButtonEnabled = watched;
            mCollectionButtonEnabled = collection;
            mWatchlistButtonEnabled = watchlist;
            mCheckinButtonVisible = checkin;
            mCancelCheckinButtonVisible = cancelCheckin;
            // No need to rebind here as setMovie will be called after
        }

        void setRateCircleEnabled(boolean enabled) {
            mRatingCircleEnabled = enabled;
            // No need to rebind here as setMovie will be called after
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
            if (seenButton.getDrawable() == null) {
                seenButton.setImageDrawable(
                        TintingBitmapDrawable.createFromStateListResource(getResources(),
                                R.drawable.ic_btn_seen, R.color.default_button));
            }

            CheckableImageButton watchlistButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_watchlist);
            watchlistButton.setEnabled(mWatchlistButtonEnabled);
            watchlistButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(watchlistButton);
            updateButtonState(watchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                    R.string.action_remove_watchlist);
            if (watchlistButton.getDrawable() == null) {
                watchlistButton.setImageDrawable(
                        TintingBitmapDrawable.createFromStateListResource(getResources(),
                                R.drawable.ic_btn_watchlist, R.color.default_button));
            }

            CheckableImageButton collectionButton =
                    (CheckableImageButton) view.findViewById(R.id.btn_collection);
            collectionButton.setEnabled(mCollectionButtonEnabled);
            collectionButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(collectionButton);
            updateButtonState(collectionButton, mMovie.inCollection(),
                    R.string.action_add_collection,
                    R.string.action_remove_collection);
            if (collectionButton.getDrawable() == null) {
                collectionButton.setImageDrawable(
                        TintingBitmapDrawable.createFromStateListResource(getResources(),
                                R.drawable.ic_btn_collection, R.color.default_button));
            }

            ImageButton checkinButton = (ImageButton) view.findViewById(R.id.btn_checkin);
            checkinButton.setOnClickListener(MovieDetailFragment.this);
            checkinButton.setVisibility(mCheckinButtonVisible ? View.VISIBLE : View.GONE);
            CheatSheet.setup(checkinButton);
            if (mCheckinButtonVisible && checkinButton.getDrawable() == null) {
                checkinButton.setImageDrawable(
                        TintingBitmapDrawable.createFromStateListResource(getResources(),
                                R.drawable.ic_btn_checkin, R.color.default_button));
            }

            ImageButton cancelCheckinButton = (ImageButton) view
                    .findViewById(R.id.btn_cancel_checkin);
            cancelCheckinButton.setOnClickListener(MovieDetailFragment.this);
            cancelCheckinButton.setVisibility(mCancelCheckinButtonVisible
                    ? View.VISIBLE : View.GONE);
            CheatSheet.setup(cancelCheckinButton);
            if (mCancelCheckinButtonVisible && cancelCheckinButton.getDrawable() == null) {
                cancelCheckinButton.setImageDrawable(
                        TintingBitmapDrawable.createFromColorResource(getResources(),
                                R.drawable.ic_btn_checkin, android.R.color.holo_red_dark));
            }
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
            MovieDetailInfoLayout budgetInfoLayout = (MovieDetailInfoLayout) view
                    .findViewById(R.id.layout_info_budget);
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

            if (mMovie.getBudget() > 0) {
                budgetInfoLayout.setContentText(
                        getString(R.string.movie_details_budget_content, mMovie.getBudget()));
                budgetInfoLayout.setVisibility(View.VISIBLE);
            } else {
                budgetInfoLayout.setVisibility(View.GONE);
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

            final ColorScheme scheme = getColorScheme();
            if (scheme != null) {
                ratingBarLayout.setColorScheme(scheme);
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
//            final TextView titleTextView = (TextView) view.findViewById(R.id.textview_title);
//            titleTextView.setText(getString(R.string.movie_title_year,
//                    mMovie.getTitle(), mMovie.getYear()));

            final TextView taglineTextView = (TextView) view.findViewById(R.id.textview_tagline);
            taglineTextView.setText(mMovie.getTagline());

            PhilmImageView posterImageView = (PhilmImageView)
                    view.findViewById(R.id.imageview_poster);

            if (hasBigPosterView()) {
                // Hide small poster if there's a big poster imageview
                posterImageView.setVisibility(View.GONE);
            } else {
                posterImageView.setVisibility(View.VISIBLE);
                posterImageView.loadPoster(mMovie, mPosterListener);
            }

            final ColorScheme scheme = getColorScheme();
            if (scheme != null) {
                view.setBackgroundColor(scheme.primaryAccent);
                //titleTextView.setTextColor(scheme.primaryText);
                taglineTextView.setTextColor(scheme.primaryText);
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

        private void bindBackdropSpacing(View view) {
            final int backdropHeight = getResources()
                    .getDimensionPixelSize(R.dimen.movie_detail_fanart_height);
            view.getLayoutParams().height = backdropHeight - getListView().getPaddingTop();
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
                case BACKDROP_SPACING:
                    bindBackdropSpacing(view);
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

    @Override
    protected void setSupportActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar, false);
    }
}
