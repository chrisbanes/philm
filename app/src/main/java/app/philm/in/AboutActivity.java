package app.philm.in;

import android.os.Bundle;

public class AboutActivity extends BasePhilmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_about;
    }
}
