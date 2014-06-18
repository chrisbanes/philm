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

package app.philm.in;

import com.google.common.base.Preconditions;

import android.content.Context;

import app.philm.in.util.StringFetcher;

public class AndroidStringFetcher implements StringFetcher {

    private final Context mContext;

    public AndroidStringFetcher(Context context) {
        mContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @Override
    public String getString(int id) {
        return mContext.getString(id);
    }

    @Override
    public String getString(int id, Object... format) {
        return mContext.getString(id, format);
    }
}
