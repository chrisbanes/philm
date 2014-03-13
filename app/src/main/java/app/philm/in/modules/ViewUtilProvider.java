package app.philm.in.modules;

import android.content.Context;
import android.content.res.AssetManager;

import java.text.DateFormat;

import javax.inject.Singleton;

import app.philm.in.adapters.MovieSectionedListAdapter;
import app.philm.in.fragments.MovieDetailFragment;
import app.philm.in.modules.library.ContextProvider;
import app.philm.in.modules.library.UtilProvider;
import app.philm.in.modules.qualifiers.ApplicationContext;
import app.philm.in.util.FlagUrlProvider;
import app.philm.in.util.TypefaceManager;
import app.philm.in.view.FontTextView;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.RatingCircleView;
import dagger.Module;
import dagger.Provides;

@Module(
        includes =  {
                ContextProvider.class,
                UtilProvider.class
        },
        injects = {
                FontTextView.class,
                PhilmImageView.class,
                RatingCircleView.class,
                MovieSectionedListAdapter.class,
                MovieDetailFragment.class
        }
)
public class ViewUtilProvider {

    @Provides @Singleton
    public TypefaceManager provideTypefaceManager(AssetManager assetManager) {
        return new TypefaceManager(assetManager);
    }

    @Provides @Singleton
    public DateFormat provideMediumDateFormat(@ApplicationContext Context context) {
        return android.text.format.DateFormat.getMediumDateFormat(context);
    }

    @Provides @Singleton
    public FlagUrlProvider getFlagUrlProvider() {
        return new FlagUrlProvider();
    }

}
