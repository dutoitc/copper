package ch.mno.copper.web.filters;

import java.util.*;

public class AuthStorage {

    private static Map<String, Date> expirationDates = new HashMap<>();

    AuthStorage() {
        // Needed by Sonar
    }

    public static void addToken(String token, Date expirationDate) {
        synchronized (expirationDates) {
            expirationDates.put(token, expirationDate);
        }
    }

    public static boolean isTokenValid(String token) {
        synchronized (expirationDates) {
            Date now = new Date();

            // Cleanup (ugly but works for yet)
            List<String> keys = new ArrayList<>(expirationDates.keySet());
            keys.forEach(k-> {
                if (expirationDates.get(k).before(now)) {
                    expirationDates.remove(k);
                }
            });

            // If present, is valid
            return expirationDates.containsKey(token);
        }
    }

}