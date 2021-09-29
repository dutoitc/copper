package ch.mno.copper.stories.data;

import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.report.AbstractReporterWrapper;
import ch.mno.copper.store.ValuesStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class Story {

    private static final Logger LOG = LoggerFactory.getLogger(Story.class);
    private When when = null;
    private String storyName;
    private String storyText;
    private String error;
    private String cron;
    private boolean valid;
    private final String given;
    private transient AbstractReporterWrapper reporterWrapper;


    public Story(StoryGrammar grammar, String storyName, String storyText) throws IOException {
        this(grammar, new ByteArrayInputStream(storyText.getBytes()), storyName);
    }


    public Story(StoryGrammar grammar, InputStream is, String storyName) throws IOException {
        this.storyName = storyName;
        storyText = IOUtils.toString(is);
        storyText = Pattern.compile("#.*?\n", Pattern.DOTALL).matcher(storyText).replaceAll(""); // Remove comments lines
        if (!storyText.endsWith("\n")) storyText = storyText + "\n"; // Little help for parsing

        String patternMain = grammar.getPatternFull("MAIN");

        // Check Story Syntax
        // TODO: mark storyText as invalid, and permit WEB update
        try {
            SyntaxHelper.checkSyntax(grammar, patternMain, storyText);
            valid = true;
        } catch (SyntaxException e) {
            error = e.getMessage();
            LOG.trace("Story syntax error: {}", error);
            valid = false;
        }

        // Extract triggers
        if (Pattern.compile(grammar.getPatternFull("RUN_ON"), Pattern.DOTALL).matcher(storyText).find()) {
            this.cron = buildRunOn(grammar);
        } else {
            throw new RuntimeException("cannot find a RUN_ON expression");
        }


        // Extract collector using GIVEN pattern
        Matcher matchGiven = Pattern.compile(grammar.getPatternFull("GIVEN"), Pattern.DOTALL).matcher(storyText);
        if (!matchGiven.find()) throw new RuntimeException("Cannot find a valid GIVEN expression");
        given = matchGiven.group();

        // Extract WHEN
        //WHEN::=WHEN [a-zA-Z0-9_]+[<>=]\d(\.\d)
        Matcher matchWhen = Pattern.compile(grammar.getPatternFull("WHEN"), Pattern.DOTALL).matcher(storyText);
        if (matchWhen.find()) {
            this.when = new When(matchWhen.group());
        }


        // Extract repporter using THEN pattern
        Matcher matchREPORTER = Pattern.compile(grammar.getPatternFull("REPORTER"), Pattern.DOTALL).matcher(storyText);
        if (!matchREPORTER.find()) throw new RuntimeException("Cannot find a valid REPORTER expression");
//        String storyReporter = matchREPORTER.group();
        // FIXME: complete test and mock mailServer ?
        //this.reporterWrapper = ReporterWrapperFactory.buildReporterWrapper(grammar, storyReporter);
    }

    static boolean matchWhen(String storedValue, String operator, String expectedValue) {
        if (expectedValue.contains(".")) {
            float a = Float.parseFloat(storedValue);
            float b = Float.parseFloat(expectedValue);
            switch (operator) {
                case "<":
                    return a < b;
                case ">":
                    return a > b;
                case "=":
                    return Math.abs(a - b) < Math.abs(a / 25);
                default:
                    throw new RuntimeException("Unsuppported operator " + operator);
            }
        }

        int a = Integer.parseInt(storedValue);
        int b = Integer.parseInt(expectedValue);
        switch (operator) {
            case "<":
                return a < b;
            case ">":
                return a > b;
            case "=":
                return a == b;
            default:
                throw new RuntimeException("Unsuppported operator " + operator);
        }
    }

    private String buildRunOn(StoryGrammar grammar) {
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patEol = grammar.getPatternFull("EOL");
        String patternRunOn = grammar.getPatternFull("RUN_ON");
        Matcher matcher3 = Pattern.compile(patternRunOn, Pattern.DOTALL).matcher(storyText);
        if (!matcher3.find()) throw new RuntimeException("Only supporting RUN_ON expressions yet.");
        String cronTxt = matcher3.group(0);
        matcher3 = Pattern.compile("DAILY at (\\d{4})").matcher(cronTxt);
        if (matcher3.find()) {
            String date = matcher3.group(0).substring(9);
            int hour = Integer.parseInt(date.substring(0, 2), 10);
            int min = Integer.parseInt(date.substring(2, 4), 10);
            return min + " " + hour + " * * *";
        }

        String patCronStd = grammar.getPatternFull("CRON_STD");
        matcher3 = Pattern.compile("CRON" + patSpaceEol + "+(" + patCronStd + ")" + patEol, Pattern.DOTALL).matcher(cronTxt);
        if (!matcher3.find()) throw new RuntimeException("Not found cron in " + cronTxt);
        return matcher3.group(1);
    }

    public AbstractReporterWrapper getReporterWrapper() {
        return reporterWrapper;
    }

    public String getCron() {
        return cron;
    }

    public String getName() {
        return storyName;
    }

    public String getStoryText() {
        return storyText;
    }

    public String getGiven() {
        return given;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean matchWhen(Map<String, String> values, ValuesStore valuesStore) {
        if (when == null) return true;
        if (values.containsKey(when.variable)) {
            return matchWhen(values.get(when.variable), when.operator, when.value);
        }
        if (valuesStore.getValue(when.variable) != null) {
            return matchWhen(valuesStore.getValue(when.variable), when.operator, when.value);
        }
        return false;
    }

    public String getError() {
        return error;
    }

    public void setCronData4Test(String cronData) {
        this.cron = cronData;
    }

    public boolean isValid() {
        return valid;
    }

    private static class When {
        private final String variable;
        private final String operator;
        private final String value;

        public When(String expression) {
            //WHEN::=WHEN [a-zA-Z0-9_]+[<>=]\d(\.\d)
            Matcher matcher = Pattern.compile("WHEN ([a-zA-Z0-9_]+)([<>=])(\\d(\\.\\d)?)").matcher(expression);
            if (!matcher.find()) throw new RuntimeException("Wrong pattern WHEN: '" + expression + "'");
            this.variable = matcher.group(1);
            this.operator = matcher.group(2);
            this.value = matcher.group(3);
        }
    }
}
