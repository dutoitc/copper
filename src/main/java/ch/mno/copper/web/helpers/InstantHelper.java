package ch.mno.copper.web.helpers;

import javax.ws.rs.QueryParam;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantHelper {

    public static final Instant INSTANT_2000 = Instant.parse("2000-01-01T00:00:00.00Z");

    private InstantHelper() {

    }

    public static Instant findInstant(@QueryParam("from") String dateFrom, Instant defaultValue, boolean b) {
        Instant from;
        if (dateFrom == null) {
            from = defaultValue;
        } else {
            from = toInstant(dateFrom, b);
        }
        return from;
    }

    public static Instant toInstant(String date, boolean am) {
        if (date == null || "null".equals(date)) return null;

        String[] formats = new String[]{"dd.MM.yyyy", "yyyy-MM-dd"};
        for (String format : formats) {
            try {
//                System.out.println("Parsing '"+date+"' with '"+format+"'");
                LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
                if (am) return LocalDateTime.of(ld, LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC);
                return LocalDateTime.of(ld, LocalTime.of(23, 59, 59)).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                // Nothing yet
            }
        }

        formats = new String[]{"yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy'T'HH:mm"};
        for (String format : formats) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            try {
                //return (LocalDateTime)formatter.parse(date);
                return LocalDateTime.parse(date, formatter).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                // Nothing yet
            }
        }
        throw new RuntimeException("Cannot parse '" + date + "'");
    }


}
