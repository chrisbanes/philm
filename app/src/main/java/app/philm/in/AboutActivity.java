package app.philm.in;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

public class AboutActivity extends BasePhilmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        if (!display.hasMainFragment()) {
            display.showAboutFragment();
        }
    }
}
