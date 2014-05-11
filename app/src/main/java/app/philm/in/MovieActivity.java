package app.philm.in;

import android.content.Intent;

public class MovieActivity extends BasePhilmActivity {

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_no_drawer;
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        if (!display.hasMainFragment()) {
            display.showMovieDetailFragment(intent.getStringExtra(Display.PARAM_ID));
        }
    }
}
