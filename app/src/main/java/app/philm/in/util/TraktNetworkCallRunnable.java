package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.os.*;
import android.os.Process;

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

        R result = null;
        RetrofitError retrofitError = null;

        try {
            result = doTraktCall(mTraktClient);
        } catch (RetrofitError re) {
            retrofitError = re;
        }

        sHandler.post(new ResultCallback(result, retrofitError));
    }

    public abstract R doTraktCall(Trakt trakt) throws RetrofitError;

    public abstract void onSuccess(R result);

    public abstract void onError(RetrofitError re);

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
        }
    }
 }
