package app.philm.in.view;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import android.content.Context;
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

public class PhilmImageView extends ImageView implements Callback {

    @Inject ImageHelper mImageHelper;
    private PicassoHandler mPicassoHandler;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PhilmApplication.from(context).inject(this);
    }

    public void loadPosterUrl(PhilmMovie movie) {
        loadPosterUrl(movie, null, null);
    }

    public void loadPosterUrl(PhilmMovie movie, Callback listener, Transformation transformation) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            setPicassoHandler(new MoviePosterHandler(movie, listener, transformation));
        } else {
            reset();
        }
    }

    public void loadBackdropUrl(PhilmMovie movie) {
        loadBackdropUrl(movie, null);
    }

    public void loadBackdropUrl(PhilmMovie movie, Callback listener) {
        if (!TextUtils.isEmpty(movie.getBackdropUrl())) {
            setPicassoHandler(new MovieBackdropHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadProfileUrl(PhilmCast cast) {
        loadProfileUrl(cast, null);
    }

    public void loadProfileUrl(PhilmCast cast, Callback listener) {
        if (!TextUtils.isEmpty(cast.getPictureUrl())) {
            setPicassoHandler(new CastProfileHandler(cast, listener));
        } else {
            reset();
        }
    }

    @Override
    public void onSuccess() {
        if (mPicassoHandler != null && mPicassoHandler.mCallback != null) {
            mPicassoHandler.mCallback.onSuccess();
        }
        mPicassoHandler = null;
    }

    @Override
    public void onError() {
        if (mPicassoHandler != null && mPicassoHandler.mCallback != null) {
            mPicassoHandler.mCallback.onError();
        }
        mPicassoHandler = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && canLoadImage() && mPicassoHandler != null) {
            loadUrlImmediate();
        }
    }

    private boolean canLoadImage() {
        return getWidth() != 0 && getHeight() != 0;
    }

    private void loadUrlImmediate() {
        Preconditions.checkNotNull(mPicassoHandler, "mPicassoHandler cannot be null");

        final String url = mPicassoHandler.getUrl(mImageHelper, this);

        if (url != null) {
            RequestCreator request = Picasso.with(getContext()).load(url);
            if (mPicassoHandler.mTransformation != null) {
                request.transform(mPicassoHandler.mTransformation);
            }
            request.into(this, this);

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
        if (!Objects.equal(handler, mPicassoHandler)) {
            mPicassoHandler = handler;

            if (mPicassoHandler != null && canLoadImage()) {
                loadUrlImmediate();
            }
        }
    }

    private static abstract class PicassoHandler {

        final Callback mCallback;
        final Transformation mTransformation;

        PicassoHandler(Callback callback, Transformation transformation) {
            mCallback = callback;
            mTransformation = transformation;
        }

        public abstract String getUrl(ImageHelper helper, ImageView imageView);

    }

    private class MovieBackdropHandler extends PicassoHandler {

        private final PhilmMovie mMovie;

        MovieBackdropHandler(PhilmMovie movie, Callback callback) {
            super(callback, null);
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

        MoviePosterHandler(PhilmMovie movie, Callback callback, Transformation transformation) {
            super(callback, transformation);
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

        CastProfileHandler(PhilmCast cast, Callback callback) {
            super(callback, null);
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


}
