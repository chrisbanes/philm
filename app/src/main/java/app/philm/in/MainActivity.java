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

import android.content.Intent;
import android.view.Menu;

import app.philm.in.controllers.MainController;

public class MainActivity extends BasePhilmActivity implements MainController.MainUi {

    private MainController.MainControllerUiCallbacks mUiCallbacks;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMainController().attachUi(this);
    }

    @Override
    protected void onPause() {
        getMainController().detachUi(this);
        super.onPause();
    }

    @Override
    public void showLoginPrompt() {
        // TODO: Show delayed prompt
        //if (mUiCallbacks != null) {
        //    mUiCallbacks.setShownLoginPrompt();
        //}
    }

    @Override
    public void setCallbacks(MainController.MainControllerUiCallbacks callbacks) {
        mUiCallbacks = callbacks;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            if (!display.hasMainFragment()) {
                getMainController().setSelectedSideMenuItem(MainController.SideMenuItem.DISCOVER);
                display.showDiscover();
            }
        }
    }
}
