package ch.mno.copper.stories;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by dutoitc on 10.02.2016.
 */
@Disabled
public class LocalTest {

    private static StoryGrammar grammar;

    @BeforeAll
    public static void init() throws FileNotFoundException {
        grammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    //@Theory
    public void checkStory(File file) throws IOException, ConnectorException {
        System.out.println("Checking " + file.getName());
        new Story(grammar, new FileInputStream(file), file.getName());
    }

//    public static @DataPoints File[] candidates;
//    static {
//        try {
//            candidates = Files.walk(Paths.get("local/dsi")).filter(Files::isRegularFile).map(Path::toFile).toArray(s-> new File[s]);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    @Test
//    public void testLocal() throws IOException, ConnectorException {
////        new Story(grammar, new FileInputStream("local/dsi/jmxRCFACEINCollect.txt"));
////        new Story(grammar, new FileInputStream("local/dsi/RCFACEDbInCollector.txt"));
////        new Story(grammar, new FileInputStream("local/dsi/RCFACEDbVaCollector.txt"));
////        new Story(grammar, new FileInputStream("local/dsi/RCFACEDbPpCollector.txt"));
////        new Story(grammar, new FileInputStream("local/dsi/RCFACEDbPrCollector.txt"));
//
//        File file = new File("local/dsi/RCFACEDbReportByPushover.txt");
//        new Story(grammar, new FileInputStream(file), file.toPath());
//    }

//    @Test
//    public void test2() throws FileNotFoundException {
//        StoryGrammar grammar = new StoryGrammar(new FileInputStream("StoryGrammar.txt"));
//        SyntaxHelper.checkSyntax(grammar.getPatternFull("COLLECTOR_JMX"), "COLLECTOR JMX WITH url=service:jmx:rmi://src2737v.myhost:44444/jndi/rmi://src2737v.myhost:1099/karaf-trun ,user=tadmin,password=tadmin\n" +
//                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
//                "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION");
//    }

}
