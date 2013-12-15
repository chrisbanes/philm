package app.philm.in.state;


public interface UserState extends BaseState {

    public String getUsername();

    public String getHashedPassword();

    public void setCredentials(String username, String hashedPassword);

    public static class UserCredentialsConfirmedEvent {}

}
