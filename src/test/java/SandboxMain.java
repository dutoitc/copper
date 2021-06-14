import ch.mno.copper.collect.WebCollector;
import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xsicdt on 07/11/17.
 */
public class SandboxMain {

//    public static String convert(String s) throws UnsupportedEncodingException {
//        byte[] bytes = s.getBytes("UTF-8");
//        for (int i=0; i<bytes.length; i++) {
//            System.out.println((int)bytes[i]);
//        }
//
//        return "";
//    }

    public static void main(String[] args) throws UnsupportedEncodingException, ConnectorException {
        List<Pair<String, String>> vk = new ArrayList<>();
        vk.add(Pair.of("body", null));
        List<String> lst = WebCollector.query("http://tom.etat-de-vaud.ch/registres/refact/rcent/summary.txt", null, null, vk);
        for (String el:lst) {
            System.out.println(el);
        }


        /*HttpConnector conn = new HttpConnector("int-esgate.etat-de-vaud.ch", -1, "https",
                null, -1, null,
                "gvd0refmon-va", "SQFKT8QQ71RNP2MQ");
        String res = conn.get("https://int-esgate.etat-de-vaud.ch/rcentds-i2/infrastructure/ping");
        System.out.println(res);*/
//        String s="éà";
//        System.out.println(convert(s));

        /*System.out.println(new String(s.getBytes("ISO-8859-1"), "UTF-8"));
        for (int i=0; i<s.length(); i++) {
            System.out.println((int)s.charAt(i)+" " + s.charAt(i));
        }*/
    }

}
