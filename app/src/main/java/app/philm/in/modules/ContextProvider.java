package app.philm.in.modules;

import com.google.common.base.Preconditions;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        complete = false
)
public class ContextProvider {

    private final Context mApplicationContext;

    public ContextProvider(Context context) {
        mApplicationContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @Provides
    public Context provideContext() {
        return mApplicationContext;
    }

    @Provides @Singleton
    public AccountManager provideAccountManager() {
        return AccountManager.get(mApplicationContext);
    }

    @Provides
    public File providePrivateFileDirectory() {
        return mApplicationContext.getFilesDir();
    }

    @Provides @Singleton
    public AssetManager provideAssetManager() {
        return mApplicationContext.getAssets();
    }

}
