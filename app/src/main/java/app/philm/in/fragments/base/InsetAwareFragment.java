package app.philm.in.fragments.base;

import android.graphics.Rect;
import android.support.v4.app.Fragment;

import app.philm.in.PhilmActivity;

public class InsetAwareFragment extends Fragment
        implements PhilmActivity.OnActivityInsetsCallback {

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof PhilmActivity) {
            ((PhilmActivity) getActivity()).addInsetChangedCallback(this);
        }
    }

    @Override
    public void onPause() {
        if (getActivity() instanceof PhilmActivity) {
            PhilmActivity activity = ((PhilmActivity) getActivity());
            activity.setInsetTopAlpha(1.0f);
            activity.removeInsetChangedCallback(this);
        }
        super.onPause();
    }

    @Override
    public void onInsetsChanged(Rect insets) {
    }

    public void setTopInsetAlpha(float alpha) {
        if (getActivity() instanceof PhilmActivity) {
            ((PhilmActivity) getActivity()).setInsetTopAlpha(alpha);
        }
    }
}
