package app.philm.in.network;

public abstract class BackgroundCallRunnable<R> {

    public void onPreBackgroundCall() {}

    public abstract R doBackgroundCall();

    public void onFinished(R result) {}

 }
