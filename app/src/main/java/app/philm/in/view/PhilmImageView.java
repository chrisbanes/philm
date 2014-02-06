package app.philm.in.view;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmMovie;
import app.philm.in.util.ColorUtils;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.TextUtils;

public class PhilmImageView extends ImageView {

    private static final int TYPE_BACKDROP = 0;
    private static final int TYPE_POSTER = 1;

    @Inject ImageHelper mImageHelper;

    private int mType;
    private PhilmMovie mMovieToLoad;
    private PhilmCast mCastToLoad;

    private Callback mCallback;

    private boolean mFindDominant;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PhilmApplication.from(context).inject(this);
    }

    public void loadPosterUrl(PhilmMovie movie) {
        loadPosterUrl(movie, null);
    }

    public void loadPosterUrl(PhilmMovie movie, Callback listener) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            mCallback = listener;
            if (canLoadImage()) {
                loadUrlImmediate(movie, TYPE_POSTER);
            } else {
                mType = TYPE_POSTER;
                mMovieToLoad = movie;
            }
        } else {
            mMovieToLoad = null;
            setImageDrawable(null);
        }
    }

    public void loadBackdropUrl(PhilmMovie movie) {
        loadBackdropUrl(movie, null);
    }

    public void loadBackdropUrl(PhilmMovie movie, Callback listener) {
        if (!TextUtils.isEmpty(movie.getBackdropUrl())) {
            mCallback = listener;
            mFindDominant = true;
            if (canLoadImage()) {
                loadUrlImmediate(movie, TYPE_BACKDROP);
            } else {
                mType = TYPE_BACKDROP;
                mMovieToLoad = movie;
            }
        } else {
            mMovieToLoad = null;
            setImageDrawable(null);
        }
    }

    public void loadProfileUrl(PhilmCast cast) {
        loadProfileUrl(cast, null);
    }

    public void loadProfileUrl(PhilmCast cast, Callback listener) {
        if (!TextUtils.isEmpty(cast.getPictureUrl())) {
            mCallback = listener;
            if (canLoadImage()) {
                loadUrlImmediate(cast);
            } else {
                mCastToLoad = cast;
            }
        } else {
            mMovieToLoad = null;
            mCallback = null;
            mCastToLoad = null;
            setImageDrawable(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && canLoadImage()) {
            if (mMovieToLoad != null) {
                loadUrlImmediate(mMovieToLoad, mType);
                mMovieToLoad = null;
            } else if (mCastToLoad != null) {
                loadUrlImmediate(mCastToLoad);
                mCastToLoad = null;
            }
        }
    }

    private boolean canLoadImage() {
        return getWidth() != 0 && getHeight() != 0;
    }

    private void loadUrlImmediate(final PhilmMovie movie, final int type) {
        String url = null;

        switch (type) {
            case TYPE_BACKDROP:
                url = mImageHelper.getFanartUrl(movie, getWidth());
                break;
            case TYPE_POSTER:
                url = mImageHelper.getPosterUrl(movie, getWidth());
                break;
        }

        if (url != null) {
            RequestCreator requestCreator = Picasso.with(getContext()).load(url);
            if (mFindDominant) {
                requestCreator = requestCreator.transform(new Transformation() {
                    @Override
                    public Bitmap transform(Bitmap bitmap) {
                        Log.d("PhilmImageView", "transform");

                        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);

                        final int[] dominantColors = ColorUtils.findDominateColors(smallBitmap, 3);

                        Log.d("PhilmImageView", "dominantColor: #" + Integer.toHexString(dominantColors[0]));

                        return bitmap;
                    }

                    @Override
                    public String key() {
                        return null;
                    }
                });
            }
            requestCreator.into(this, mCallback);

            if (Constants.DEBUG) {
                Log.d("PhilmImageView", "Loading " + url);
            }
        }
    }

    private void loadUrlImmediate(final PhilmCast cast) {
        String url = mImageHelper.getProfileUrl(cast, getWidth());

        if (url != null) {
            Picasso.with(getContext()).load(url).into(this, mCallback);
            mCallback = null;
            if (Constants.DEBUG) {
                Log.d("PhilmImageView", "Loading " + url);
            }
        }
    }


}
