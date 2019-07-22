package ch.mno.copper.web.filters;

import javax.xml.bind.DatatypeConverter;

public class BasicAuth {
    /**
     * Decode basic-auth as user / pass
     */
    public static String[] decode(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");

        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);

        if(decodedBytes == null || decodedBytes.length == 0){
            return null;
        }

        return new String(decodedBytes).split(":", 2);
    }
}