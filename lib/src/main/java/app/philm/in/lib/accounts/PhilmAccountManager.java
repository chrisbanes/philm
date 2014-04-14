package app.philm.in.lib.accounts;

import java.util.List;

import app.philm.in.lib.model.PhilmAccount;

public interface PhilmAccountManager {

    public List<PhilmAccount> getAccounts();

    public void addAccount(PhilmAccount account);

    public void removeAccount(PhilmAccount account);

    public void updatePassword(PhilmAccount account);

}
