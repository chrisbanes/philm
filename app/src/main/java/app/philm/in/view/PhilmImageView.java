package app.philm.in.view;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.model.PhilmCast;
import app.philm.in.model.PhilmMovie;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.TextUtils;

public class PhilmImageView extends ImageView {

    private static final int TRANSITION_DURATION = 200;

    public interface Listener {

        public void onSuccess(PhilmImageView imageView, Bitmap bitmap);

        public void onError(PhilmImageView imageView);

    }

    @Inject ImageHelper mImageHelper;
    private PicassoHandler mPicassoHandler;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PhilmApplication.from(context).inject(this);
    }

    public void loadPosterUrl(PhilmMovie movie) {
        loadPosterUrl(movie, null);
    }

    public void loadPosterUrl(PhilmMovie movie, Listener listener) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            setPicassoHandler(new MoviePosterHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadBackdropUrl(PhilmMovie movie) {
        loadBackdropUrl(movie, null);
    }

    public void loadBackdropUrl(PhilmMovie movie, Listener listener) {
        if (!TextUtils.isEmpty(movie.getBackdropUrl())) {
            setPicassoHandler(new MovieBackdropHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadProfileUrl(PhilmCast cast) {
        loadProfileUrl(cast, null);
    }

    public void loadProfileUrl(PhilmCast cast, Listener listener) {
        if (!TextUtils.isEmpty(cast.getPictureUrl())) {
            setPicassoHandler(new CastProfileHandler(cast, listener));
        } else {
            reset();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && canLoadImage() && mPicassoHandler != null && !mPicassoHandler.isStarted()) {
            loadUrlImmediate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Picasso.with(getContext()).cancelRequest(mPicassoTarget);

        super.onDetachedFromWindow();
    }

    private boolean canLoadImage() {
        return getWidth() != 0 && getHeight() != 0;
    }

    private void loadUrlImmediate() {
        Preconditions.checkNotNull(mPicassoHandler, "mPicassoHandler cannot be null");

        final String url = mPicassoHandler.getUrl(mImageHelper, this);

        if (url != null) {
            mPicassoHandler.markAsStarted();
            Picasso.with(getContext()).load(url)
                    .resize(getWidth(), getHeight()).centerCrop()
                    .into(mPicassoTarget);

            if (Constants.DEBUG) {
                Log.d("PhilmImageView", "Loading " + url);
            }
        }
    }

    private void reset() {
        setPicassoHandler(null);
        setImageDrawable(null);
    }

    private void setPicassoHandler(PicassoHandler handler) {
        if (mPicassoHandler != null) {
            Picasso.with(getContext()).cancelRequest(mPicassoTarget);
        }

        if (handler != null) {
            mPicassoHandler = handler;
            if (canLoadImage()) {
                loadUrlImmediate();
            }
        }
    }

    private static abstract class PicassoHandler {

        private final Listener mCallback;

        private boolean mIsStarted;

        PicassoHandler(Listener callback) {
            mCallback = callback;
        }

        public abstract String getUrl(ImageHelper helper, ImageView imageView);

        void markAsStarted() {
            mIsStarted = true;
        }

        void markAsFinished() {
            mIsStarted = false;
        }

        boolean isStarted() {
            return mIsStarted;
        }
    }

    private class MovieBackdropHandler extends PicassoHandler {

        private final PhilmMovie mMovie;

        MovieBackdropHandler(PhilmMovie movie, Listener callback) {
            super(callback);
            mMovie = Preconditions.checkNotNull(movie, "movie cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getFanartUrl(mMovie, imageView.getWidth());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MovieBackdropHandler that = (MovieBackdropHandler) o;
            return Objects.equal(mMovie, that.mMovie);
        }

        @Override
        public int hashCode() {
            return mMovie != null ? mMovie.hashCode() : 0;
        }
    }

    private class MoviePosterHandler extends PicassoHandler {

        private final PhilmMovie mMovie;

        MoviePosterHandler(PhilmMovie movie, Listener callback) {
            super(callback);
            mMovie = Preconditions.checkNotNull(movie, "movie cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getPosterUrl(mMovie, imageView.getWidth());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MoviePosterHandler that = (MoviePosterHandler) o;
            return Objects.equal(mMovie, that.mMovie);
        }
    }

    private class CastProfileHandler extends PicassoHandler {

        private final PhilmCast mCast;

        CastProfileHandler(PhilmCast cast, Listener callback) {
            super(callback);
            mCast = Preconditions.checkNotNull(cast, "cast cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getProfileUrl(mCast, imageView.getWidth());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CastProfileHandler that = (CastProfileHandler) o;
            return Objects.equal(mCast, that.mCast);
        }
    }

    private final Target mPicassoTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            setImageBitmapFromNetwork(bitmap, loadedFrom != Picasso.LoadedFrom.MEMORY);

            if (mPicassoHandler != null) {
                if (mPicassoHandler.mCallback != null) {
                    mPicassoHandler.mCallback.onSuccess(PhilmImageView.this, bitmap);
                }
                mPicassoHandler.markAsFinished();
            }
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            if (mPicassoHandler != null) {
                if (mPicassoHandler.mCallback != null) {
                    mPicassoHandler.mCallback.onError(PhilmImageView.this);
                }
                mPicassoHandler.markAsFinished();
            }
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            setImageDrawable(null);
        }
    };

    void setImageBitmapFromNetwork(final Bitmap bitmap, final boolean fade) {
        if (fade) {
            Drawable currentDrawable = getDrawable();
            if (currentDrawable == null) {
                currentDrawable = new ColorDrawable(Color.TRANSPARENT);
            }

            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{currentDrawable, new BitmapDrawable(getResources(), bitmap)});
            transitionDrawable.setCrossFadeEnabled(true);

            setImageDrawable(transitionDrawable);

            transitionDrawable.startTransition(TRANSITION_DURATION);
        } else {
            setImageBitmap(bitmap);
        }
    }

    private boolean handlersEquals(PicassoHandler handler1, PicassoHandler handler2) {
        if (!Objects.equal(handler1, handler2)) {
            String handler1Url = handler1 != null ? handler1.getUrl(mImageHelper, this) : null;
            String handler2Url = handler2 != null ? handler2.getUrl(mImageHelper, this) : null;
            return Objects.equal(handler1Url, handler2Url);
        }
        return true;
    }


}
