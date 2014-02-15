package app.philm.in.modules.library;

import com.google.common.base.Preconditions;

import app.philm.in.util.Injector;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class InjectorModule {

    private final Injector mInjector;

    public InjectorModule(Injector injector) {
        mInjector = Preconditions.checkNotNull(injector, "injector cannot be null");
    }

    @Provides
    public Injector provideMainInjector() {
        return mInjector;
    }

}
