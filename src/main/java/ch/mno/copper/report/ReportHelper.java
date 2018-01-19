package ch.mno.copper.report;

import ch.mno.copper.data.ValuesStore;

import java.util.Map;

/**
 * Created by dutoitc on 03.04.2016.
 */
public class ReportHelper {

    public static String expandMessage(Map<String, String> values, String message, ValuesStore instance) {
        if (values==null) {
            throw new RuntimeException("Null Values detected");
        }

        int p1 = message.indexOf("{{");
        while (p1>=0) {
            int p2 = message.indexOf("}}");
            if (p2==-1) throw new RuntimeException("Wrong message format: " + message);

            String key = message.substring(p1+2, p2);
            String value = values.get(key);
            if (value==null) {
                value = instance.getValue(key);
                if (value==null) value="?";
            }
            message = message.substring(0, p1) + value + message.substring(p2+2);
            p1 = message.indexOf("{{");
        }
        return message;
    }

}
