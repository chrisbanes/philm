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

package app.philm.in.modules.library;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import app.philm.in.AndroidStringFetcher;
import app.philm.in.qualifiers.ApplicationContext;
import app.philm.in.qualifiers.ForDatabase;
import app.philm.in.qualifiers.GeneralPurpose;
import app.philm.in.util.AndroidCountryProvider;
import app.philm.in.util.AndroidLogger;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmBackgroundExecutor;
import app.philm.in.util.StringFetcher;
import dagger.Module;
import dagger.Provides;

@Module(
        includes = ContextProvider.class,
        library = true
)
public class UtilProvider {

    @Provides @Singleton
    public Bus provideEventBus() {
        return new Bus();
    }

    @Provides @Singleton
    public Logger provideLogger() {
        return new AndroidLogger();
    }

    @Provides @Singleton
    public ImageHelper provideImageHelper() {
        return new ImageHelper();
    }

    @Provides @Singleton
    public CountryProvider provideCountryProvider(@ApplicationContext Context context) {
        return new AndroidCountryProvider(context);
    }

    @Provides @Singleton @GeneralPurpose
    public BackgroundExecutor provideMultiThreadExecutor() {
        final int numberCores = Runtime.getRuntime().availableProcessors();
        return new PhilmBackgroundExecutor(Executors.newFixedThreadPool(numberCores * 2 + 1));
    }

    @Provides @Singleton @ForDatabase
    public BackgroundExecutor provideDatabaseThreadExecutor() {
        return new PhilmBackgroundExecutor(Executors.newSingleThreadExecutor());
    }

    @Provides @Singleton
    public StringFetcher provideStringFetcher(@ApplicationContext Context context) {
        return new AndroidStringFetcher(context);
    }

}
