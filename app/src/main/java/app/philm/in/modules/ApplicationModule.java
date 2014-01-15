package app.philm.in.modules;


import app.philm.in.PhilmApplication;
import dagger.Module;

@Module(
        injects = PhilmApplication.class,
        includes = {
                UtilProvider.class,
                AccountsProvider.class,
                NetworkProvider.class,
                StateProvider.class,
                PersistenceProvider.class
        }
)
public class ApplicationModule {
}
