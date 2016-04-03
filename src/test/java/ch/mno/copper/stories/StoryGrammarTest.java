package ch.mno.copper.stories;

import ch.mno.copper.collect.JdbcCollectorWrapper;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class StoryGrammarTest {

    private StoryGrammar storyGrammar;

    @Before
    public void init() throws FileNotFoundException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
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

//    @Test
//    public void testDEFINE() {
//        String pattern = storyGrammar.getPatternFull("DEFINE");
//        Pattern pattern1 = Pattern.compile(pattern);
//        Assert.assertTrue(pattern1.matcher("DEFINE key1 value1\n").matches());
//        Assert.assertFalse(pattern1.matcher("DEFINE key1").matches());
//    }

    @Test
    public void testJDBC_URL() {
        String pattern = storyGrammar.getPatternFull("JDBC_URL");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("jdbc:oracle:thin:@//myhost:1521/orcl").matches());
        Assert.assertTrue(pattern1.matcher("jdbc:oracle:oci:@myhost:1521:orcl").matches());
    }

    @Test
    public void testCOLLECTOR_JDBC() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JDBC");
        testPattern(pattern, "JDBC WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,user=aUser,password=aPass  QUERY select 1 from dual\n");
        testPattern(pattern, "JDBC WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,\n   user=aUser,\n   password=aPass\n QUERY \"select something as chose\"\n");
    }


    @Test
    public void testCOLLECTOR_JMX() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JMX");
        testPattern(pattern, "JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass QUERY name FOR attribute AS something\n");
    }

    @Test
    public void testCOLLECTOR_JMX1() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JMX");
        //String pattern="JMX[\\s+\\r\\n]+WITH[\\s+\\r\\n]+url=service[:\\w/\\d\\.\\-]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=\\S+?[\\s+\\r\\n]\\s*QUERY.*? FOR .*?\\s+AS .*";
        testPattern(pattern, "JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n   user=aUser,\n   password=aPass QUERY name FOR attribute AS something\n");

    }


    @Test
    public void testCOLLECTOR_JMX2() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JMX");
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
    public void testCOLLECTOR_JMX3() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR_JMX");
        String jmx = "JMX WITH url=service:jmx:rmi://slv2737v.etat-de-vaud.ch:44444/jndi/rmi://slv2737v.etat-de-vaud.ch:1099/karaf-trun,user=tadmin,password=tadmin\n" +
                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION\n";
        String s = SyntaxHelper.checkSyntax(storyGrammar, pattern, jmx);
        Assert.assertEquals(jmx, s);
        // TODO: vérifier qu'il y a bien les deux queries
    }


    @Test
    public void testCOLLECTOR1() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR");
        //pattern="COLLECTOR[\\s+\\r\\n]+(ORACLE[\\s+\\r\\n]+WITH[\\s+\\r\\n]+//url=jdbc[:\\w@/\\d]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=.*?[\\s+\\r\\n]QUERY ((\\\".*?\\\")|.*)\\r?\\n|JMX[\\s+\\r\\n]+WITH[\\s+\\r\\n]+url=service[:\\w/\\d]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=\\S+?[\\s+\\r\\n]\\s*(QUERY .*? FOR .*?\\s+AS .*?[\\s+\\r\\n])+)";
        Pattern pattern1 = Pattern.compile(pattern);
        //Assert.assertTrue(pattern1.matcher("COLLECTOR ORACLE WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,user=aUser,password=aPass\n").matches());
        testPattern(pattern, "COLLECTOR JDBC WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,user=aUser,password=aPass QUERY select 1 from dual\n");
    }

    @Test
    public void testCOLLECTOR2() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR");
        Pattern pattern1 = Pattern.compile(pattern);
        testPattern(pattern, "COLLECTOR JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass QUERY oname FOR att AS att1\n");
    }

    @Test
    public void testCOLLECTOR3() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR");
        Pattern pattern1 = Pattern.compile(pattern);
        testPattern(pattern,"COLLECTOR JDBC\n" +
                "        WITH url=jdbc:oracle:thin:@//myhost:1521/orcl,\n" +
                "             user=aUser,\n" +
                "             password=aPass\n"+
                "       QUERY \"Select a, b, c\"\n");
   }
    @Test
    public void testCOLLECTOR4() {
        String pattern = storyGrammar.getPatternFull("COLLECTOR");
        Pattern pattern1 = Pattern.compile(pattern);
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
    public void testRUN_ON() {
        String pattern = storyGrammar.getPatternFull("RUN_ON");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher("RUN ON CRON * * * * *\n").matches());
        Assert.assertTrue(pattern1.matcher("RUN ON CRON 1 1 1 1 1\n").matches());
        Assert.assertTrue(pattern1.matcher("RUN DAILY at 0600\n").matches());
        Assert.assertTrue(pattern1.matcher("RUN ON CRON 24,56 * * * *\n").matches());
    }

    @Test
    public void testPushover() {
        String txt="REPORT BY PUSHOVER to \"dest\"\n" +
                "     WITH token=\"xxx\"\n" +
                "     WITH title=\"Status RCEnt\"\n" +
                "     WITH message=\"Status (nouveau, en cours, en erreur, traitée):\n" +
                "                PR {{RCENT_PR_STG_NOUVEAU}}/{{RCENT_PR_STG_EN_COURS}}/{{RCENT_PR_MST_EN_ERREUR}}/{{RCENT_PR_TRAITEE}}\n" +
                "                PP {{RCENT_PP_STG_NOUVEAU}}/{{RCENT_PP_STG_EN_COURS}}/{{RCENT_PP_MST_EN_ERREUR}}/{{RCENT_PP_TRAITEE}}\n" +
                "                VA {{RCENT_VA_STG_NOUVEAU}}/{{RCENT_VA_STG_EN_COURS}}/{{RCENT_VA_MST_EN_ERREUR}}/{{RCENT_VA_TRAITEE}}\n" +
                "                IN {{RCENT_IN_STG_NOUVEAU}}/{{RCENT_IN_STG_EN_COURS}}/{{RCENT_IN_MST_EN_ERREUR}}/{{RCENT_IN_TRAITEE}}\"";

       /* String pattern = storyGrammar.getPatternFull("PUSHOVER");
        Pattern pattern1 = Pattern.compile(pattern);
        Assert.assertTrue(pattern1.matcher(txt).matches());*/
        SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("PUSHOVER"),txt);
    }

    @Test
    public void testCSV() {
        String txt="REPORT BY CSV to \"filename.csv\" WITH headers=\"my header1;my header2;my header3\"\n"+
                "    WITH line=\"{{value1}};{{value2}};{{value3}}\"";

        SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("CSV"),txt);
        SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("REPORTER"),txt);
    }

    @Test
    public void testCSV2() {
        String txt="RUN ON CRON 0 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "THEN REPORT BY CSV to \"data.csv\"\n" +
                "     WITH header=\"h1\"\n" +
                "     WITH line=\"v1\"\n";

        SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("MAIN"),txt);
    }


    @Test
    public void testJdbcStory() throws IOException, ConnectorException, URISyntaxException {
        String pattern = storyGrammar.getPatternFull("MAIN");
//        Pattern pattern1 = Pattern.compile(pattern, Pattern.DOTALL);
        URL resource = getClass().getResource("/OracleStory1.txt");
        String storyText = IOUtils.toString(resource);
//        Assert.assertTrue(pattern1.matcher(story).matches());
//        testPattern(pattern, story);
        SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("MAIN"),storyText);


        Path path = Paths.get(resource.toURI());
        Story story = new Story(storyGrammar, new FileInputStream(path.toFile()), path);
        JdbcCollectorWrapper wrapper = (JdbcCollectorWrapper)story.getCollectorWrapper();
        Assert.assertEquals("jdbc:oracle:thin:@//myhost:1521/orcl", wrapper.getUrl());
        Assert.assertEquals("aUser", wrapper.getUsername());
        String query = wrapper.getQuery().replaceAll("\r", "");
        Assert.assertEquals("select 1 from dual,\n" +
                "                      2 from trial", query);
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

    @Test
    public void testMailStory() throws IOException {
        String pattern = storyGrammar.getPatternFull("MAIL");
        String story = IOUtils.toString(getClass().getResource("/MailStory.txt"));
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

//    @Test
//    public void testX() {
//        testPattern("JMX[\\s+\\r\\n]+WITH[\\s+\\r\\n]+url=service[:\\w/\\d]+\\w,[\\s+\\r\\n]*user=.*?,[\\s+\\r\\n]*password=\\S+?[\\s+\\r\\n]\\s*(QUERY .*? FOR .*?\\s+AS .*?[\\s+\\r\\n])+", "JMX WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,user=aUser,password=aPass\n" +
//                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n");
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