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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.philm.in.R;

/**
 * Layout which an {@link android.widget.EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 */
public class FloatLabelLayout extends LinearLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float DEFAULT_LABEL_PADDING_LEFT = 3f;
    private static final float DEFAULT_LABEL_PADDING_TOP = 4f;
    private static final float DEFAULT_LABEL_PADDING_RIGHT = 3f;
    private static final float DEFAULT_LABEL_PADDING_BOTTOM = 4f;

    private EditText mEditText;
    private TextView mLabel;

    private CharSequence mHint;
    private Interpolator mInterpolator;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(VERTICAL);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        int leftPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingLeft,
                dipsToPix(DEFAULT_LABEL_PADDING_LEFT));
        int topPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingTop,
                dipsToPix(DEFAULT_LABEL_PADDING_TOP));
        int rightPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingRight,
                dipsToPix(DEFAULT_LABEL_PADDING_RIGHT));
        int bottomPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingBottom,
                dipsToPix(DEFAULT_LABEL_PADDING_BOTTOM));
        mHint = a.getText(R.styleable.FloatLabelLayout_floatLabelHint);

        mLabel = new TextView(context);
        mLabel.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        mLabel.setVisibility(INVISIBLE);
        mLabel.setText(mHint);
        ViewCompat.setPivotX(mLabel, 0f);
        ViewCompat.setPivotY(mLabel, 0f);

        mLabel.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small));
        a.recycle();

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mInterpolator = AnimationUtils.loadInterpolator(context,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? android.R.interpolator.fast_out_slow_in
                        : android.R.anim.decelerate_interpolator);
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        // If we already have an EditText, throw an exception
        if (mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        mEditText = editText;

        // Update the label visibility with no animation
        updateLabelVisibility(false);

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLabelVisibility(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                updateLabelVisibility(true);
            }
        });

        // If we do not have a valid hint, try and retrieve it from the EditText
        if (TextUtils.isEmpty(mHint)) {
            setHint(mEditText.getHint());
        }
    }

    private void updateLabelVisibility(boolean animate) {
        boolean hasText = !TextUtils.isEmpty(mEditText.getText());
        boolean isFocused = mEditText.isFocused();

        mLabel.setActivated(isFocused);

        if (hasText || isFocused) {
            // We should be showing the label so do so if it isn't already
            if (mLabel.getVisibility() != VISIBLE) {
                showLabel(animate);
            }
        } else {
            // We should not be showing the label so hide it
            if (mLabel.getVisibility() == VISIBLE) {
                hideLabel(animate);
            }
        }
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Set the hint to be displayed in the floating label
     */
    public void setHint(CharSequence hint) {
        mHint = hint;
        mLabel.setText(hint);
    }

    /**
     * Show the label
     */
    private void showLabel(boolean animate) {
        if (animate) {
            mLabel.setVisibility(View.VISIBLE);
            ViewCompat.setTranslationY(mLabel, mLabel.getHeight());

            float scale = mEditText.getTextSize() / mLabel.getTextSize();
            ViewCompat.setScaleX(mLabel, scale);
            ViewCompat.setScaleY(mLabel, scale);

            ViewCompat.animate(mLabel)
                    .translationY(0f)
                    .scaleY(1f)
                    .scaleX(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(null)
                    .setInterpolator(mInterpolator).start();
        } else {
            mLabel.setVisibility(VISIBLE);
        }

        mEditText.setHint(null);
    }

    /**
     * Hide the label
     */
    private void hideLabel(boolean animate) {
        if (animate) {
            float scale = mEditText.getTextSize() / mLabel.getTextSize();
            ViewCompat.setScaleX(mLabel, 1f);
            ViewCompat.setScaleY(mLabel, 1f);
            ViewCompat.setTranslationY(mLabel, 0f);

            ViewCompat.animate(mLabel)
                    .translationY(mLabel.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mLabel.setVisibility(INVISIBLE);
                            mEditText.setHint(mHint);
                        }
                    })
                    .setInterpolator(mInterpolator).start();
        } else {
            mLabel.setVisibility(INVISIBLE);
            mEditText.setHint(mHint);
        }
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }
}