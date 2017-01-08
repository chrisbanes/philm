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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;

public class DrawableTintUtils {

    public static Drawable createFromColorRes(@NonNull Context context,
            @DrawableRes int drawableId, @ColorRes int colorId) {
        Drawable d = ContextCompat.getDrawable(context, drawableId);
        d = DrawableCompat.wrap(d.mutate());

        ColorStateList tint = AppCompatResources.getColorStateList(context, colorId);
        DrawableCompat.setTintList(d, tint);
        return d;
    }
}
