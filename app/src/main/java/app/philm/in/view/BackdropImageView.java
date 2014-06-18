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
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

public class BackdropImageView extends PhilmImageView {

    private int mOffset;

    public BackdropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void offsetBackdrop(int offset) {
        if (offset != mOffset) {
            mOffset = offset;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOffset != 0) {
            canvas.save();
            canvas.translate(0f, mOffset);
            canvas.clipRect(0f, 0f, canvas.getWidth(), canvas.getHeight() + mOffset);
            super.onDraw(canvas);
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }
}
