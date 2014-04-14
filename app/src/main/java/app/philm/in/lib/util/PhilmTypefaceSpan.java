package app.philm.in.lib.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import javax.inject.Inject;

import app.philm.in.PhilmApplication;
import app.philm.in.view.FontTextView;

public class PhilmTypefaceSpan extends MetricAffectingSpan {

    private final Typeface mTypeface;
    private final int mFontId;

    private boolean mUpdateColor;
    private int mTextColor;

    @Inject TypefaceManager mTypefaceManager;

    public PhilmTypefaceSpan(Context context, int font) {
        PhilmApplication.from(context).inject(this);

        mFontId = font;
        mTypeface = FontTextView.getFont(mTypefaceManager, mFontId);
    }

    public PhilmTypefaceSpan(Context context, int font, int textColor) {
        PhilmApplication.from(context).inject(this);

        mFontId = font;
        mTypeface = FontTextView.getFont(mTypefaceManager, mFontId);

        mTextColor = textColor;
        mUpdateColor = true;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setTypeface(mTypeface);
        if (mUpdateColor) {
            ds.setColor(mTextColor);
        }
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.setTypeface(mTypeface);
    }
}