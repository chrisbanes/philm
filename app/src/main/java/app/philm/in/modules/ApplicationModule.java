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

package app.philm.in.modules;


import app.philm.in.PhilmApplication;
import app.philm.in.modules.library.AccountsProvider;
import app.philm.in.modules.library.InjectorModule;
import app.philm.in.modules.library.NetworkProvider;
import app.philm.in.modules.library.PersistenceProvider;
import app.philm.in.modules.library.StateProvider;
import app.philm.in.modules.library.UtilProvider;
import dagger.Module;

@Module(
        injects = PhilmApplication.class,
        includes = {
                UtilProvider.class,
                AccountsProvider.class,
                NetworkProvider.class,
                StateProvider.class,
                PersistenceProvider.class,
                InjectorModule.class
        }
)
public class ApplicationModule {
}
