package app.philm.in;

import com.crashlytics.android.Crashlytics;
import com.github.johnpersano.supertoasts.SuperCardToast;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.Window;

import app.philm.in.lib.Display;
import app.philm.in.lib.controllers.MainController;

public abstract class BasePhilmActivity extends FragmentActivity
        implements MainController.HostCallbacks {

    private MainController mMainController;
    private Intent mLaunchIntent;
    private Display mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        mMainController = PhilmApplication.from(this).getMainController();

        // Request Progress Bar in Action Bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Record launch intent
        mLaunchIntent = getIntent();

        // Let SuperCardToast restore itself
        SuperCardToast.onRestoreState(savedInstanceState, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDisplay = new AndroidDisplay(this, getDrawerToggle(), getDrawerLayout());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLaunchIntent = intent;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainController.attachDisplay(mDisplay);
        mMainController.setHostCallbacks(this);
        mMainController.init();

        if (mLaunchIntent != null) {
            mMainController.handleIntent(mLaunchIntent);
            mLaunchIntent = null;
        }
    }

    protected ActionBarDrawerToggle getDrawerToggle() {
        return null;
    }

    protected DrawerLayout getDrawerLayout() {
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SuperCardToast.onSaveState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        mMainController.suspend();
        mMainController.setHostCallbacks(null);
        mMainController.detachDisplay(mDisplay);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisplay = null;
    }

    @Override
    public void setAccountAuthenticatorResult(String username, String authToken,
            String authTokenType) {
        // NO-OP
    }

    protected MainController getMainController() {
        return mMainController;
    }

}
