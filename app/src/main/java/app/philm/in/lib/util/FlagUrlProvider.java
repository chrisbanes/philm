package app.philm.in.lib.util;

public class FlagUrlProvider {

    public String getCountryFlagUrl(String countryCode) {
        return "http://www.geonames.org/flags/x/" + countryCode.toLowerCase() + ".gif";
    }

}
