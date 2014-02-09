package app.philm.in.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.drawable.PercentageDrawable;
import app.philm.in.model.ColorScheme;
import app.philm.in.util.TextUtils;

public class RatingBarLayout extends FrameLayout implements ColorSchemable {

    private final View mLeftRatingBarLayout;
    private final View mRightRatingBarLayout;

    private final RatingCircleView mRatingCircleView;

    private final TextView mRatingGlobalPercentageTextView;
    private final TextView mRatingGlobalPercentageLabelTextView;
    private final TextView mRatingGlobalVotesTextView;
    private final TextView mRatingGlobalVotesLabelTextView;

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
        mRatingGlobalPercentageLabelTextView = (TextView) findViewById(R.id.textview_global_rating_label);
        mRatingGlobalVotesTextView = (TextView) findViewById(R.id.textview_votes);
        mRatingGlobalVotesLabelTextView = (TextView) findViewById(R.id.textview_votes_label);

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
        if (TextUtils.isEmpty(mRatingGlobalPercentageTextView.getText()) ||
                mRatingGlobalPercentage != ratingGlobalPercentage) {
            mRatingGlobalPercentage = ratingGlobalPercentage;
            mRatingGlobalPercentageTextView.setText(ratingGlobalPercentage + "%");
        }
    }

    public void setRatingGlobalVotes(int ratingGlobalVotes) {
        if (TextUtils.isEmpty(mRatingGlobalVotesTextView.getText())
                || mRatingGlobalVotes != ratingGlobalVotes) {
            mRatingGlobalVotes = ratingGlobalVotes;
            mRatingGlobalVotesTextView.setText(String.valueOf(ratingGlobalVotes));
        }
    }

    public void setRatingCircleClickListener(OnClickListener clickListener) {
        mRatingCircleView.setOnClickListener(clickListener);
    }

    @Override
    public void setColorScheme(ColorScheme colorScheme) {
        mLeftRatingBarLayout.setBackgroundColor(colorScheme.primaryAccent);
        mRightRatingBarLayout.setBackgroundColor(colorScheme.primaryAccent);

        mRatingGlobalPercentageTextView.setTextColor(colorScheme.primaryText);
        mRatingGlobalPercentageLabelTextView.setTextColor(colorScheme.primaryText);

        mRatingGlobalVotesTextView.setTextColor(colorScheme.primaryText);
        mRatingGlobalVotesLabelTextView.setTextColor(colorScheme.primaryText);

        PercentageDrawable percentageDrawable = mRatingCircleView.getPercentageDrawable();
        if (percentageDrawable != null) {
            percentageDrawable.setForegroundCircleColor(colorScheme.secondaryAccent);

            percentageDrawable.setTextColor(colorScheme.tertiaryAccent);
            percentageDrawable.setArcColor(colorScheme.tertiaryAccent);
        }
        mRatingCircleView.invalidate();
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
