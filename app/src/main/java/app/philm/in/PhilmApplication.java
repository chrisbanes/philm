package app.philm.in;


import android.app.Application;
import android.content.Context;

import javax.inject.Inject;

import app.philm.in.controllers.MainController;
import app.philm.in.modules.ApplicationModule;
import app.philm.in.modules.ContextProvider;
import app.philm.in.modules.ViewUtilProvider;
import dagger.ObjectGraph;

public class PhilmApplication extends Application {

    public static PhilmApplication from(Context context) {
        return (PhilmApplication) context.getApplicationContext();
    }

    @Inject MainController mMainController;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        mObjectGraph = ObjectGraph.create(
                new ContextProvider(this),
                new ApplicationModule(),
                new ViewUtilProvider()
        );

        mObjectGraph.inject(this);
    }

    public MainController getMainController() {
        return mMainController;
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }
}
