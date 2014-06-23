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

import com.google.common.base.Preconditions;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ViewRecycler {

    private final ViewGroup mViewGroup;
    private final ArrayList<View> mRecycledViews;

    public ViewRecycler(ViewGroup viewGroup) {
        mViewGroup = Preconditions.checkNotNull(viewGroup, "viewGroup cannot be null");
        mRecycledViews = new ArrayList<>();
    }

    public void recycleViews() {
        if (mViewGroup.getChildCount() > 0) {
            for (int i = 0, z = mViewGroup.getChildCount() ; i < z ; i++) {
                mRecycledViews.add(mViewGroup.getChildAt(i));
            }
            mViewGroup.removeAllViews();
        }
    }

    public View getRecycledView() {
        if (!mRecycledViews.isEmpty()) {
            final int lastIndex = mRecycledViews.size() - 1;

            View view = mRecycledViews.get(lastIndex);
            mRecycledViews.remove(lastIndex);

            return view;
        }
        return null;
    }

    public void clearRecycledViews() {
        mRecycledViews.clear();
    }
}
