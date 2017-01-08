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

import android.widget.TextView;

import com.google.common.base.Preconditions;

public class ViewUtils {

    public static boolean isEmpty(TextView textView) {
        Preconditions.checkNotNull(textView, "textView cannot be null");
        return TextUtils.isEmpty(textView.getText());
    }

}
