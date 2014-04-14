package app.philm.in.lib.util;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

import app.philm.in.lib.Constants;

public class AndroidCountryProvider implements CountryProvider {

    private static final String LOG_TAG = AndroidCountryProvider.class.getSimpleName();

    private final Context mContext;

    private String mCountryCode;

    public AndroidCountryProvider(Context context) {
        mContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @Override
    public String getTwoLetterCountryCode() {
        if (mCountryCode == null) {
            // Try getting it from the SIM/Network
            String code = getTwoLetterCountryCodeFromSim();

            if (code == null) {
                code = getTwoLetterCountryCodeFromLocale();
                // TODO: Fallback to last location
            }

            mCountryCode = code;
        }

        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "getTwoLetterCountryCode: " + mCountryCode);
        }

        return mCountryCode;
    }

    private String getTwoLetterCountryCodeFromSim() {
        final TelephonyManager tm = (TelephonyManager)
                mContext.getSystemService(Context.TELEPHONY_SERVICE);

        final String simCountry = tm.getSimCountryIso();
        if (simCountry != null && simCountry.length() == 2) {
            // SIM country code is available
            return simCountry.toLowerCase(Locale.US);
        } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            // device is not 3G (would be unreliable)
            String networkCountry = tm.getNetworkCountryIso();
            if (networkCountry != null && networkCountry.length() == 2) {
                // network country code is available
                return networkCountry.toLowerCase(Locale.US);
            }
        }

        return null;
    }

    private String getTwoLetterCountryCodeFromLocale() {
        final Locale locale = Locale.getDefault();

        final String countryCode = locale.getCountry();
        if (!TextUtils.isEmpty(countryCode)) {
            return countryCode;
        }

        return null;
    }
}
