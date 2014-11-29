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
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.util.TextUtils;
import app.philm.in.view.PhilmImageView;

public class PersonCreditSectionedListAdapter
        extends BasePhilmSectionedListAdapter<PhilmPersonCredit> {

    private static final String LOG_TAG = PersonCreditSectionedListAdapter.class.getSimpleName();

    @Inject DateFormat mMediumDateFormatter;
    private final Date mDate;

    public PersonCreditSectionedListAdapter(Activity activity) {
        super(activity, R.layout.item_list_3line, R.layout.item_list_movie_section_header);
        mDate = new Date();
        PhilmApplication.from(activity).inject(this);
    }

    @Override
    protected void bindView(int position, View view, ListItem<PhilmPersonCredit> item) {
        PhilmPersonCredit credit = item.getListItem();

        final TextView nameTextView = (TextView) view.findViewById(R.id.textview_title);
        nameTextView.setText(credit.getTitle());

        final TextView characterTextView = (TextView) view.findViewById(R.id.textview_subtitle_1);
        if (TextUtils.isEmpty(credit.getJob())) {
            characterTextView.setVisibility(View.GONE);
        } else {
            characterTextView.setVisibility(View.VISIBLE);
            characterTextView.setText(credit.getJob());
        }

        final TextView release = (TextView) view.findViewById(R.id.textview_subtitle_2);
        mDate.setTime(credit.getReleaseDate());
        release.setText(mActivity.getString(R.string.movie_release_date,
                mMediumDateFormatter.format(mDate)));

        final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
        imageView.loadPoster(credit);
    }
}
