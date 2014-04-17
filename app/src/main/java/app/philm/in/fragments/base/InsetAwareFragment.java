package app.philm.in.fragments.base;

import android.graphics.Rect;
import android.support.v4.app.Fragment;

import java.util.List;

import app.philm.in.BasePhilmActivity;
import app.philm.in.lib.util.PhilmCollections;

public abstract class InsetAwareFragment extends Fragment
        implements BasePhilmActivity.OnActivityInsetsCallback {

    private final Rect mInsets = new Rect();
    private Rect mAdditionalInsets;

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BasePhilmActivity) {
            ((BasePhilmActivity) getActivity()).addInsetChangedCallback(this);
        }
    }

    @Override
    public void onPause() {
        if (getActivity() instanceof BasePhilmActivity) {
            BasePhilmActivity activity = ((BasePhilmActivity) getActivity());
            activity.removeInsetChangedCallback(this);
            activity.resetInsets();
        }
        super.onPause();
    }

    @Override
    public final void onInsetsChanged(Rect insets) {
        mInsets.set(insets);
        doPopulateInsets();
    }

    protected void populateInsets(Rect insets) {
    }

    public void setTopInsetAlpha(float alpha) {
        if (getActivity() instanceof BasePhilmActivity) {
            ((BasePhilmActivity) getActivity()).setInsetTopAlpha(alpha);
        }
    }

    public void setAdditionalInsets(final Rect rect) {
        mAdditionalInsets = rect;
        doPopulateInsets();
    }

    protected void propogateAdditionalInsetsToChildren(final Rect rect) {
        final List<Fragment> children = getChildFragmentManager().getFragments();
        if (!PhilmCollections.isEmpty(children)) {
            for (final Fragment fragment : children) {
                if (fragment instanceof InsetAwareFragment) {
                    ((InsetAwareFragment) fragment).setAdditionalInsets(rect);
                }
            }
        }
    }

    private void doPopulateInsets() {
        Rect insetsToPopulate;

        if (mAdditionalInsets == null) {
            insetsToPopulate = mInsets;
        } else {
            final Rect tempRect = new Rect(mInsets);
            tempRect.left += mAdditionalInsets.left;
            tempRect.top += mAdditionalInsets.top;
            tempRect.right += mAdditionalInsets.right;
            tempRect.bottom += mAdditionalInsets.bottom;

            insetsToPopulate = tempRect;
        }

        if (getView() != null) {
            populateInsets(insetsToPopulate);
        }
    }
}
