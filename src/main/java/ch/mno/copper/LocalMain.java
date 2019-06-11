package ch.mno.copper;

import ch.mno.copper.collect.WebCollectorWrapper;
import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class LocalMain {

    public static void Main(String[] args) throws ConnectorException {
        String url="http://int-registres-app-west-ws.mydomain.ch/somecontext/api/version";
        List<Pair<String, String>> valuesKept = new ArrayList<>();
        valuesKept.add(Pair.of("BODY", "BODY"));
        WebCollectorWrapper values = new WebCollectorWrapper(url, "username", "password", valuesKept);
        System.out.println(values.execute().get("BODY"));
    }

}
