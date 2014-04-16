package app.philm.in;

import com.crashlytics.android.Crashlytics;
import com.github.johnpersano.supertoasts.SuperCardToast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.Window;

import app.philm.in.lib.controllers.MainController;

public abstract class BasePhilmActivity extends FragmentActivity
        implements MainController.HostCallbacks {

    private MainController mMainController;
    private Intent mLaunchIntent;

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLaunchIntent = intent;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainController.setDisplay(new AndroidDisplay(this, getDrawerToggle(), getDrawerLayout()));
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
    protected void onPause() {
        mMainController.suspend();
        mMainController.setHostCallbacks(null);
        mMainController.setDisplay(null);
        super.onPause();
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
