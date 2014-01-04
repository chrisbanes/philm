package app.philm.in.util;

import app.philm.in.network.BackgroundCallRunnable;
import app.philm.in.network.TraktNetworkCallRunnable;

public interface BackgroundExecutor {

    public <R> void execute(TraktNetworkCallRunnable<R> runnable);

    public <R> void execute(BackgroundCallRunnable<R> runnable);

}
