package app.philm.in.modules;

import com.squareup.otto.Bus;

import android.content.Context;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import app.philm.in.modules.qualifiers.ApplicationContext;
import app.philm.in.modules.qualifiers.ForDatabase;
import app.philm.in.modules.qualifiers.GeneralPurpose;
import app.philm.in.util.AndroidCountryProvider;
import app.philm.in.util.AndroidLogger;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.CountryProvider;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmBackgroundExecutor;
import dagger.Module;
import dagger.Provides;

@Module(
        includes = ContextProvider.class,
        complete = false,
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

}
