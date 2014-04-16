package app.philm.in;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashSet;

import app.philm.in.lib.controllers.MainController;
import app.philm.in.lib.util.IntUtils;
import app.philm.in.lib.util.PhilmCollections;
import app.philm.in.view.InsetDrawerLayout;

public abstract class PhilmActivity extends BasePhilmActivity
        implements InsetDrawerLayout.OnInsetsCallback {

    public static interface OnActivityInsetsCallback {
        public void onInsetsChanged(Rect insets);
    }

    private ActionBarDrawerToggle mDrawerToggle;

    private HashSet<OnActivityInsetsCallback> mInsetCallbacks;
    private Rect mInsets;

    private View mCardContainer;
    private InsetDrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCardContainer = findViewById(R.id.card_container);

        mDrawerLayout = (InsetDrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setOnInsetsCallback(this);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open_content_desc, R.string.drawer_closed_content_desc);

            mDrawerLayout.setDrawerListener(mDrawerToggle);

            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    public void addInsetChangedCallback(OnActivityInsetsCallback callback) {
        if (mInsetCallbacks == null) {
            mInsetCallbacks = new HashSet<>();
        }
        mInsetCallbacks.add(callback);

        if (mInsets != null) {
            callback.onInsetsChanged(mInsets);
        }
    }

    public void removeInsetChangedCallback(OnActivityInsetsCallback callback) {
        if (mInsetCallbacks != null) {
            mInsetCallbacks.remove(callback);
        }
    }

    @Override
    public void onInsetsChanged(Rect insets) {
        mInsets = insets;

        mCardContainer.setPadding(0, insets.top, 0, 0);

        if (!PhilmCollections.isEmpty(mInsetCallbacks)) {
            for (OnActivityInsetsCallback callback : mInsetCallbacks) {
                callback.onInsetsChanged(insets);
            }
        }
    }

    public void setInsetTopAlpha(float alpha) {
        mDrawerLayout.setTopInsetAlpha(IntUtils.anchor(Math.round(alpha * 255), 0, 255));
    }

    public void resetInsets() {
        setInsetTopAlpha(255);
    }

}
