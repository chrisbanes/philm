package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmPerson;
import app.philm.in.view.PhilmImageView;

public class PersonSectionedListAdapter extends BasePhilmSectionedListAdapter<PhilmPerson> {

    private static final String LOG_TAG = PersonSectionedListAdapter.class.getSimpleName();

    public PersonSectionedListAdapter(Activity activity) {
        super(activity, R.layout.item_list_movie_sml, R.layout.item_list_movie_section_header);
    }

    @Override
    protected void bindView(int position, View view, ListItem<PhilmPerson> item) {
        PhilmPerson person = item.getItem();

        final TextView nameTextView = (TextView) view.findViewById(R.id.textview_title);
        nameTextView.setText(person.getName());

        final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
        imageView.loadProfileUrl(person);
    }
}
