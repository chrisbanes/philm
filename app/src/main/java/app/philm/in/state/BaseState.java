package app.philm.in.state;

interface BaseState {

    public String getUsername();

    public String getHashedPassword();

    public void registerForEvents(Object receiver);

    public void unregisterForEvents(Object receiver);

}
