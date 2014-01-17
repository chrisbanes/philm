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
