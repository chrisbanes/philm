package app.philm.in;

import android.content.Intent;

public class MovieImagesActivity extends BasePhilmActivity {

    @Override
    protected Intent getParentIntent() {
        final Intent thisIntent = getIntent();

        final Intent intent = super.getParentIntent();
        intent.setAction(Display.PHILM_ACTION_VIEW_MOVIE);
        intent.putExtra(Display.PARAM_ID, thisIntent.getStringExtra(Display.PARAM_ID));

        return intent;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_no_drawer;
    }

}
