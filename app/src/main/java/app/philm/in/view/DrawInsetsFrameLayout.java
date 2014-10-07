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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import app.philm.in.R;

public class DrawInsetsFrameLayout extends FrameLayout {

    private final Rect mInsets = new Rect();
    private final Paint mPaint = new Paint();

    public DrawInsetsFrameLayout(Context context) {
        this(context, null);
    }

    public DrawInsetsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawInsetsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawInsetsFrameLayout);
        mPaint.setColor(a.getColor(R.styleable.DrawInsetsFrameLayout_insetBackground,
                getResources().getColor(android.R.color.transparent)));
        a.recycle();

        setWillNotDraw(false);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (!Objects.equal(mInsets, insets)) {
            mInsets.set(insets);
            invalidate();
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mInsets.top != 0) {
            canvas.drawRect(0, 0, canvas.getWidth(), mInsets.top, mPaint);
        }
        super.draw(canvas);
    }
}
