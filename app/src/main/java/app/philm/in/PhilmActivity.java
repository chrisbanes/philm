package app.philm.in;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashSet;

import app.philm.in.util.IntUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.InsetFrameLayout;

public class PhilmActivity extends BasePhilmActivity implements InsetFrameLayout.OnInsetsCallback {

    public static interface OnActivityInsetsCallback {
        public void onInsetsChanged(Rect insets);
    }

    private ActionBarDrawerToggle mDrawerToggle;

    private HashSet<OnActivityInsetsCallback> mInsetCallbacks;
    private Rect mInsets;
    private InsetFrameLayout mInsetFrameLayout;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mInsetFrameLayout = (InsetFrameLayout) findViewById(R.id.inset_fl);
        mInsetFrameLayout.setOnInsetsCallback(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open_content_desc, R.string.drawer_closed_content_desc);

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
            mInsetCallbacks = new HashSet<OnActivityInsetsCallback>();
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

        if (!PhilmCollections.isEmpty(mInsetCallbacks)) {
            for (OnActivityInsetsCallback callback : mInsetCallbacks) {
                callback.onInsetsChanged(insets);
            }
        }
    }

    public void setInsetTopAlpha(float alpha) {
        mInsetFrameLayout.setTopInsetAlpha(IntUtils.anchor(Math.round(alpha * 255), 0, 255));
    }

    public void setInsetBottomAlpha(float alpha) {
        mInsetFrameLayout.setBottomInsetAlpha(IntUtils.anchor(Math.round(alpha * 255), 0, 255));
    }

    public void resetAlpha() {
        setInsetTopAlpha(255);
        setInsetBottomAlpha(0);
    }
}
