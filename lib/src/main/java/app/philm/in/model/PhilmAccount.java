package app.philm.in.model;

public class PhilmAccount {

    private final String mAccountName;
    private final String mPassword;

    private String mAuthToken;
    private String mAuthTokenType;

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

    public void setAuthToken(String authToken, String type) {
        mAuthToken = authToken;
        mAuthTokenType = type;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getAuthTokenType() {
        return mAuthTokenType;
    }
}
