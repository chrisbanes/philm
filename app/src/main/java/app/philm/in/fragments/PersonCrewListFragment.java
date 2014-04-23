package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.text.TextUtils;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePersonCreditListFragment;

public class PersonCrewListFragment extends BasePersonCreditListFragment {

    private static final String KEY_QUERY_PERSON_ID = "person_id";

    public static PersonCrewListFragment create(String personId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(personId), "personId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_PERSON_ID, personId);

        PersonCrewListFragment fragment = new PersonCrewListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.PERSON_CREDITS_CREW;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_PERSON_ID);
    }
}
