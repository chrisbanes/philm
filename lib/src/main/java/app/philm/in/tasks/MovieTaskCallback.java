package app.philm.in.tasks;


import app.philm.in.network.NetworkError;

public interface MovieTaskCallback {

    public void showLoadingProgress(boolean show);

    public void showError(NetworkError error);

}
