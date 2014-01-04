package app.philm.in.model;

public class PhilmAccount {

    private final String mAccountName;
    private final String mPassword;

    public PhilmAccount(String accountName, String password) {
        mAccountName = accountName;
        mPassword = password;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getPassword() {
        return mPassword;
    }
}
