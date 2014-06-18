/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

import app.philm.in.Constants;

public class AndroidCountryProvider implements CountryProvider {

    private static final String LOG_TAG = AndroidCountryProvider.class.getSimpleName();

    private final Context mContext;

    private String mCountryCode;
    private String mLanguageCode;

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

    @Override
    public String getTwoLetterLanguageCode() {
        if (mLanguageCode == null) {
            final Locale locale = Locale.getDefault();
            mLanguageCode = locale.getLanguage();
        }
        return mLanguageCode;
    }
}
