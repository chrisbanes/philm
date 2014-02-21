package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.util.TextUtils;

public class MovieDetailCardLayout extends LinearLayout {

    private final TextView mTitleTextView;
    private final TextView mSeeMoreTextView;
    private LinearLayout mCardContent;

    public MovieDetailCardLayout(Context context) {
        this(context, null);
    }

    public MovieDetailCardLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieDetailCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.include_movie_detail_card, this, true);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mSeeMoreTextView = (TextView) findViewById(R.id.textview_see_more);
        mCardContent = (LinearLayout) getChildAt(1);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MovieDetailCardLayout);
        final String title = a.getString(R.styleable.MovieDetailCardLayout_title);
        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
        a.recycle();
    }

    public void setSeeMoreVisibility(boolean visible) {
        mSeeMoreTextView.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setSeeMoreOnClickListener(OnClickListener listener) {
        mSeeMoreTextView.setOnClickListener(listener);
    }

    public void setTitle(CharSequence title) {
        mTitleTextView.setText(title);
    }

    public void setTitle(int titleResId) {
        setTitle(getResources().getString(titleResId));
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mCardContent != null) {
            mCardContent.addView(child, index, params);
        } else {
            super.addView(child, index, params);
        }
    }
}
