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


import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import javax.inject.Inject;

import app.philm.in.controllers.MainController;
import app.philm.in.modules.ApplicationModule;
import app.philm.in.modules.ReceiverProvider;
import app.philm.in.modules.TaskProvider;
import app.philm.in.modules.ViewUtilProvider;
import app.philm.in.modules.library.ContextProvider;
import app.philm.in.modules.library.InjectorModule;
import app.philm.in.util.Injector;
import dagger.ObjectGraph;

public class PhilmApplication extends Application implements Injector {

    public static PhilmApplication from(Context context) {
        return (PhilmApplication) context.getApplicationContext();
    }

    @Inject MainController mMainController;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        if (AndroidConstants.STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyDeath()
                    .penaltyLog()
                    .build());
        }

        mObjectGraph = ObjectGraph.create(
                new ContextProvider(this),
                new ApplicationModule(),
                new ViewUtilProvider(),
                new TaskProvider(),
                new InjectorModule(this),
                new ReceiverProvider()
        );

        mObjectGraph.inject(this);
    }

    public MainController getMainController() {
        return mMainController;
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    @Override
    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
}
