package ch.mno.copper.stories;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class StoryGrammarTest {

    private StoryGrammar storyGrammar;

    @Before
    public void init() throws FileNotFoundException {
        storyGrammar = new StoryGrammar(new FileInputStream("StoryGrammar.txt"));
    }

    @Test
    public void testSPACE() {
        String pattern = storyGrammar.getPattern("SPACE");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher(" ").matches());
        Assert.assertTrue(pattern1.matcher("\r\n").matches());
        Assert.assertTrue(pattern1.matcher("\t").matches());
        Assert.assertTrue(pattern1.matcher(" \t \r\n").matches());
        Assert.assertFalse(pattern1.matcher("yop").matches());
    }

    @Test
    public void testEOL() {
        String pattern = storyGrammar.getPattern("EOL");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertFalse(pattern1.matcher("\r").matches());
        Assert.assertTrue(pattern1.matcher("\r\n").matches());
        Assert.assertTrue(pattern1.matcher("\n").matches());
        Assert.assertFalse(pattern1.matcher(" ").matches());
        Assert.assertFalse(pattern1.matcher("\t").matches());
    }

    @Test
    public void testDEFINE() {
        String pattern = storyGrammar.getPatternFull("DEFINE");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("DEFINE key1 value1\n").matches());
        Assert.assertFalse(pattern1.matcher("DEFINE key1").matches());
    }

    @Test
    public void testJDBC_URL() {
        String pattern = storyGrammar.getPatternFull("JDBC_URL");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("jdbc:oracle:thin:@//myhost:1521/orcl").matches());
        Assert.assertTrue(pattern1.matcher("jdbc:oracle:oci:@myhost:1521:orcl").matches());
    }

    @Test
    public void testCOLLECTOR_ORACLE() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_ORACLE");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("ORACLE WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,user=aUser,password=aPass\n").matches());
        Assert.assertTrue(pattern1.matcher("ORACLE WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,\n   user=aUser,\n   password=aPass\n").matches());
    }


    @Test
    public void testCOLLECTOR_JMX() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JMX");
        testPattern(pattern, "JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass QUERY name FOR attribute AS something\n");
        testPattern(pattern, "JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n   user=aUser,\n   password=aPass QUERY name FOR attribute AS something\n");
        testPattern(pattern, "JMX\n" +
                "        WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n" +
                "             user=aUser,\n" +
                "             password=aPass\n" +
                "        QUERY java.lang:type=Runtime FOR SpecName       AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "        QUERY java.lang:type=Runtime FOR SpecVersion    AS JMX_LOCAL_RUNTIME_SPECVERSION\n");
//        Assert.assertTrue(pattern1.matcher("JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass QUERY name attribute AS something\n").matches());
//        Assert.assertTrue(pattern1.matcher("JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n   user=aUser,\n   password=aPass QUERY name FOR attribute AS something\n").matches());
    }



    @Test
    public void testCOLLECTOR() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("COLLECTOR ORACLE WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,user=aUser,password=aPass\n").matches());
        Assert.assertTrue(pattern1.matcher("COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass QUERY oname FOR att AS att1\n").matches());
        Assert.assertTrue(pattern1.matcher("COLLECTOR ORACLE\n" +
                                "        WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,\n" +
                                "             user=aUser,\n" +
                                "             password=aPass\n").matches());
        Assert.assertFalse(pattern1.matcher("JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass\n").matches());
    }


    @Test
    public void testCRON_STD() {
        String pattern = storyGrammar.getPatternFull("CRON_STD");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("* * * * *").matches());
        Assert.assertTrue(pattern1.matcher("1 1 1 1 1").matches());
        Assert.assertTrue(pattern1.matcher("*/5 7-18 * * 1-5").matches());

    }

    @Test
    public void testCRON() {
        String pattern = storyGrammar.getPatternFull("CRON");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("CRON * * * * *\n").matches());
        Assert.assertTrue(pattern1.matcher("CRON 1 1 1 1 1\n").matches());
        Assert.assertTrue(pattern1.matcher("CRON DAILY at 0600\n").matches());
    }


    @Test
    public void testOracleStory() throws IOException {
        String pattern = storyGrammar.getPatternFull("MAIN");
        Pattern pattern1 = Pattern.compile(pattern, Pattern.DOTALL);
        String story = IOUtils.toString(getClass().getResource("/OracleStory1.txt"));
        Assert.assertTrue(pattern1.matcher(story).matches());
    }


    @Test
    public void testJmxStory1() throws IOException {
        String pattern = storyGrammar.getPatternFull("MAIN");
        String story = IOUtils.toString(getClass().getResource("/JmxStory1.txt"));
        testPattern(pattern, story);
    }

    @Test
    public void testJmxStory2() throws IOException {
        String pattern = storyGrammar.getPatternFull("MAIN");
        String story = IOUtils.toString(getClass().getResource("/JmxStory2.txt"));
        testPattern(pattern, story);
    }

//    @Test
//    public void testTemp() {
//        String pat="GIVEN[\\s+\\r\\n]+COLLECTOR[\\s+\\r\\n]+(ORACLE[\\s+\\r\\n]+WITH[\\s+\\r\\n]+url=jdbc[:\\w@/\\d]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=.*?\\r?\\n|JMX[\\s+\\r\\n]+WITH[\\s+\\r\\n]+url=service[:\\w/\\d]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=.*?\\r?\\n)[\\s+\\r\\n]*WHEN[\\s+\\r\\n]+CRON[\\s+\\r\\n]+(DAILY at \\d{4})\\r?\\n";
//        String dst="GIVEN\n" +
//                "    COLLECTOR JMX\n" +
//                "        WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n" +
//                "             user=aUser,\n" +
//                "             password=aPass\n" +
//                "WHEN\n" +
//                "    CRON DAILY at 0605\n" +
//                "";
//        Assert.assertTrue(Pattern.compile(pat, Pattern.DOTALL+Pattern.MULTILINE).matcher(dst).matches());
//        //Assert.assertTrue(Pattern.compile("WHEN[\\s+\\r\\n]+CRON[\\s+\\r\\n]+((DAILY at \\d{4})|(\\S \\S \\S \\S \\S))",Pattern.DOTALL).matcher("WHEN\n   CRON DAILY at 0605").matches());
//    }


    /** Test pattern, and if does not match, find a subpart off pattern which match */
    private void testPattern(String pattern, String value) {
        if (Pattern.compile(pattern, Pattern.DOTALL).matcher(value).matches()) return;
        // Test latest pattern possible
        for (int i=pattern.length()-1; i>0; i--) {
            String currPattern = pattern.substring(0, i);
            try {
                Pattern currPatternCompiled = Pattern.compile(currPattern, Pattern.DOTALL);

                for (int j = value.length()-1; j>1; j--) {
                    String valuePart = value.substring(0, j);
                    if (currPatternCompiled.matcher(valuePart).matches()) {
                        Assert.fail("Pattern \n   >>>" + pattern + "\n does not match\n   >>>" + value + "\n but pattern start \n   >>>" + currPattern + "\nmatches\n   >>>" + valuePart);
                    }
                }
            } catch (PatternSyntaxException e) {
                // Just ignore
            }
        }
    }

}