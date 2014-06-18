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

package app.philm.in.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;

public class TintingBitmapDrawable extends BitmapDrawable {


    public static TintingBitmapDrawable createFromStateList(Resources res, int drawableId,
            int colorStateId) {
        TintingBitmapDrawable d = new TintingBitmapDrawable(res,
                BitmapFactory.decodeResource(res, drawableId, null));
        d.setTintResources(colorStateId);
        return d;
    }

    public static TintingBitmapDrawable createFromColorResource(Resources res, int drawableId,
            int colorId) {
        TintingBitmapDrawable d = new TintingBitmapDrawable(res,
                BitmapFactory.decodeResource(res, drawableId, null));
        d.setDefaultColor(res.getColor(colorId));
        return d;
    }

    private final Resources mResources;

    private ColorStateList mTint;
    private int mDefaultColor;

    public TintingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
        mResources = res;
    }

    public TintingBitmapDrawable(Resources res, int drawableId, int colorStateId) {
        this(res, BitmapFactory.decodeResource(res, drawableId, null));
        setTintResources(colorStateId);
    }

    public TintingBitmapDrawable(Resources res, int drawableId, int colorStateId, int defaultColor) {
        this(res, drawableId, colorStateId);
        setDefaultColor(defaultColor);
    }

    public void setDefaultColor(int color) {
        mDefaultColor = color;
        updateTint(getState());
    }

    public void setTintResources(int colorId) {
        ColorStateList tint = mResources.getColorStateList(colorId);
        if (tint != null) {
            setTint(tint);
        }
    }

    public void setTint(ColorStateList tint) {
        if (mTint != tint) {
            mTint = tint;
            updateTint(getState());
        }
    }

    @Override
    public boolean isStateful() {
        if (mTint != null && mTint.isStateful()) {
            return true;
        }
        return super.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mTint != null) {
            updateTint(state);
            return true;
        }
        return super.onStateChange(state);
    }

    private void updateTint(int[] state) {
        int color;

        if (mTint != null) {
            color = mTint.getColorForState(state, mDefaultColor);
        } else {
            color = mDefaultColor;
        }

        setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
