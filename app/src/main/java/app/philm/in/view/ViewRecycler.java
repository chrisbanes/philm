package app.philm.in.view;

import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

public class ViewRecycler {

    private final ViewGroup mViewGroup;
    private final ArrayList<View> mRecycledViews;

    public ViewRecycler(ViewGroup viewGroup) {
        mViewGroup = Preconditions.checkNotNull(viewGroup, "viewGroup cannot be null");
        mRecycledViews = new ArrayList<View>();
    }


    public void recycleViews() {
        if (mViewGroup.getChildCount() > 0) {
            for (int i = 0, z = mViewGroup.getChildCount() ; i < z ; i++) {
                mRecycledViews.add(mViewGroup.getChildAt(i));
            }
        }
        mViewGroup.removeAllViews();
    }

    public View getRecycledView() {
        if (!mRecycledViews.isEmpty()) {
            final int lastIndex = mRecycledViews.size() - 1;

            View view = mRecycledViews.get(lastIndex);
            mRecycledViews.remove(lastIndex);

            return view;
        }
        return null;
    }

    public void clearRecycledViews() {
        mRecycledViews.clear();
    }
}
