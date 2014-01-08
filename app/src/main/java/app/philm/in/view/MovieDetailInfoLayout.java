package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.util.TextUtils;

public class MovieDetailInfoLayout extends LinearLayout {

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

    public void setContentText(String text) {
        mContentTextView.setText(text);
    }
}
