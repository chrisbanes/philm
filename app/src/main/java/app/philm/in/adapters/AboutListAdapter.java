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

package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.base.Objects;

import java.util.List;

import app.philm.in.R;
import app.philm.in.controllers.AboutController;
import app.philm.in.view.StringManager;

public class AboutListAdapter extends BaseAdapter {

    private static final String LOG_TAG = AboutListAdapter.class.getSimpleName();

    private final Activity mActivity;

    private List<AboutController.AboutItem> mItems;

    public AboutListAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setItems(List<AboutController.AboutItem> items) {
        if (!Objects.equal(items, mItems)) {
            mItems = items;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public AboutController.AboutItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final AboutController.AboutItem item = getItem(position);

        View view = convertView;
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.item_about, viewGroup, false);
        }

        ((TextView) view.findViewById(android.R.id.text1))
                .setText(StringManager.getTitleResId(item));
        ((TextView) view.findViewById(android.R.id.text2))
                .setText(StringManager.getSubtitle(mActivity, item));

        return view;
    }
}
