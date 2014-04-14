package app.philm.in.view;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.drawable.RoundedAvatarDrawable;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.model.PhilmTrailer;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.TextUtils;

public class PhilmImageView extends ImageView {

    private static final int TRANSITION_DURATION = 175;

    public interface Listener {

        public void onSuccess(PhilmImageView imageView, Bitmap bitmap);

        public void onError(PhilmImageView imageView);

    }

    @Inject ImageHelper mImageHelper;
    private PicassoHandler mPicassoHandler;

    private final Drawable mTransparentDrawable;

    private boolean mAutoFade = true;
    private boolean mAvatarMode = false;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PhilmApplication.from(context).inject(this);

        mTransparentDrawable = new ColorDrawable(Color.TRANSPARENT);
    }

    public void setAvatarMode(boolean avatarMode) {
        mAvatarMode = avatarMode;
    }

    public void loadPoster(PhilmMovie movie) {
        loadPoster(movie, null);
    }

    public void loadPoster(PhilmMovie movie, Listener listener) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            setPicassoHandler(new MoviePosterHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadPoster(PhilmPersonCredit credit) {
        loadPoster(credit, null);
    }

    public void loadPoster(PhilmPersonCredit credit, Listener listener) {
        if (!TextUtils.isEmpty(credit.getPosterPath())) {
            setPicassoHandler(new PersonCreditHandler(credit, listener));
        } else {
            reset();
        }
    }

    public void loadBackdrop(PhilmMovie movie) {
        loadBackdrop(movie, null);
    }

    public void loadBackdrop(PhilmMovie movie, Listener listener) {
        if (!TextUtils.isEmpty(movie.getBackdropUrl())) {
            setPicassoHandler(new MovieBackdropHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadProfile(PhilmPerson person) {
        loadProfile(person, null);
    }

    public void loadProfile(PhilmPerson cast, Listener listener) {
        if (!TextUtils.isEmpty(cast.getPictureUrl())) {
            setPicassoHandler(new CastProfileHandler(cast, listener));
        } else {
            reset();
            setImageResourceImpl(R.drawable.ic_profile_placeholder);
        }
    }

    public void loadTrailer(PhilmTrailer trailer) {
        loadTrailer(trailer, null);
    }

    public void loadTrailer(PhilmTrailer trailer, Listener listener) {
        setPicassoHandler(new MovieTrailerHandler(trailer, listener));
    }

    public void setAutoFade(boolean autoFade) {
        mAutoFade = autoFade;
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

            RequestCreator request = Picasso.with(getContext()).load(url);
            if (mPicassoHandler.getPlaceholderDrawable() != 0) {
                request = request.placeholder(mPicassoHandler.getPlaceholderDrawable());
            }
            request.resize(getWidth(), getHeight()).centerCrop().into(mPicassoTarget);

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

        int getPlaceholderDrawable() {
            return 0;
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
            return helper.getFanartUrl(mMovie, imageView.getWidth(), imageView.getHeight());
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
            return helper.getPosterUrl(mMovie, imageView.getWidth(), imageView.getHeight());
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

    private class MovieTrailerHandler extends PicassoHandler {

        private final PhilmTrailer mTrailer;

        MovieTrailerHandler(PhilmTrailer movie, Listener callback) {
            super(callback);
            mTrailer = Preconditions.checkNotNull(movie, "movie cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getTrailerUrl(mTrailer, imageView.getWidth(), imageView.getHeight());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MovieTrailerHandler that = (MovieTrailerHandler) o;
            return Objects.equal(mTrailer, that.mTrailer);
        }
    }

    private class CastProfileHandler extends PicassoHandler {

        private final PhilmPerson mPerson;

        CastProfileHandler(PhilmPerson cast, Listener callback) {
            super(callback);
            mPerson = Preconditions.checkNotNull(cast, "cast cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getProfileUrl(mPerson, imageView.getWidth(), imageView.getHeight());
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
            return Objects.equal(mPerson, that.mPerson);
        }

        @Override
        int getPlaceholderDrawable() {
            return R.drawable.ic_profile_placeholder;
        }
    }

    private class PersonCreditHandler extends PicassoHandler {

        private final PhilmPersonCredit mCredit;

        PersonCreditHandler(PhilmPersonCredit credit, Listener callback) {
            super(callback);
            mCredit = Preconditions.checkNotNull(credit, "credit cannot be null");
        }

        @Override
        public String getUrl(ImageHelper helper, ImageView imageView) {
            return helper.getPosterUrl(mCredit, imageView.getWidth(), imageView.getHeight());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PersonCreditHandler that = (PersonCreditHandler) o;
            return Objects.equal(mCredit, that.mCredit);
        }
    }

    private final Target mPicassoTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            setImageBitmapFromNetwork(bitmap, loadedFrom);

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
            setImageDrawableImpl(drawable);
        }
    };

    void setImageBitmapFromNetwork(final Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        final boolean fade = mAutoFade && loadedFrom != Picasso.LoadedFrom.MEMORY;

        if (fade) {
            setAlpha(0f);
            setImageBitmapImpl(bitmap);
            animate().alpha(1f).setDuration(TRANSITION_DURATION);
        } else {
            setImageBitmapImpl(bitmap);
        }
    }

    void setImageBitmapImpl(final Bitmap bitmap) {
        if (mAvatarMode) {
            setImageDrawable(new RoundedAvatarDrawable(bitmap));
        } else {
            setImageBitmap(bitmap);
        }
    }

    void setImageDrawableImpl(final Drawable drawable) {
        if (mAvatarMode && drawable instanceof BitmapDrawable) {
            setImageBitmapImpl(((BitmapDrawable) drawable).getBitmap());
        } else {
            setImageDrawable(drawable);
        }
    }

    void setImageResourceImpl(int resId) {
        if (mAvatarMode) {
            BitmapDrawable d = (BitmapDrawable) getResources().getDrawable(resId);
            setImageDrawable(new RoundedAvatarDrawable(d.getBitmap()));
        } else {
            setImageResource(resId);
        }
    }

}
