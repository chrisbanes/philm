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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.genius.groupie.Group;
import com.genius.groupie.GroupAdapter;
import com.genius.groupie.Item;
import com.genius.groupie.NestedGroup;
import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.databinding.ItemMovieDetailButtonsBinding;
import app.philm.in.databinding.ItemMovieDetailDetailsLineBinding;
import app.philm.in.databinding.ItemMovieDetailRatingBinding;
import app.philm.in.databinding.ItemMovieDetailSummaryBinding;
import app.philm.in.databinding.ItemMovieDetailTitleBinding;
import app.philm.in.drawable.DrawableTintUtils;
import app.philm.in.fragments.base.BasePhilmMovieFragment;
import app.philm.in.model.ColorScheme;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.model.PhilmMovieVideo;
import app.philm.in.util.ActivityTransitions;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.CheatSheet;
import app.philm.in.view.CheckableImageButton;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.MovieDetailInfoLayout;
import app.philm.in.view.PhilmImageView;

public class MovieDetailFragment extends BasePhilmMovieFragment
        implements MovieController.MovieDetailUi, View.OnClickListener {

    private static final Date DATE = new Date();
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private final PhilmImageView.Listener mPosterListener = new PhilmImageView.Listener() {
        @Override
        public void onSuccess(PhilmImageView imageView, Bitmap bitmap) {
            imageView.setVisibility(View.VISIBLE);

            if (getColorScheme() == null) {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
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

                            if (mCollapsingToolbarLayout != null) {
                                mCollapsingToolbarLayout
                                        .setContentScrimColor(scheme.secondaryAccent);
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

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private PhilmImageView mBackdropImageView;

    private RecyclerView mRecyclerView;

    private GroupAdapter mAdapter;

    private boolean mRatingCircleEnabled;
    private boolean mCollectionButtonEnabled;
    private boolean mWatchlistButtonEnabled;
    private boolean mWatchedButtonEnabled;
    private boolean mCheckinButtonVisible;
    private boolean mCancelCheckinButtonVisible;

    public static MovieDetailFragment create(@NonNull String movieId) {
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
        return inflater.inflate(R.layout.fragment_movie_detail_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout)
                view.findViewById(R.id.collapsing_toolbar_layout);

        mBackdropImageView = (PhilmImageView) view.findViewById(R.id.imageview_fanart);
        if (mBackdropImageView != null) {
            mBackdropImageView.setOnClickListener(this);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
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
    public void setMovie(PhilmMovie movie) {
        mMovie = movie;
        populateUi();
    }

    @Override
    public void setButtonsEnabled(boolean watched, boolean collection, boolean watchlist,
            boolean checkin, boolean cancelCheckin) {
        // TODO
    }

    @Override
    public void setRateCircleEnabled(final boolean enabled) {
        // TODO
        //getListAdapter().setRateCircleEnabled(enabled);
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
    public boolean isModal() {
        return false;
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
    protected void onColorSchemeChanged(ColorScheme colorScheme) {
        // TODO
//        DetailAdapter adapter = getListAdapter();
//        if (adapter != null) {
//            adapter.onColorSchemeChanged();
//        }
    }

    private void populateUi() {
        if (mMovie == null) {
            return;
        }

        if (mBackdropImageView != null) {
            if (mMovie.hasBackdropUrl()) {
                mBackdropImageView.loadBackdrop(mMovie);
            }
        }

        if (mCollapsingToolbarLayout != null) {
            mCollapsingToolbarLayout.setTitle(mMovie.getTitle());
        }

        mAdapter = new GroupAdapter();

        mAdapter.add(new TitleItem());
        mAdapter.add(new ButtonsItem());

        if (!TextUtils.isEmpty(mMovie.getOverview())) {
            mAdapter.add(new SummaryItem());
        }

        mAdapter.add(new RatingItem());
        mAdapter.add(new DetailsGroup());

//
//        if (!PhilmCollections.isEmpty(mMovie.getTrailers())) {
//            mItems.add(DetailItemType.TRAILERS);
//        }
//
//        if (!PhilmCollections.isEmpty(mMovie.getCast())) {
//            mItems.add(DetailItemType.CAST);
//        }
//
//        if (!PhilmCollections.isEmpty(mMovie.getCrew())) {
//            mItems.add(DetailItemType.CREW);
//        }
//
//        if (!PhilmCollections.isEmpty(mMovie.getRelated())) {
//            mItems.add(DetailItemType.RELATED);
//        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                mAdapter.getSpanCount());
        layoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mAdapter);
    }

    class TitleItem extends Item<ItemMovieDetailTitleBinding> {
        @Override
        public int getLayout() {
            return R.layout.item_movie_detail_title;
        }

        @Override
        public void bind(ItemMovieDetailTitleBinding viewBinding, int position) {
            viewBinding.textviewTagline.setText(mMovie.getTagline());
            viewBinding.imageviewPoster.loadPoster(mMovie, mPosterListener);

            final ColorScheme scheme = getColorScheme();
            if (scheme != null) {
                viewBinding.getRoot().setBackgroundColor(scheme.primaryAccent);
                viewBinding.textviewTagline.setTextColor(scheme.primaryText);
            }
        }
    }

    class ButtonsItem extends Item<ItemMovieDetailButtonsBinding> {
        @Override
        public int getLayout() {
            return R.layout.item_movie_detail_buttons;
        }

        @Override
        public void bind(ItemMovieDetailButtonsBinding viewBinding, int position) {
            CheckableImageButton seenButton = viewBinding.btnSeen;
            seenButton.setEnabled(mWatchedButtonEnabled);
            seenButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(seenButton);
            updateButtonState(seenButton, mMovie.isWatched(), R.string.action_mark_seen,
                    R.string.action_mark_unseen);
            if (seenButton.getDrawable() == null) {
                seenButton.setImageDrawable(
                        DrawableTintUtils.createFromColorRes(getContext(),
                                R.drawable.ic_btn_seen, R.color.default_button));
            }

            CheckableImageButton watchlistButton = viewBinding.btnWatchlist;
            watchlistButton.setEnabled(mWatchlistButtonEnabled);
            watchlistButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(watchlistButton);
            updateButtonState(watchlistButton, mMovie.inWatchlist(), R.string.action_add_watchlist,
                    R.string.action_remove_watchlist);
            if (watchlistButton.getDrawable() == null) {
                watchlistButton.setImageDrawable(
                        DrawableTintUtils.createFromColorRes(getContext(),
                                R.drawable.ic_btn_watchlist, R.color.default_button));
            }

            CheckableImageButton collectionButton = viewBinding.btnCollection;
            collectionButton.setEnabled(mCollectionButtonEnabled);
            collectionButton.setOnClickListener(MovieDetailFragment.this);
            CheatSheet.setup(collectionButton);
            updateButtonState(collectionButton, mMovie.inCollection(),
                    R.string.action_add_collection,
                    R.string.action_remove_collection);
            if (collectionButton.getDrawable() == null) {
                collectionButton.setImageDrawable(
                        DrawableTintUtils.createFromColorRes(getContext(),
                                R.drawable.ic_btn_collection, R.color.default_button));
            }

            ImageButton checkinButton = viewBinding.btnCheckin;
            checkinButton.setOnClickListener(MovieDetailFragment.this);
            checkinButton.setVisibility(mCheckinButtonVisible ? View.VISIBLE : View.GONE);
            CheatSheet.setup(checkinButton);
            if (mCheckinButtonVisible && checkinButton.getDrawable() == null) {
                checkinButton.setImageDrawable(
                        DrawableTintUtils.createFromColorRes(getContext(),
                                R.drawable.ic_btn_checkin, R.color.default_button));
            }

            ImageButton cancelCheckinButton = viewBinding.btnCancelCheckin;
            cancelCheckinButton.setOnClickListener(MovieDetailFragment.this);
            cancelCheckinButton.setVisibility(mCancelCheckinButtonVisible
                    ? View.VISIBLE : View.GONE);
            CheatSheet.setup(cancelCheckinButton);
            if (mCancelCheckinButtonVisible && cancelCheckinButton.getDrawable() == null) {
                cancelCheckinButton.setImageDrawable(
                        DrawableTintUtils.createFromColorRes(getContext(),
                                R.drawable.ic_btn_checkin, android.R.color.holo_red_dark));
            }
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

    class SummaryItem extends Item<ItemMovieDetailSummaryBinding> {
        @Override
        public int getLayout() {
            return R.layout.item_movie_detail_summary;
        }

        @Override
        public void bind(ItemMovieDetailSummaryBinding viewBinding, int position) {
            viewBinding.textviewSummary.setText(mMovie.getOverview());
        }
    }

    class RatingItem extends Item<ItemMovieDetailRatingBinding> {
        @Override
        public int getLayout() {
            return R.layout.item_movie_detail_rating;
        }

        @Override
        public void bind(ItemMovieDetailRatingBinding viewBinding, int position) {
            viewBinding.ratingBarLayout.setRatingCircleEnabled(mRatingCircleEnabled);

            if (mMovie.getUserRatingAdvanced() != PhilmMovie.NOT_SET) {
                viewBinding.ratingBarLayout.showUserRating(mMovie.getUserRatingAdvanced());
            } else {
                viewBinding.ratingBarLayout.showRatePrompt();
            }
            viewBinding.ratingBarLayout.setRatingGlobalPercentage(mMovie.getAverageRatingPercent());
            viewBinding.ratingBarLayout.setRatingGlobalVotes(mMovie.getAverageRatingVotes());
            viewBinding.ratingBarLayout.setRatingCircleClickListener(MovieDetailFragment.this);

            final ColorScheme scheme = getColorScheme();
            if (scheme != null) {
                viewBinding.ratingBarLayout.setColorScheme(scheme);
            }
        }
    }

    class DetailsGroup extends NestedGroup {
        private final ArrayList<Group> mItems = new ArrayList<>();

        DetailsGroup() {
            if (mMovie.getRuntime() > 0) {
                mItems.add(new RuntimeDetailsItem());
            }
            if (!TextUtils.isEmpty(mMovie.getCertification())) {
                mItems.add(new CertificationDetailsItem());
            }
            if (!TextUtils.isEmpty(mMovie.getGenres())) {
                mItems.add(new GenreDetailsItem());
            }
            if (mMovie.getReleasedTime() > 0) {
                mItems.add(new ReleasedDetailsItem());
            }
            if (mMovie.getBudget() > 0) {
                mItems.add(new BudgetDetailsItem());
            }
            if (!TextUtils.isEmpty(mMovie.getMainLanguageTitle())) {
                mItems.add(new MainLanguageDetailsItem());
            }
        }

        @Override
        public Group getGroup(int position) {
            return mItems.get(position);
        }

        @Override
        public int getGroupCount() {
            return mItems.size();
        }

        @Override
        public int getPosition(Group group) {
            return mItems.indexOf(group);
        }
    }

    private abstract class SingleDetailsItem extends Item<ItemMovieDetailDetailsLineBinding> {
        @Override
        public int getLayout() {
            return R.layout.item_movie_detail_details_line;
        }

        @Override
        public void bind(ItemMovieDetailDetailsLineBinding viewBinding, int position) {
            TextView left = (TextView) viewBinding.getRoot().findViewById(android.R.id.text1);
            left.setText(getTitleString());

            TextView right = (TextView) viewBinding.getRoot().findViewById(android.R.id.text2);
            right.setText(getContentString());
        }

        @StringRes
        protected abstract int getTitleString();

        protected abstract String getContentString();
    }

    class RuntimeDetailsItem extends SingleDetailsItem {
        @Override
        protected int getTitleString() {
            return R.string.movie_details_runtime;
        }

        @Override
        protected String getContentString() {
            return getString(R.string.movie_details_runtime_content, mMovie.getRuntime());
        }
    }

    class CertificationDetailsItem extends SingleDetailsItem {
        @Override
        protected int getTitleString() {
            return R.string.movie_details_certification;
        }

        @Override
        protected String getContentString() {
            return mMovie.getCertification();
        }
    }

    class GenreDetailsItem extends SingleDetailsItem {
        @Override
        protected int getTitleString() {
            return R.string.movie_details_genres;
        }

        @Override
        protected String getContentString() {
            return mMovie.getGenres();
        }
    }

    class ReleasedDetailsItem extends SingleDetailsItem {

        @Override
        public void bind(ItemMovieDetailDetailsLineBinding viewBinding, int position) {
            super.bind(viewBinding, position);

//            final String countryCode = mMovie.getReleasedCountryCode();
//            if (!TextUtils.isEmpty(countryCode)) {
//                loadFlagImage(countryCode, viewBinding.layoutInfoReleased);
//            }
        }

        @Override
        protected int getTitleString() {
            return R.string.movie_details_released;
        }

        @Override
        protected String getContentString() {
            DATE.setTime(mMovie.getReleasedTime());
            return mMediumDateFormatter.format(DATE);
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
    }

    class BudgetDetailsItem extends SingleDetailsItem {
        @Override
        protected int getTitleString() {
            return R.string.movie_details_budget;
        }

        @Override
        protected String getContentString() {
            return getString(R.string.movie_details_budget_content, mMovie.getBudget());
        }
    }

    class MainLanguageDetailsItem extends SingleDetailsItem {
        @Override
        protected int getTitleString() {
            return R.string.movie_details_language;
        }

        @Override
        protected String getContentString() {
            return mMovie.getMainLanguageTitle();
        }
    }

    private enum DetailItemType {
        TRAILERS(R.layout.item_movie_detail_trailers),
        RELATED(R.layout.item_movie_detail_generic_card),
        CAST(R.layout.item_movie_detail_generic_card),
        CREW(R.layout.item_movie_detail_generic_card);

        private final int mLayoutId;

        DetailItemType(int layoutId) {
            mLayoutId = layoutId;
        }

        public int getLayoutId() {
            return mLayoutId;
        }

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

    private class DetailAdapter extends BaseAdapter {
        private RelatedMoviesAdapter mRelatedMoviesAdapter;
        private MovieCastAdapter mMovieCastAdapter;
        private MovieCrewAdapter mMovieCrewAdapter;
        private MovieTrailersAdapter mMovieTrailersAdapter;

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return DetailItemType.values().length;
        }

        void setRateCircleEnabled(boolean enabled) {
            mRatingCircleEnabled = enabled;
            // No need to rebind here as setMovie will be called after
        }

        public void onColorSchemeChanged() {
            //rebindView(DetailItemType.TITLE);
            //rebindView(DetailItemType.RATING);
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

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.cast_movies);

//            populateDetailGrid(
//                    (ViewGroup) view.findViewById(R.id.card_content),
//                    cardLayout,
//                    seeMoreClickListener,
//                    getMovieCastAdapter());
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

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.crew_movies);

//            populateDetailGrid(
//                    (ViewGroup) view.findViewById(R.id.card_content),
//                    cardLayout,
//                    seeMoreClickListener,
//                    getMovieCrewAdapter());
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

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.related_movies);

//            populateDetailGrid(
//                    (ViewGroup) view.findViewById(R.id.card_content),
//                    cardLayout,
//                    seeMoreClickListener,
//                    getRelatedMoviesAdapter());
        }

        private void bindTrailers(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindTrailers");
            }

//            populateDetailGrid(
//                    (ViewGroup) view.findViewById(R.id.card_content),
//                    (MovieDetailCardLayout) view,
//                    null,
//                    getMovieTrailersAdapter());
        }


        protected void bindView(final DetailItemType item, final View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindView. Item: " + item.name());
            }

            switch (item) {
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

        private RelatedMoviesAdapter getRelatedMoviesAdapter() {
            if (mRelatedMoviesAdapter == null) {
                mRelatedMoviesAdapter = new RelatedMoviesAdapter(LayoutInflater.from(getActivity()));
            }
            return mRelatedMoviesAdapter;
        }

        private MovieCastAdapter getMovieCastAdapter() {
            if (mMovieCastAdapter == null) {
                mMovieCastAdapter = new MovieCastAdapter(LayoutInflater.from(getActivity()));
            }
            return mMovieCastAdapter;
        }

        private MovieCrewAdapter getMovieCrewAdapter() {
            if (mMovieCrewAdapter == null) {
                mMovieCrewAdapter = new MovieCrewAdapter(LayoutInflater.from(getActivity()));
            }
            return mMovieCrewAdapter;
        }

        private MovieTrailersAdapter getMovieTrailersAdapter() {
            if (mMovieTrailersAdapter == null) {
                mMovieTrailersAdapter = new MovieTrailersAdapter(LayoutInflater.from(getActivity()));
            }
            return mMovieTrailersAdapter;
        }
    }

    @Override
    protected void setSupportActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar, false);
    }
}
