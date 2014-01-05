package app.philm.in.accounts;

import java.util.List;

import app.philm.in.model.PhilmAccount;

public interface PhilmAccountManager {

    public List<PhilmAccount> getAccounts();

    public void addAccount(PhilmAccount account);

    public void setAuthToken(PhilmAccount account);

    public void setPassword(PhilmAccount account);

}
