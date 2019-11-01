package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.NotImplementedException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebCollectorWrapper extends AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    protected List<Pair<String, String>> valuesKept;
    private List<String> as;

    public WebCollectorWrapper(String url, String username, String password, List<Pair<String, String>>  valuesKept) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.valuesKept = valuesKept;

        this.as = new ArrayList<>();
        for (Pair<String, String> pair : valuesKept) {
            as.add(pair.getKey());
        }
    }

    public List<String> getAs() {
        return as;
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<String> values = WebCollector.query(url, username, password, valuesKept);
        Map<String, String> map = new HashMap();
        if (values.size()!=valuesKept.size()) {
            throw new RuntimeException("Wrong values number, expected " + valuesKept.size() + ", got " + values.size());
        }
        for (int i=0; i<valuesKept.size(); i++) {
            map.put(valuesKept.get(i).getValue(), values.get(i));
        }
        return map;
    }

    @Override
    public List<List<String>> execute2D() throws ConnectorException {
        throw new NotImplementedException();
    }

    public static WebCollectorWrapper buildCollector(StoryGrammar grammar, String storyGiven) {
        String patternJMX = grammar.getPatternFull("COLLECTOR_WEB");
        Matcher matcher = Pattern.compile(patternJMX, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_WEB JMX");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJMX);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJMX + "\nin\n   >>>" + storyGiven);
        }
        //
        String collectorWebData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patSpace = grammar.getPatternFull("SPACE");
        String patEol = grammar.getPatternFull("EOL");

        Matcher matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorWebData);
        String queries;
        String url;
        String username=null;
        String password = null;
        if (matcher2.find()) {
            url = matcher2.group(1);
            username = matcher2.group(2);
            password = matcher2.group(3);
            queries = matcher2.group(4);
        } else {
            matcher2 = Pattern.compile("url=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorWebData);
            if (matcher2.find()) {
                url = matcher2.group(1);
                queries = matcher2.group(2);
            } else {
                throw new RuntimeException("Cannot readInstant COLLECTOR_WEB body in <" + collectorWebData + ">");
            }
        }

        Matcher matcher3 = Pattern.compile("KEEP (.*?)" + patSpace + "AS (.*?)" + patSpaceEol).matcher(queries);
        List<Pair<String, String>> valuesKept = new ArrayList<>();
        while (matcher3.find()) {
            String name1 = matcher3.group(1);
            String name2 = matcher3.group(2);
            valuesKept.add(new ImmutablePair(name1, name2));
        }
        return new WebCollectorWrapper(url, username, password, valuesKept);
    }


}
