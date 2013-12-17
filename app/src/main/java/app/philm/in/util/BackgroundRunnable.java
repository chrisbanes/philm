package app.philm.in.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

public abstract class BackgroundRunnable<R> implements Runnable {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    @Override
    public final void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        final R result = runAsync();

        sHandler.post(new Runnable() {
            @Override
            public void run() {
                postExecute(result);
            }
        });
    }

    public abstract R runAsync();

    public void postExecute(R result) {
    }
}
