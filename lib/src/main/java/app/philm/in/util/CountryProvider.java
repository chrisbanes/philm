package app.philm.in.util;

public interface CountryProvider {

    public static final String US_TWO_LETTER_CODE = "US";

    /**
     * @return ISO 3166-1 country code
     */
    public String getTwoLetterCountryCode();

    /**
     * @return ISO 639-1 language code
     */
    public String getTwoLetterLanguageCode();

}
