package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.view.PhilmImageView;

public class MovieImagesFragment extends BasePhilmMovieFragment
        implements MovieController.MovieImagesUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

    private static final String BUNDLE_CURRENT_ITEM = "viewpager_current";

    public static MovieImagesFragment create(String movieId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(movieId), "movieId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_MOVIE_ID, movieId);

        MovieImagesFragment fragment = new MovieImagesFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private ViewPager mViewPager;
    private ImageAdapter mAdapter;
    private List<PhilmMovie.BackdropImage> mImages;

    private int mVisibleItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_images, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ImageAdapter();

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setPageTransformer(true, new CardTransformer(0.8f));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_CURRENT_ITEM)) {
            mVisibleItem = savedInstanceState.getInt(BUNDLE_CURRENT_ITEM);
        }
    }

    @Override
    public void onPause() {
        mVisibleItem = mViewPager.getCurrentItem();
        super.onPause();
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.MOVIE_IMAGES;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_MOVIE_ID);
    }

    @Override
    public String getUiTitle() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public void setItems(List<PhilmMovie.BackdropImage> images) {
        mImages = images;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(mVisibleItem);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_CURRENT_ITEM, mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    private class ImageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final PhilmMovie.BackdropImage image = mImages.get(position);

            final View view = getLayoutInflater(null)
                    .inflate(R.layout.item_movie_image, container, false);

            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

            final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_backdrop);
            imageView.setAutoFade(false);
            imageView.loadBackdrop(image, new PhilmImageView.Listener() {
                @Override
                public void onSuccess(PhilmImageView imageView, Bitmap bitmap) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(PhilmImageView imageView) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mImages != null ? mImages.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    /**
     * Awesome Launcher-inspired page transformer
     */
    private static class CardTransformer implements ViewPager.PageTransformer {

        private final float scaleAmount;

        public CardTransformer(float scalingStart) {
            scaleAmount = 1 - scalingStart;
        }

        @Override
        public void transformPage(View page, float position) {
            if (position >= 0f) {
                final int w = page.getWidth();
                float scaleFactor = 1 - scaleAmount * position;

                page.setAlpha(1f - position);
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setTranslationX(w * (1 - position) - w);
            }
        }
    }
}
