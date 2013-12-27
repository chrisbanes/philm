package app.philm.in.network;

import com.google.common.base.Preconditions;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import app.philm.in.Constants;
import app.philm.in.trakt.Trakt;
import retrofit.RetrofitError;

public abstract class TraktNetworkCallRunnable<R> implements Runnable {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private final Trakt mTraktClient;

    public TraktNetworkCallRunnable(Trakt traktClient) {
        mTraktClient = Preconditions.checkNotNull(traktClient, "traktClient cannot be null");
    }

    @Override
    public final void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                onPreTraktCall();
            }
        });

        R result = null;
        RetrofitError retrofitError = null;

        try {
            result = doTraktCall(mTraktClient);
        } catch (RetrofitError re) {
            retrofitError = re;
            if (Constants.DEBUG) {
                Log.d(getClass().getSimpleName(), "Error while completing network call", re);
            }
        }

        sHandler.post(new ResultCallback(result, retrofitError));
    }

    public void onPreTraktCall() {}

    public abstract R doTraktCall(Trakt trakt) throws RetrofitError;

    public abstract void onSuccess(R result);

    public abstract void onError(RetrofitError re);

    public void onFinished() {}

    private class ResultCallback implements Runnable {
        private final R mResult;
        private final RetrofitError mRetrofitError;

        private ResultCallback(R result, RetrofitError retrofitError) {
            mResult = result;
            mRetrofitError = retrofitError;
        }

        @Override
        public void run() {
            if (mResult != null) {
                onSuccess(mResult);
            } else if (mRetrofitError != null) {
                onError(mRetrofitError);
            }

            onFinished();
        }
    }
 }
