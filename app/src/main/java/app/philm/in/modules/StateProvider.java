package app.philm.in.modules;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import app.philm.in.state.ApplicationState;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        includes = UtilProvider.class
)
public class StateProvider {

    @Provides @Singleton
    public ApplicationState provideApplicationState(Bus bus) {
        return new ApplicationState(bus);
    }

    @Provides
    public MoviesState provideMovieState(ApplicationState state) {
        return state;
    }

    @Provides
    public UserState provideUserState(ApplicationState state) {
        return state;
    }

}
