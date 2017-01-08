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

package app.philm.in.network;

import com.jakewharton.retrofit.Ok3Client;
import com.jakewharton.trakt.Trakt;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit.RestAdapter;

public class PhilmTrakt extends Trakt {

    private static final String TAG = "PhilmTrakt";

    private static final int CACHE_SIZE = 10 * 1024 * 1024;

    private final File mCacheLocation;

    public PhilmTrakt(File cacheLocation) {
        mCacheLocation = cacheLocation;
    }

    @Override
    protected RestAdapter.Builder newRestAdapterBuilder() {
        RestAdapter.Builder b = super.newRestAdapterBuilder();

        if (mCacheLocation != null) {
            File cacheDir = new File(mCacheLocation, "trakt_requests");
            Cache cache = new Cache(cacheDir, CACHE_SIZE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();

            b.setClient(new Ok3Client(client));
        }

        return b;
    }

}
