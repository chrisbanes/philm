package app.philm.in.state;

interface BaseState {

    public void registerForEvents(Object receiver);

    public void unregisterForEvents(Object receiver);

}
