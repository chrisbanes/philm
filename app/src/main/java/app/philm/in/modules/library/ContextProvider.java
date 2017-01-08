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

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.res.AssetManager;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.inject.Singleton;

import app.philm.in.qualifiers.ApplicationContext;
import app.philm.in.qualifiers.FilesDirectory;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class ContextProvider {

    private final Context mApplicationContext;

    public ContextProvider(Context context) {
        mApplicationContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @Provides @ApplicationContext
    public Context provideApplicationContext() {
        return mApplicationContext;
    }

    @Provides @Singleton
    public AccountManager provideAccountManager() {
        return AccountManager.get(mApplicationContext);
    }

    @Provides @FilesDirectory
    public File providePrivateFileDirectory() {
        return mApplicationContext.getFilesDir();
    }

    @Provides @Singleton
    public AssetManager provideAssetManager() {
        return mApplicationContext.getAssets();
    }

    @Provides @Singleton
    public AlarmManager provideAlarmManager() {
        return (AlarmManager) mApplicationContext.getSystemService(Context.ALARM_SERVICE);
    }

}
