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

import javax.inject.Singleton;

import app.philm.in.account.AndroidAccountManager;
import app.philm.in.accounts.PhilmAccountManager;
import dagger.Module;
import dagger.Provides;

@Module(
        includes = ContextProvider.class,
        library = true
)
public class AccountsProvider {

    @Provides @Singleton
    public PhilmAccountManager provideAccountManager(AccountManager androidAccountManager) {
        return new AndroidAccountManager(androidAccountManager);
    }

}
