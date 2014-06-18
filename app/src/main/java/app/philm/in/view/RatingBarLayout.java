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

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.drawable.PercentageDrawable;
import app.philm.in.model.ColorScheme;
import app.philm.in.util.ColorUtils;
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

    private ColorScheme mColorScheme;
    private ValueAnimator mColorSchemeAnimator;

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
    public void setColorScheme(final ColorScheme colorScheme) {
        if (!Objects.equal(mColorScheme, colorScheme)) {
            mColorScheme = colorScheme;

            if (mColorSchemeAnimator != null && mColorSchemeAnimator.isRunning()) {
                mColorSchemeAnimator.cancel();
            }

            if (getDrawingTime() > 0) {
                setColorSchemeAnimate(colorScheme);
            } else {
                setColorSchemeNoAnimate(colorScheme);
            }
        }
    }

    void setColorSchemeNoAnimate(ColorScheme colorScheme) {
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

    void setColorSchemeAnimate(final ColorScheme scheme) {
        final PercentageDrawable percentageDrawable = mRatingCircleView.getPercentageDrawable();

        final int leftRatingBarColor = ((ColorDrawable) mLeftRatingBarLayout.getBackground())
                .getColor();
        final int rightRatingBarColor = ((ColorDrawable) mRightRatingBarLayout.getBackground())
                .getColor();

        final ColorDrawable leftRatingBarBackground = new ColorDrawable(leftRatingBarColor);
        mLeftRatingBarLayout.setBackgroundDrawable(leftRatingBarBackground);
        final ColorDrawable rightRatingBarBackground = new ColorDrawable(rightRatingBarColor);
        mRightRatingBarLayout.setBackgroundDrawable(rightRatingBarBackground);

        final int ratingPercTextColor = mRatingGlobalPercentageTextView.getCurrentTextColor();
        final int ratingPercLabelColor = mRatingGlobalPercentageLabelTextView.getCurrentTextColor();

        final int ratingVotesTextColor = mRatingGlobalVotesTextView.getCurrentTextColor();
        final int ratingVotesLabelColor = mRatingGlobalVotesLabelTextView.getCurrentTextColor();

        final int circleForegroundColor = percentageDrawable.getForegroundCircleColor();
        final int circleArcColor = percentageDrawable.getArcColor();
        final int circleTextColor = percentageDrawable.getTextColor();

        mColorSchemeAnimator = new ValueAnimator();
        mColorSchemeAnimator.setFloatValues(0f, 1f);
        mColorSchemeAnimator.setDuration(175);
        mColorSchemeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float currentValue = 1f - (Float) valueAnimator.getAnimatedValue();

                leftRatingBarBackground.setColor(ColorUtils.blendColors(
                        leftRatingBarColor, scheme.primaryAccent, currentValue));
                rightRatingBarBackground.setColor(ColorUtils.blendColors(
                        rightRatingBarColor, scheme.primaryAccent, currentValue));

                mRatingGlobalPercentageTextView.setTextColor(ColorUtils.blendColors(
                        ratingPercTextColor, scheme.primaryText, currentValue));
                mRatingGlobalPercentageLabelTextView.setTextColor(ColorUtils.blendColors(
                        ratingPercLabelColor, scheme.primaryText, currentValue));

                mRatingGlobalVotesTextView.setTextColor(ColorUtils.blendColors(
                        ratingVotesTextColor, scheme.primaryText, currentValue));
                mRatingGlobalVotesLabelTextView.setTextColor(ColorUtils.blendColors(
                        ratingVotesLabelColor, scheme.primaryText, currentValue));

                percentageDrawable.setForegroundCircleColor(ColorUtils
                        .blendColors(circleForegroundColor, scheme.secondaryAccent, currentValue));
                percentageDrawable.setArcColor(ColorUtils
                        .blendColors(circleArcColor, scheme.tertiaryAccent, currentValue));
                percentageDrawable.setTextColor(ColorUtils
                        .blendColors(circleTextColor, scheme.tertiaryAccent, currentValue));
            }
        });
        mColorSchemeAnimator.start();
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
