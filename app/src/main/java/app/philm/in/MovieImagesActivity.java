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

public class MovieImagesActivity extends BasePhilmActivity {

    @Override
    protected Intent getParentIntent() {
        final Intent thisIntent = getIntent();

        final Intent intent = super.getParentIntent();
        intent.putExtra(Display.PARAM_ID, thisIntent.getStringExtra(Display.PARAM_ID));

        return intent;
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        if (!display.hasMainFragment()) {
            display.showMovieImagesFragment(intent.getStringExtra(Display.PARAM_ID));
        }
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_no_drawer;
    }

}
