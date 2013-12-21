package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.accounts.Account;
import android.accounts.AccountManager;

import app.philm.in.Constants;

public class AccountManagerHelper {

    private AccountManager mAccountManager;

    public AccountManagerHelper(AccountManager accountManager) {
        mAccountManager = Preconditions.checkNotNull(accountManager,
                "accountManager cannot be null");
    }

    public void addAccount(Account account, String password) {
        mAccountManager.addAccountExplicitly(account, password, null);
    }

    public void setAuthToken(Account account, String authToken, String authTokenType) {
        mAccountManager.setAuthToken(account, authToken, authTokenType);
    }

    public void setPassword(Account account, String password) {
        mAccountManager.setPassword(account, password);
    }

    public String getPassword(Account account) {
        return mAccountManager.getPassword(account);
    }

    public Account[] getAccounts() {
        return mAccountManager.getAccountsByType(Constants.TRAKT_ACCOUNT_TYPE);
    }

}
