package app.philm.in.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.philm.in.R;

public class RatingBarLayout extends FrameLayout {

    private final View mLeftRatingBarLayout;
    private final View mRightRatingBarLayout;

    private final RatingCircleView mRatingCircleView;

    private final TextView mRatingGlobalPercentageTextView;
    private final TextView mRatingGlobalVotesTextView;

    private int mRatingGlobalPercentage;
    private int mRatingGlobalVotes;

    public RatingBarLayout(Context context) {
        this(context, null);
    }

    public RatingBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.include_rating_bar, this);

        mRatingCircleView = (RatingCircleView) findViewById(R.id.rcv_rating);

        mLeftRatingBarLayout = findViewById(R.id.rating_left_bar);
        mRightRatingBarLayout = findViewById(R.id.rating_right_bar);

        mRatingGlobalPercentageTextView = (TextView) findViewById(R.id.textview_global_rating);
        mRatingGlobalVotesTextView = (TextView) findViewById(R.id.textview_votes);
    }

    public void setRatingCircleEnabled(boolean enabled) {
        mRatingCircleView.setEnabled(enabled);
    }

    public void showUserRating(int userRating) {
        mRatingCircleView.showRating(userRating);
    }

    public void showRatePrompt() {
        mRatingCircleView.showRatePrompt();
    }

    public void setRatingGlobalPercentage(int ratingGlobalPercentage) {
        if (mRatingGlobalPercentage != ratingGlobalPercentage) {
            mRatingGlobalPercentage = ratingGlobalPercentage;
            mRatingGlobalPercentageTextView.setText(ratingGlobalPercentage + "%");
        }
    }

    public void setRatingGlobalVotes(int ratingGlobalVotes) {
        if (mRatingGlobalVotes != ratingGlobalVotes) {
            mRatingGlobalVotes = ratingGlobalVotes;
            mRatingGlobalVotesTextView.setText(String.valueOf(ratingGlobalVotes));
        }
    }

    public void setRatingCircleClickListener(OnClickListener clickListener) {
        mRatingCircleView.setOnClickListener(clickListener);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            final int targetPadding = mRatingCircleView.getWidth() / 2;
            if (mLeftRatingBarLayout.getPaddingRight() != targetPadding ||
                    mRightRatingBarLayout.getPaddingLeft() != targetPadding) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mLeftRatingBarLayout.setPadding(0, 0, targetPadding, 0);
                        mRightRatingBarLayout.setPadding(targetPadding, 0, 0, 0);
                    }
                });
            }
        }

    }
}
