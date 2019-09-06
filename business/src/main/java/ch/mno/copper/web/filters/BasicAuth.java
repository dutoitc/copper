package ch.mno.copper.web.filters;

import java.util.Base64;

public class BasicAuth {
    /**
     * Decode basic-auth as user / pass
     */
    public static String[] decode(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");


        byte[] decodedBytes = Base64.getDecoder().decode(auth);

        if(decodedBytes == null || decodedBytes.length == 0){
            return null;
        }

        return new String(decodedBytes).split(":", 2);
    }
}