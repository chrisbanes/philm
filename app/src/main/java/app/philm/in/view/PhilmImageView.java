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

package app.philm.in.view;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.drawable.RoundedAvatarDrawable;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmMovieVideo;
import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.util.AnimationUtils;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.TextUtils;

public class PhilmImageView extends ImageView {

    public interface Listener {

        public void onSuccess(PhilmImageView imageView, Bitmap bitmap);

        public void onError(PhilmImageView imageView);

    }

    @Inject ImageHelper mImageHelper;
    private PicassoHandler mPicassoHandler;
    private boolean mAutoFade = true;
    private boolean mAvatarMode = false;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PhilmApplication.from(context).inject(this);
    }

    public void setAvatarMode(boolean avatarMode) {
        mAvatarMode = avatarMode;
    }

    public void loadPoster(PhilmMovie movie) {
        loadPoster(movie, null);
    }

    public void loadPoster(PhilmMovie movie, Listener listener) {
        if (movie.hasPosterUrl()) {
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
        if (movie.hasBackdropUrl()) {
            setPicassoHandler(new MovieBackdropHandler(movie, listener));
        } else {
            reset();
        }
    }

    public void loadBackdrop(PhilmMovie.BackdropImage image) {
        loadBackdrop(image, null);
    }

    public void loadBackdrop(PhilmMovie.BackdropImage image, Listener listener) {
        if (!TextUtils.isEmpty(image.url)) {
            setPicassoHandler(new MovieBackdropImageHandler(image, listener));
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

    public void loadTrailer(PhilmMovieVideo trailer) {
        loadTrailer(trailer, null);
    }

    public void loadTrailer(PhilmMovieVideo trailer, Listener listener) {
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
            if (mPicassoHandler.centerCrop()) {
                request = request.resize(getWidth(), getHeight()).centerCrop();
            } else {
                request = request.resize(getWidth(), getHeight()).centerInside();
            }
            request.into(mPicassoTarget);

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
        if (mPicassoHandler != null && mPicassoHandler.isStarted()
                && !mPicassoHandler.isFinished()) {
            Picasso.with(getContext()).cancelRequest(mPicassoTarget);
        }

        if (handler != null && Objects.equal(handler, mPicassoHandler)) {
            handler.setDisplayPlaceholder(false);
        }

        mPicassoHandler = handler;

        if (handler != null && canLoadImage()) {
            loadUrlImmediate();
        }
    }

    private static abstract class PicassoHandler<T> {

        private final T mObject;
        private final Listener mCallback;
        private boolean mIsStarted, mIsFinished;
        private boolean mDisplayPlaceholder = true;

        PicassoHandler(T object, Listener callback) {
            mObject = Preconditions.checkNotNull(object, "object cannot be null");
            mCallback = callback;
        }

        public final String getUrl(ImageHelper helper, ImageView imageView) {
            return buildUrl(mObject, helper, imageView);
        }

        protected abstract String buildUrl(T object, ImageHelper helper, ImageView imageView);

        void markAsStarted() {
            mIsStarted = true;
        }

        void markAsFinished() {
            mIsFinished = true;
        }

        boolean isStarted() {
            return mIsStarted;
        }

        boolean isFinished() {
            return mIsFinished;
        }

        int getPlaceholderDrawable() {
            return 0;
        }

        public void setDisplayPlaceholder(boolean displayPlaceholder) {
            mDisplayPlaceholder = displayPlaceholder;
        }

        public boolean shouldDisplayPlaceholder() {
            return mDisplayPlaceholder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PicassoHandler that = (PicassoHandler) o;
            return Objects.equal(mObject, that.mObject);
        }

        public boolean centerCrop() {
            return true;
        }

        @Override
        public int hashCode() {
            return mObject != null ? mObject.hashCode() : 0;
        }
    }

    private class MovieBackdropHandler extends PicassoHandler<PhilmMovie> {

        MovieBackdropHandler(PhilmMovie movie, Listener callback) {
            super(movie, callback);
        }

        @Override
        protected String buildUrl(PhilmMovie movie, ImageHelper helper, ImageView imageView) {
            return helper.getFanartUrl(movie, imageView.getWidth(), imageView.getHeight());
        }

    }

    private class MoviePosterHandler extends PicassoHandler<PhilmMovie> {

        MoviePosterHandler(PhilmMovie movie, Listener callback) {
            super(movie, callback);
        }

        @Override
        protected String buildUrl(PhilmMovie movie, ImageHelper helper, ImageView imageView) {
            return helper.getPosterUrl(movie, imageView.getWidth(), imageView.getHeight());
        }

    }

    private class MovieTrailerHandler extends PicassoHandler<PhilmMovieVideo> {

        MovieTrailerHandler(PhilmMovieVideo trailer, Listener callback) {
            super(trailer, callback);
        }

        @Override
        protected String buildUrl(PhilmMovieVideo trailer, ImageHelper helper, ImageView imageView) {
            return helper.getVideoSnapshotUrl(trailer, imageView.getWidth(), imageView.getHeight());
        }

    }

    private class MovieBackdropImageHandler extends PicassoHandler<PhilmMovie.BackdropImage> {

        MovieBackdropImageHandler(PhilmMovie.BackdropImage backdrop, Listener callback) {
            super(backdrop, callback);
        }

        @Override
        protected String buildUrl(PhilmMovie.BackdropImage backdrop, ImageHelper helper,
                ImageView imageView) {
            return helper.getFanartUrl(backdrop, imageView.getWidth(), imageView.getHeight());
        }

        @Override
        public boolean centerCrop() {
            return false;
        }
    }

    private class CastProfileHandler extends PicassoHandler<PhilmPerson> {

        CastProfileHandler(PhilmPerson person, Listener callback) {
            super(person, callback);
        }

        @Override
        protected String buildUrl(PhilmPerson person, ImageHelper helper, ImageView imageView) {
            return helper.getProfileUrl(person, imageView.getWidth(), imageView.getHeight());
        }

        @Override
        int getPlaceholderDrawable() {
            return R.drawable.ic_profile_placeholder;
        }
    }

    private class PersonCreditHandler extends PicassoHandler<PhilmPersonCredit> {

        PersonCreditHandler(PhilmPersonCredit credit, Listener callback) {
            super(credit, callback);
        }

        @Override
        protected String buildUrl(PhilmPersonCredit credit, ImageHelper helper, ImageView imageView) {
            return helper.getPosterUrl(credit, imageView.getWidth(), imageView.getHeight());
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
            if (mPicassoHandler == null || mPicassoHandler.shouldDisplayPlaceholder()) {
                setImageDrawableImpl(drawable);
            }
        }

    };

    void setImageBitmapFromNetwork(final Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        final boolean fade = mAutoFade && loadedFrom != Picasso.LoadedFrom.MEMORY;
        final Drawable currentDrawable = getDrawable();

        if (fade) {
            if (currentDrawable == null || mPicassoHandler.getPlaceholderDrawable() != 0) {
                // If we have no current drawable, or it is a placeholder drawable. Just fade in
                setVisibility(View.INVISIBLE);
                setImageBitmapImpl(bitmap);
                AnimationUtils.Fade.show(this);
            } else {
                AnimationUtils.startCrossFade(this, currentDrawable,
                        new BitmapDrawable(getResources(), bitmap));
            }
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
