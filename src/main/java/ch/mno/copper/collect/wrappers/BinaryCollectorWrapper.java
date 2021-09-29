package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.connectors.BinaryConnector;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class BinaryCollectorWrapper implements AbstractCollectorWrapper {

    private final List<CheckElement> checkElements;
    private Map<String, String> results;

    public BinaryCollectorWrapper(List<CheckElement> checkElements) {
        this.checkElements = checkElements;
        results = new HashMap<>(checkElements.size()*4/3+1);
    }

    @Override
    public List<String> getAs() {
        return checkElements.stream().map(e->e.as).collect(Collectors.toList());
    }


    @Override
    public Map<String, String> execute() {
        for (CheckElement el: checkElements) {
            boolean status;
            switch (el.cmd) {
                case "CHECK_BY_WHICH":
                    String res = BinaryConnector.executeCommand("which " + el.path);
                    status = !res.contains("which: no ") && !res.contains("Cannot run") && !res.contains("EXIT_");
                    break;
                case "CHECK_BY_PATH":
                    status = new File(el.path).exists();
                    break;
                default:
                    results.put(el.as, "KO Invalid command: " + el.cmd);
                    continue;
            }
            results.put(el.as, status?"OK":"KO");
        }

        return results;
    }


    @Override
    public List<List<String>> execute2D() {
        Map<String, String> map = execute();
        List<List<String>> lst = new ArrayList<>();
        for (CheckElement entry: checkElements) {
            lst.add(Arrays.asList(map.get(entry.as)));
        }
        return lst;
    }

    public static class CheckElement {
        private String cmd;
        private String path;
        private String as;

        public CheckElement(String cmd, String path, String as) {
            this.cmd = cmd;
            this.path = path;
            this.as = as;
        }
    }
}
