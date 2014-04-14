package app.philm.in.lib.util;

import com.google.common.base.Preconditions;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import app.philm.in.lib.Constants;
import app.philm.in.lib.network.BackgroundCallRunnable;
import app.philm.in.lib.network.NetworkCallRunnable;
import retrofit.RetrofitError;

public class PhilmBackgroundExecutor implements BackgroundExecutor {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private final ExecutorService mExecutorService;

    public PhilmBackgroundExecutor(ExecutorService executorService) {
        mExecutorService = Preconditions.checkNotNull(executorService,
                "executorService cannot be null");
    }

    @Override
    public <R> void execute(NetworkCallRunnable<R> runnable) {
        mExecutorService.execute(new TraktNetworkRunner<>(runnable));
    }

    @Override
    public <R> void execute(BackgroundCallRunnable<R> runnable) {
        mExecutorService.execute(new BackgroundCallRunner<>(runnable));
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

        private final NetworkCallRunnable<R> mBackgroundRunnable;

        TraktNetworkRunner(NetworkCallRunnable<R> runnable) {
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
