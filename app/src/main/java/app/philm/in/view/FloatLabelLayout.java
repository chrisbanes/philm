package app.philm.in.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.philm.in.R;

public class FloatLabelLayout extends FrameLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float PADDING_LEFT_RIGHT_DP = 12f;

    private EditText mEditText;
    private TextView mLabel;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        final int padding = dipsToPix(PADDING_LEFT_RIGHT_DP);
        mLabel = new TextView(context);
        mLabel.setPadding(padding, 0, padding, 0);
        mLabel.setVisibility(INVISIBLE);

        final int labelStyle = array.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance, 0);
        if (labelStyle != 0) {
            mLabel.setTextAppearance(context, labelStyle);
        }

        addView(mLabel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        array.recycle();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() == 0) {
            super.addView(child, index, params);
        } else if (getChildCount() == 1) {
            if (child instanceof EditText) {
                final LayoutParams lp = new LayoutParams(params);
                lp.gravity = Gravity.BOTTOM;
                lp.topMargin = (int) mLabel.getTextSize();

                super.addView(child, index, lp);
                setEditText((EditText) child);
            } else {
                throw new IllegalArgumentException("Child must be a EditText");
            }
        } else {
            throw new IllegalArgumentException("Can not have more than 1 child");
        }
    }

    private void setEditText(EditText editText) {
        mEditText = editText;
        mEditText.addTextChangedListener(mTextWatcher);
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                mLabel.setActivated(focused);
            }
        });

        mLabel.setText(mEditText.getHint());
    }

    public EditText getEditText() {
        return mEditText;
    }

    public TextView getLabel() {
        return mLabel;
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public final void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
                if (mLabel.getVisibility() != View.VISIBLE) {
                    mLabel.setVisibility(View.VISIBLE);
                    mLabel.setAlpha(0f);
                    mLabel.setTranslationY(mLabel.getHeight());
                    mLabel.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(ANIMATION_DURATION)
                            .setListener(null).start();
                }
            } else {
                if (mLabel.getVisibility() == View.VISIBLE) {
                    mLabel.setAlpha(1f);
                    mLabel.setTranslationY(0f);
                    mLabel.animate()
                            .alpha(0f)
                            .translationY(mLabel.getHeight())
                            .setDuration(ANIMATION_DURATION)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mLabel.setVisibility(View.GONE);
                                }
                            }).start();
                }
            }
        }

        @Override
        public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // NO-OP
        }

        @Override
        public final void onTextChanged(CharSequence s, int start, int before, int count) {
            // NO-OP
        }

    };

    private int dipsToPix(float dips) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips,
                getResources().getDisplayMetrics());
    }
}