package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.lib.model.ListItem;
import app.philm.in.lib.model.PhilmMovie;
import app.philm.in.view.PhilmImageView;

public class MovieSectionedListAdapter extends BasePhilmSectionedListAdapter<PhilmMovie> {

    private static final String LOG_TAG = MovieSectionedListAdapter.class.getSimpleName();

    @Inject DateFormat mMediumDateFormatter;
    private final Date mDate;

    public MovieSectionedListAdapter(Activity activity) {
        super(activity, R.layout.item_list_movie, R.layout.item_list_movie_section_header);
        mDate = new Date();
        PhilmApplication.from(activity).inject(this);
    }

    @Override
    protected void bindView(int position, View view, ListItem<PhilmMovie> item) {
        PhilmMovie movie = item.getItem();

        final TextView title = (TextView) view.findViewById(R.id.textview_title);
        title.setText(mActivity.getString(R.string.movie_title_year,
                movie.getTitle(), movie.getYear()));

        final TextView ratingTextView = (TextView) view.findViewById(R.id.textview_subtitle_1);
        ratingTextView.setText(mActivity.getString(R.string.movie_rating_votes,
                movie.getAverageRatingPercent(), movie.getAverageRatingVotes()));

        final TextView release = (TextView) view.findViewById(R.id.textview_subtitle_2);
        mDate.setTime(movie.getReleasedTime());
        release.setText(mActivity.getString(R.string.movie_release_date,
                mMediumDateFormatter.format(mDate)));

        final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
        imageView.loadPoster(movie);
    }
}
