package app.philm.in.modules.library;

import android.accounts.AccountManager;

import javax.inject.Singleton;

import app.philm.in.account.AndroidAccountManager;
import app.philm.in.lib.accounts.PhilmAccountManager;
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
