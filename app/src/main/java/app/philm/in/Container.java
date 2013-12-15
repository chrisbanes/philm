package app.philm.in;

import com.google.common.base.Preconditions;

import com.squareup.otto.Bus;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.philm.in.trakt.Trakt;
import app.philm.in.util.Sha1;

public class Container {

    private static Container sInstance;

    public static Container getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Container(context.getApplicationContext());
        }
        return sInstance;
    }

    private final Context mContext;

    private Bus mEventBus;
    private ExecutorService mThreadPoolExecutor;
    private Trakt mTrakt;

    private Container(Context context) {
        mContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    public Bus getEventBus() {
        if (mEventBus == null) {
            mEventBus = new Bus();
        }
        return mEventBus;
    }

    public ExecutorService getExecutor() {
        if (mThreadPoolExecutor == null) {
            final int numberCores = Runtime.getRuntime().availableProcessors();
            mThreadPoolExecutor = Executors.newFixedThreadPool(numberCores * 2 + 1);
        }
        return mThreadPoolExecutor;
    }

    public Trakt getTraktClient() {
        if (mTrakt == null) {
            mTrakt = new Trakt();
            mTrakt.setApiKey(Constants.TRAKT_API_KEY);
            mTrakt.setIsDebug(Constants.DEBUG);
        }
        return mTrakt;
    }

}
