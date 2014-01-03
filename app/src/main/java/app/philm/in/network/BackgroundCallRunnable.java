package app.philm.in.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

public abstract class BackgroundCallRunnable<R> implements Runnable {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    @Override
    public final void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                onPreBackgroundCall();
            }
        });

        R result = doBackgroundCall();

        sHandler.post(new ResultCallback(result));
    }

    public void onPreBackgroundCall() {}

    public abstract R doBackgroundCall();

    public void onFinished(R result) {}

    private class ResultCallback implements Runnable {
        private final R mResult;

        private ResultCallback(R result) {
            mResult = result;
        }

        @Override
        public void run() {
            onFinished(mResult);
        }
    }
 }
