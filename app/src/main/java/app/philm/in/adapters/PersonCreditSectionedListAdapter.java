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
        PhilmPersonCredit credit = item.getItem();

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
