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

package app.philm.in.fragments.base;

import android.graphics.Rect;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import app.philm.in.PhilmApplication;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ColorScheme;
import app.philm.in.network.NetworkError;
import app.philm.in.view.StringManager;


public abstract class BasePhilmMovieFragment extends BasePhilmFragment
        implements MovieController.MovieUi {

    private MovieController.MovieUiCallbacks mCallbacks;
    private ColorScheme mColorScheme;

    private Toast mToast;

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        cancelToast();
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        // TODO: Implement
    }

    @Override
    public void showSecondaryLoadingProgress(boolean visible) {
        // NO-OP
    }

    @Override
    public void showError(NetworkError error) {
        showToast(StringManager.getStringResId(error));
    }

    protected final void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    protected final void showToast(@StringRes int text) {
        cancelToast();

        mToast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    protected final boolean hasCallbacks() {
        return mCallbacks != null;
    }

    protected final MovieController.MovieUiCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }

    @Override
    public void setColorScheme(ColorScheme colorScheme) {
        if (mColorScheme != colorScheme) {
            mColorScheme = colorScheme;

            onColorSchemeChanged(colorScheme);
        }
    }

    protected ColorScheme getColorScheme() {
        return mColorScheme;
    }

    protected void onColorSchemeChanged(ColorScheme colorScheme) {
    }
}
