package app.philm.in.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PhilmAuthenticatorService extends Service {

    private PhilmAccountAuthenticator mPhilmAccountAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mPhilmAccountAuthenticator = new PhilmAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPhilmAccountAuthenticator.getIBinder();
    }

}
