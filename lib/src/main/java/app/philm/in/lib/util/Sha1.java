package app.philm.in.lib.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

    public static String encode(String string) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(string.getBytes("UTF-8"));
            byte[] hash = sha.digest();
            for (int i = 0; i < hash.length; i++) {
                sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException nsa) {
            nsa.printStackTrace();
        } catch (UnsupportedEncodingException ue) {
            ue.printStackTrace();
        }
        return null;
    }

}
