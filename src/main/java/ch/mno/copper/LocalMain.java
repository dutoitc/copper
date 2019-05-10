package ch.mno.copper;

import ch.mno.copper.collect.WebCollectorWrapper;
import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class LocalMain {

    public static void Main(String[] args) throws ConnectorException {
        String url="http://int-registres-rcpers-west-ws.etat-de-vaud.ch/registres/int-rcpers/west/ws/infrastructure/server/version";
        List<Pair<String, String>> valuesKept = new ArrayList<>();
        valuesKept.add(Pair.of("BODY", "BODY"));
        WebCollectorWrapper values = new WebCollectorWrapper(url, "gvd0rcent", "Welc0me_", valuesKept);
        System.out.println(values.execute().get("BODY"));
    }

}
