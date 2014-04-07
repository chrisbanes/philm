package app.philm.in.modules;

import app.philm.in.AlarmReceiver;
import app.philm.in.modules.library.ContextProvider;
import app.philm.in.modules.library.UtilProvider;
import dagger.Module;

@Module(
        injects = {
                AlarmReceiver.class
        },
        includes = {
                ContextProvider.class,
                UtilProvider.class
        }
)
public class ReceiverProvider {
}
