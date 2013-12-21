package app.philm.in.state;


import android.accounts.Account;

public interface UserState extends BaseState {

    public Account getCurrentAccount();

    public void setCurrentAccount(Account account);

    public void setCredentials(String username, String hashedPassword);

    public static class UserCredentialsConfirmedEvent {}

    public static class AccountChangedEvent {}

}
