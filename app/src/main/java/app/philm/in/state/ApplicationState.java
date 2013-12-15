package app.philm.in.state;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.otto.Bus;

import java.util.List;

public final class ApplicationState implements BaseState, MoviesState, UserState {

    private final Bus mEventBus;

    private List<Movie> mLibrary;

    private String mUsername, mHashedPassword;

    public ApplicationState(Bus eventBus) {
        mEventBus = Preconditions.checkNotNull(eventBus, "eventBus cannot null");
    }

    @Override
    public void registerForEvents(Object receiver) {
        mEventBus.register(receiver);
    }

    @Override
    public void unregisterForEvents(Object receiver) {
        mEventBus.unregister(receiver);
    }

    ///////////////////////////
    //
    // MovieState
    //
    ///////////////////////////

    @Override
    public List<Movie> getLibrary() {
        return mLibrary;
    }

    @Override
    public void setLibrary(List<Movie> collection) {
        mLibrary = collection;
        mEventBus.post(new LibraryChangedEvent());
    }

    @Override
    public boolean hasLibrary() {
        return mLibrary != null && !mLibrary.isEmpty();
    }

    ///////////////////////////
    //
    // UserState
    //
    ///////////////////////////

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public String getHashedPassword() {
        return mHashedPassword;
    }

    @Override
    public void setCredentials(String username, String hashedPassword) {
        mUsername = username;
        mHashedPassword = hashedPassword;
        mEventBus.post(new UserCredentialsConfirmedEvent());
    }
}
