package app.philm.in;

import com.google.common.base.Preconditions;

import android.app.Activity;

import app.philm.in.fragments.LibraryListFragment;
import app.philm.in.fragments.LoginFragment;

public class Display {

    private final Activity mActivity;

    public Display(Activity activity) {
        mActivity = Preconditions.checkNotNull(activity, "activity cannot be null");
    }

    public void showLibrary() {
        LibraryListFragment fragment = new LibraryListFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showTrending() {
        LibraryListFragment fragment = new LibraryListFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showLogin() {
        LoginFragment fragment = new LoginFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

}
