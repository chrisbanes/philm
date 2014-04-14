package app.philm.in.lib.util;

import app.philm.in.lib.network.BackgroundCallRunnable;
import app.philm.in.lib.network.NetworkCallRunnable;

public interface BackgroundExecutor {

    public <R> void execute(NetworkCallRunnable<R> runnable);

    public <R> void execute(BackgroundCallRunnable<R> runnable);

}
