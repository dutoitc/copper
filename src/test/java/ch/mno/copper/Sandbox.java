package ch.mno.copper;

import ch.mno.copper.collect.WebCollector;
import ch.mno.copper.collect.WebCollectorWrapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xsicdt on 26/07/16.
 */
public class Sandbox {

    public static void main(String[] args) {
     /*   DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//        System.out.println(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now()));
        LocalDate ld = LocalDate.parse("26.07.2016", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDateTime.of(ld, LocalTime.of(0, 0));
        System.out.println(ld);*/

        List<Pair<String, String>> str = new ArrayList<>();
        str.add(new ImmutablePair<>("body", ""));

        //List<String> ret = WebCollector.query("http://someprefix-jenkins-myapp-ws.mydomain.ch/outils/jenkins-myapp/job/COPPER-compile/api/json?tree=color", "username", "apass", str);
        List<String> ret = WebCollector.query("http://someprefix-jenkins-myapp-ws.mydomain.ch/outils/jenkins-myapp/job/COPPER-compile/api/json?tree=color", "username", "apass", str);
        System.out.println(ret.get(0));
    }

}
