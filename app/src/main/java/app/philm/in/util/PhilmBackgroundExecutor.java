package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.os.*;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import app.philm.in.Constants;
import app.philm.in.network.BackgroundCallRunnable;
import app.philm.in.network.TraktNetworkCallRunnable;
import retrofit.RetrofitError;

public class PhilmBackgroundExecutor implements BackgroundExecutor {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private final ExecutorService mExecutorService;

    public PhilmBackgroundExecutor(ExecutorService executorService) {
        mExecutorService = Preconditions.checkNotNull(executorService,
                "executorService cannot be null");
    }

    @Override
    public <R> void execute(TraktNetworkCallRunnable<R> runnable) {
        mExecutorService.execute(new TraktNetworkRunner<R>(runnable));
    }

    @Override
    public <R> void execute(BackgroundCallRunnable<R> runnable) {
        mExecutorService.execute(new BackgroundCallRunner<R>(runnable));
    }

    private class BackgroundCallRunner<R> implements Runnable {
        private final BackgroundCallRunnable<R> mBackgroundRunnable;

        BackgroundCallRunner(BackgroundCallRunnable<R> runnable) {
            mBackgroundRunnable = runnable;
        }

        @Override
        public final void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBackgroundRunnable.preExecute();
                }
            });

            R result = mBackgroundRunnable.runAsync();

            sHandler.post(new ResultCallback(result));
        }

        private class ResultCallback implements Runnable {
            private final R mResult;

            private ResultCallback(R result) {
                mResult = result;
            }

            @Override
            public void run() {
                mBackgroundRunnable.postExecute(mResult);
            }
        }
    }

    class TraktNetworkRunner<R> implements Runnable {

        private final TraktNetworkCallRunnable<R> mBackgroundRunnable;

        TraktNetworkRunner(TraktNetworkCallRunnable<R> runnable) {
            mBackgroundRunnable = runnable;
        }

        @Override
        public final void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBackgroundRunnable.onPreTraktCall();
                }
            });

            R result = null;
            RetrofitError retrofitError = null;

            try {
                result = mBackgroundRunnable.doBackgroundCall();
            } catch (RetrofitError re) {
                retrofitError = re;
                if (Constants.DEBUG) {
                    Log.d(getClass().getSimpleName(), "Error while completing network call", re);
                }
            }

            sHandler.post(new ResultCallback(result, retrofitError));
        }

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
                    mBackgroundRunnable.onSuccess(mResult);
                } else if (mRetrofitError != null) {
                    mBackgroundRunnable.onError(mRetrofitError);
                }
                mBackgroundRunnable.onFinished();
            }
        }
    }


}
