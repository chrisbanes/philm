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

package app.philm.in.util;

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

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mUpdateColor = true;
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.setTypeface(mTypeface);
    }
}