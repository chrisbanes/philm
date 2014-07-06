/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in;

import com.crashlytics.android.Crashlytics;
import com.github.johnpersano.supertoasts.SuperCardToast;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.HashSet;

import app.philm.in.controllers.MainController;
import app.philm.in.util.IntUtils;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.InsetFrameLayout;

public abstract class BasePhilmActivity extends FragmentActivity
        implements MainController.HostCallbacks, InsetFrameLayout.OnInsetsCallback {

    private MainController mMainController;
    private Display mDisplay;

    private HashSet<OnActivityInsetsCallback> mInsetCallbacks;

    private Rect mInsets;

    private View mCardContainer;

    private InsetFrameLayout mInsetFrameLayout;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

        // Request Progress Bar in Action Bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(getContentViewLayoutId());

        mCardContainer = findViewById(R.id.card_container);

        mInsetFrameLayout = (InsetFrameLayout) findViewById(R.id.fl_insets);
        if (mInsetFrameLayout != null) {
            mInsetFrameLayout.setOnInsetsCallback(this);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Let SuperCardToast restore itself
        SuperCardToast.onRestoreState(savedInstanceState, this);

        mMainController = PhilmApplication.from(this).getMainController();

        mDisplay = new AndroidDisplay(this, mDrawerLayout, mInsetFrameLayout);

        handleIntent(getIntent(), getDisplay());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, getDisplay());
    }

    protected void handleIntent(Intent intent, Display display) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainController.attachDisplay(mDisplay);
        mMainController.setHostCallbacks(this);
        mMainController.init();
    }

    @Override
    public void setAccountAuthenticatorResult(String username, String authToken,
            String authTokenType) {
        // NO-OP
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getMainController().onHomeButtonPressed()) {
                    return true;
                }
                if (navigateUp()) {
                    return true;
                }
                break;

            case R.id.menu_about:
                if (getMainController().onAboutButtonPressed()) {
                    return true;
                }
                break;

            case R.id.menu_settings:
                if (getMainController().onSettingsButtonPressed()) {
                    return true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
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
        mMainController.detachDisplay(mDisplay);
        super.onPause();
    }

    protected boolean navigateUp() {
        final Intent intent = getParentIntent();
        if (intent != null) {
            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return false;
    }

    protected Intent getParentIntent() {
        return NavUtils.getParentActivityIntent(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisplay = null;
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

        if (mCardContainer != null) {
            mCardContainer.setPadding(0, insets.top, 0, 0);
        }

        if (!PhilmCollections.isEmpty(mInsetCallbacks)) {
            for (OnActivityInsetsCallback callback : mInsetCallbacks) {
                callback.onInsetsChanged(insets);
            }
        }
    }

    public void setInsetTopAlpha(float alpha) {
        if (mInsetFrameLayout != null) {
            mInsetFrameLayout.setTopInsetAlpha(IntUtils.anchor(Math.round(alpha * 255), 0, 255));
        }
    }

    public void resetInsets() {
        setInsetTopAlpha(255);
    }

    public static interface OnActivityInsetsCallback {
        public void onInsetsChanged(Rect insets);
    }

    protected DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected final MainController getMainController() {
        return mMainController;
    }

    protected int getContentViewLayoutId() {
        return R.layout.activity_main;
    }

    protected Display getDisplay() {
        return mDisplay;
    }

}
