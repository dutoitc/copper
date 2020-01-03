package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.BinaryConnector;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class BinaryCollectorWrapper extends AbstractCollectorWrapper {

    private final List<CheckElement> checkElements;
    private Map<String, String> results;

    public BinaryCollectorWrapper(List<CheckElement> checkElements) {
        this.checkElements = checkElements;
        results = new HashMap<>(checkElements.size()*4/3+1);
    }

    public List<String> getAs() {
        return checkElements.stream().map(e->e.as).collect(Collectors.toList());
    }


    @Override
    public Map<String, String> execute() throws ConnectorException {
        for (CheckElement el: checkElements) {
            boolean status;
            switch (el.cmd) {
                case "CHECK_BY_WHICH":
                    String res = BinaryConnector.executeCommand("which " + el.path);
                    System.out.println("DBG>" + res);
                    status = !res.contains("which: no ") && !res.contains("Cannot run");
                    break;
                case "CHECK_BY_PATH":
                    status = new File(el.path).exists();
                    break;
                default:
                    System.out.println("DBG> invalid command");
                    throw new RuntimeException("Invalid command: " + el.cmd);
            }
            results.put(el.as, status?"OK":"KO");
        }

        return results;
    }


    @Override
    public List<List<String>> execute2D() throws ConnectorException {
        Map<String, String> map = execute();
        List<List<String>> lst = new ArrayList<>();
        for (CheckElement entry: checkElements) {
            lst.add(Arrays.asList(map.get(entry.as)));
        }
        return lst;
    }

    public static BinaryCollectorWrapper buildCollector(StoryGrammar grammar, String storyGiven) {
        String patternJdbc = grammar.getPatternFull("COLLECTOR_BINARY");
        Matcher matcher = Pattern.compile(patternJdbc, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_BINARY");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJdbc);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJdbc + "\nin\n   >>>" + storyGiven);
        }

        String collectorSocketData = matcher.group(0);
        String patSpace = grammar.getPatternFull("SPACE");
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String regex = "(CHECK_BY_WHICH|CHECK_BY_PATH)" + patSpace + "(.*?)" + patSpace + "AS" + patSpace + "(.*?)" + patSpaceEol;
        Matcher matcher2 = Pattern.compile(regex, Pattern.DOTALL).matcher(collectorSocketData);
        List<CheckElement> checkElements = new ArrayList<>(8);
        while (matcher2.find()) {
            checkElements.add(new CheckElement(matcher2.group(1),matcher2.group(2),matcher2.group(3)));
        }
        if (checkElements.isEmpty()) {
            throw new RuntimeException("No CHECK found in BINARY_CHECK");
        }
        return new BinaryCollectorWrapper(checkElements);
    }


    private static class CheckElement {
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
