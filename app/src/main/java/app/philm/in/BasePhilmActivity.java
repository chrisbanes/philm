package app.philm.in;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import app.philm.in.controllers.MainController;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import com.crashlytics.android.Crashlytics;

public abstract class BasePhilmActivity extends Activity implements MainController.HostCallbacks {

    private MainController mMainController;
    private Intent mLaunchIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        mMainController = PhilmApplication.from(this).getMainController();
        mLaunchIntent = getIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLaunchIntent = intent;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mMainController.setDisplay(new AndroidDisplay(this, getDrawerToggle()));
        mMainController.setHostCallbacks(this);
        mMainController.init();

        if (mLaunchIntent != null) {
            mMainController.handleIntent(mLaunchIntent.getAction());
            mLaunchIntent = null;
        }
    }

    protected ActionBarDrawerToggle getDrawerToggle() {
        return null;
    }

    @Override
    protected void onPause() {
        mMainController.suspend();
        mMainController.setHostCallbacks(null);
        mMainController.setDisplay(null);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMainController.onActivityMenuItemSelected(item.getItemId())) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setAccountAuthenticatorResult(Bundle bundle) {
        // NO-OP
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }
}
