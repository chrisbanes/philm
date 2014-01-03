package app.philm.in;

import android.os.Bundle;

public class AboutActivity extends BasePhilmActivity {

    public static final String ACTION_ABOUT = "philm.intent.action.ABOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

}
