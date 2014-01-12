package app.philm.in.util;

import app.philm.in.network.BackgroundCallRunnable;
import app.philm.in.network.NetworkCallRunnable;

public interface BackgroundExecutor {

    public <R> void execute(NetworkCallRunnable<R> runnable);

    public <R> void execute(BackgroundCallRunnable<R> runnable);

}
