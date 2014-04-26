package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.view.PhilmImageView;

public class MovieImagesFragment extends BasePhilmMovieFragment
        implements MovieController.MovieImagesUi {

    private static final String KEY_QUERY_MOVIE_ID = "movie_id";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_images, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ImageAdapter();
        mViewPager.setAdapter(mAdapter);
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
        }
    }

    private class ImageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final PhilmMovie.BackdropImage image = mImages.get(position);

            final PhilmImageView imageView = new PhilmImageView(getActivity(), null);
            imageView.loadBackdrop(image);

            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            return imageView;
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
}
