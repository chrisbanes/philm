package app.philm.in.state;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.otto.Bus;

import java.util.List;

public final class ApplicationState implements BaseState, MoviesState {

    private final Bus mEventBus;

    private List<Movie> mCollection;

    public ApplicationState(Bus eventBus) {
        mEventBus = Preconditions.checkNotNull(eventBus, "eventBus cannot null");
    }

    @Override
    public List<Movie> getCollection() {
        return mCollection;
    }

    @Override
    public void setCollection(List<Movie> collection) {
        mCollection = collection;
        mEventBus.post(new CollectionChangedEvent());
    }

    @Override
    public boolean hasCollection() {
        return mCollection != null && !mCollection.isEmpty();
    }

    @Override
    public void registerForEvents(Object receiver) {
        mEventBus.register(receiver);
    }

    @Override
    public void unregisterForEvents(Object receiver) {
        mEventBus.unregister(receiver);
    }
}
