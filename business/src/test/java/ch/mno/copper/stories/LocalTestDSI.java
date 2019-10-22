package ch.mno.copper.stories;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by xsicdt on 11/02/16.
 */
@Ignore
public class LocalTestDSI {
    StoryGrammar grammar;


    @Before
    public void init() throws FileNotFoundException {
        grammar = new StoryGrammar(new FileInputStream("src/main/resources/StoryGrammar.txt"));
    }

    @Test
    public void testLocalJmx() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEINCollectByJmx.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEINCollectByJmx.txt");
    }

    @Test
    public void testLocalIn() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEINDbCollector.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEINDbCollector.txt");
    }


    @Test
    public void testLocalVa() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEVADbCollector.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEVADbCollector.txt");
    }


    @Test
    public void testLocalPP() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEPPDbCollector.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEPPDbCollector.txt");
    }


    @Test
    public void testLocalPr() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEPRDbCollector.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEPRDbCollector.txt");
    }


//    @Test
//    public void testLocalPushover() throws IOException, ConnectorException {
//        Path path = Paths.get("dsi/RCFACEDbReportByPushover.txt");
//        new Story(grammar, new FileInputStream(path.toFile()), path);
//
//        String txt="GIVEN STORED VALUES\n" +
//                "WHEN CRON 20 10 * * *\n" +
//                "THEN REPORT BY PUSHOVER to \"uPCrexdCXkyWg5EirDomUBc5erxjWG\"\n" +
//                "     WITH token=\"asEkV6yeh69w8fS8vxGo19eWq2bJjS\"\n" +
//                "     WITH title=\"test RCFACE\"\n" +
//                "     WITH message=\"Status (nouveau, en cours, en erreur, traitée):\n" +
//                "                PR {{RCFACE_PR_STG_NOUVEAU}}/{{RCFACE_PR_STG_EN_COURS}}/{{RCFACE_PR_MST_EN_ERREUR}}/{{RCFACE_PR_TRAITEE}}\n" +
//                "                PP {{RCFACE_PP_STG_NOUVEAU}}/{{RCFACE_PP_STG_EN_COURS}}/{{RCFACE_PP_MST_EN_ERREUR}}/{{RCFACE_PP_TRAITEE}}\n" +
//                "                VA {{RCFACE_VA_STG_NOUVEAU}}/{{RCFACE_VA_STG_EN_COURS}}/{{RCFACE_VA_MST_EN_ERREUR}}/{{RCFACE_VA_TRAITEE}}\n" +
//                "                IN {{RCFACE_IN_STG_NOUVEAU}}/{{RCFACE_IN_STG_EN_COURS}}/{{RCFACE_IN_MST_EN_ERREUR}}/{{RCFACE_IN_TRAITEE}}\"";
//        String pattern = grammar.getPatternFull("MAIN");
//        SyntaxHelper.checkSyntax(grammar, pattern, txt);

/*
        String spaceEol="[\\s+\\r\\n]";
        String pattern = //"REPORT BY PUSHOVER to \\\".*?\\\""+spaceEol+"+WITH token=\\\".*?\\\""+spaceEol+"+WITH title=\\\".*?\\\""+spaceEol+"+WITH message=\\\".*?\\\"";
                         "REPORT BY PUSHOVER to \\\".*?\\\""+spaceEol+"+WITH token=\\\".*?\\\""+spaceEol+"+WITH title=\\\".*?\\\""+spaceEol+"+WITH message=\\\".*?\\\"";
        String txt = "REPORT BY PUSHOVER to \"uPCrexdCXkyWg5EirDomUBc5erxjWG\"\n" +
                "     WITH token=\"asEkV6yeh69w8fS8vxGo19eWq2bJjS\"\n" +
                "     WITH title=\"test RCFACE\"\n" +
                "     WITH message=\"Status (nouveau, en cours, en erreur, traitée):\n" +
                "                PR {{RCFACE_PR_STG_NOUVEAU}}/{{RCFACE_PR_STG_EN_COURS}}/{{RCFACE_PR_MST_EN_ERREUR}}/{{RCFACE_PR_TRAITEE}}\n" +
                "                PP {{RCFACE_PP_STG_NOUVEAU}}/{{RCFACE_PP_STG_EN_COURS}}/{{RCFACE_PP_MST_EN_ERREUR}}/{{RCFACE_PP_TRAITEE}}\n" +
                "                VA {{RCFACE_VA_STG_NOUVEAU}}/{{RCFACE_VA_STG_EN_COURS}}/{{RCFACE_VA_MST_EN_ERREUR}}/{{RCFACE_VA_TRAITEE}}\n" +
                "                IN {{RCFACE_IN_STG_NOUVEAU}}/{{RCFACE_IN_STG_EN_COURS}}/{{RCFACE_IN_MST_EN_ERREUR}}/{{RCFACE_IN_TRAITEE}}\"";*/
//        SyntaxHelper.checkSyntax(grammar, pattern, txt);
//    }

    @Test
    public void testLocalRep() throws IOException, ConnectorException {
        Path path = Paths.get("dsi/RCFACEDbReportByPushover.txt");
        new Story(grammar, new FileInputStream(path.toFile()), "RCFACEDbReportByPushover.txt");
    }

//    @Test
//    public void testX() {
//        String txt="REPORT BY PUSHOVER to \"uPCrexdCXkyWg5EirDomUBc5erxjWG\"\n" +
//                "        WITH token=\"asEkV6yeh69w8fS8vxGo19eWq2bJjS\"\n" +
//                "        WITH title=\"test RCFACE\"\n" +
//                "        WITH message=\"Status (nouveau, en cours, en erreur, traitée):\n" +
//                "        PR {{RCFACE_PR_STG_NOUVEAU}}/{{RCFACE_PR_STG_EN_COURS}}/{{RCFACE_PR_MST_EN_ERREUR}}/{{RCFACE_PR_TRAITEE}}\n" +
//                "        PP {{RCFACE_PP_STG_NOUVEAU}}/{{RCFACE_PP_STG_EN_COURS}}/{{RCFACE_PP_MST_EN_ERREUR}}/{{RCFACE_PP_TRAITEE}}\n" +
//                "        VA {{RCFACE_VA_STG_NOUVEAU}}/{{RCFACE_VA_STG_EN_COURS}}/{{RCFACE_VA_MST_EN_ERREUR}}/{{RCFACE_VA_TRAITEE}}\n" +
//                "        IN {{RCFACE_IN_STG_NOUVEAU}}/{{RCFACE_IN_STG_EN_COURS}}/{{RCFACE_IN_MST_EN_ERREUR}}/{{RCFACE_IN_TRAITEE}}\"";
//        String pattern="REPORT BY PUSHOVER to \"(.*?)\"[\\s+\\r\\n]+WITH token=\"(.*?)\"[\\s+\\r\\n]+WITH title=\"(.*?)\"[\\s+\\r\\n]+WITH message=\"(.*?)\"";
//        SyntaxHelper.checkSyntax(grammar, pattern, txt);
//    }

}

