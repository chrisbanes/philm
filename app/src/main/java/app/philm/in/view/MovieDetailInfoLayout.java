package app.philm.in.view;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.util.TextUtils;

public class MovieDetailInfoLayout extends LinearLayout implements Target {

    private final TextView mTitleTextView;
    private final TextView mContentTextView;

    public MovieDetailInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.include_movie_detail_info, this, true);

        mTitleTextView = (TextView) findViewById(android.R.id.text1);
        mContentTextView = (TextView) findViewById(android.R.id.text2);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MovieDetailInfoLayout);

        final String title = a.getString(R.styleable.MovieDetailInfoLayout_title);
        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }

        a.recycle();
    }

    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public TextView getContentTextView() {
        return mContentTextView;
    }

    public void setContentText(CharSequence text) {
        mContentTextView.setText(text);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        mContentTextView.setCompoundDrawablesWithIntrinsicBounds(
                new BitmapDrawable(getResources(), bitmap),
                null, null, null);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        mContentTextView.setCompoundDrawables(null, null, null, null);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        mContentTextView.setCompoundDrawables(null, null, null, null);
    }
}
