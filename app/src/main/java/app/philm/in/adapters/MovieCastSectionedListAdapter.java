package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmCast;
import app.philm.in.view.PhilmImageView;

public class MovieCastSectionedListAdapter extends BasePhilmSectionedListAdapter<PhilmCast> {

    private static final String LOG_TAG = MovieCastSectionedListAdapter.class.getSimpleName();

    public MovieCastSectionedListAdapter(Activity activity) {
        super(activity, R.layout.item_list_movie_cast, R.layout.item_list_movie_section_header);
    }

    @Override
    protected void bindView(int position, View view, ListItem<PhilmCast> item) {
        PhilmCast castMember = item.getItem();

        final TextView nameTextView = (TextView) view.findViewById(R.id.textview_name);
        nameTextView.setText(castMember.getName());

        final TextView characterTextView =
                (TextView) view.findViewById(R.id.textview_character);
        characterTextView.setText(castMember.getCharacter());

        final PhilmImageView imageView =
                (PhilmImageView) view.findViewById(R.id.imageview_profile);
        imageView.loadProfileUrl(castMember);
    }
}
