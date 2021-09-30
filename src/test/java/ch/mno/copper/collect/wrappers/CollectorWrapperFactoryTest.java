package ch.mno.copper.collect.wrappers;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import static org.junit.jupiter.api.Assertions.*;

class CollectorWrapperFactoryTest {

    private static StoryGrammar storyGrammar;

    @BeforeAll
    static void setup() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testBinary() {
        String story = "BINARY_CHECK\n" +
                "   CHECK_BY_WHICH find AS FIND_AVAILABLE\n" +
                "   CHECK_BY_PATH /usr/bin/ls AS LS_AVAILABLE\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertEquals(BinaryCollectorWrapper.class, ret.getClass());
    }

    @Test
    void testJDBC() {
        String story = "RUN ON CRON * * * * *\n" +
                "GIVEN COLLECTOR JDBC\n" +
                "    WITH url=jdbc://something,user=${user},password=${pass}\n" +
                "QUERY \"select something from somewhere as myVar3\"\n" +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertEquals(JdbcCollectorWrapper.class, ret.getClass());
    }

    @Test
    void testJMX() {
        String story = "GIVEN COLLECTOR JMX WITH url=service:jmx:rmi://src2737v.myhost:44444/jndi/rmi://src2737v.myhost:1099/karaf-trun,user=${user},password=${pass}\n" +
                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION\n" +
                "WHEN CRON */5 7-18 * * 1-5\n" +
                "THEN STORE VALUES\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertEquals(JmxCollectorWrapper.class, ret.getClass());
    }

    @Test
    void testSocket() {
        String story = "GIVEN SOCKET WITH host=localhost,port=666,timeout_ms=1000\n" +
                "    KEEP status AS myStatus\n" +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertEquals(SocketCollectorWrapper.class, ret.getClass());
    }

    @Test
    void testWEB() {
        String story = "GIVEN COLLECTOR WEB WITH url=http://hostname:8040/jolokia/exec/org.apache.karaf:type=bundles,name=trun/list,user=myuser,password=mypass\n" +
                "    KEEP $.value[?(/value.WS_Services$/.test(@.Name))].Version AS value_VERSION\n" +
                "THEN STORE VALUES";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertEquals(WebCollectorWrapper.class, ret.getClass());
    }

    @Test
    void testStoredValue() {
        String story = "RUN ON CRON 0 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "THEN REPORT BY CSV to \"RCFACE-store.csv\"\n" +
                "     WITH header=\"DATETIME;RCFACE_PR_DB_CH_AK;RCFACE_PR_DB_UID_NOT_FOUND;RCFACE_PR_DB_NOINFO;RCFACE_PR_DB_DOC;RCFACE_PR_DB_IDERR019;RCFACE_PR_DB_ERRORS;RCFACE_IN_STG_NOUVEAU;RCFACE_IN_STG_EN_COURS;RCFACE_IN_MST_EN_ERREUR;RCFACE_IN_MST_TRAITEE;RCFACE_IN_PUBLISHED_1;RCFACE_IN_PUBLISHED_2;RCFACE_VA_STG_NOUVEAU;RCFACE_VA_STG_EN_COURS;RCFACE_VA_MST_EN_ERREUR;RCFACE_VA_MST_TRAITEE;RCFACE_VA_PUBLISHED_1;RCFACE_VA_PUBLISHED_2;RCFACE_PP_STG_NOUVEAU;RCFACE_PP_STG_EN_COURS;RCFACE_PP_MST_EN_ERREUR;RCFACE_PP_MST_TRAITEE;RCFACE_PP_PUBLISHED_1;RCFACE_PP_PUBLISHED_2;RCFACE_PR_STG_NOUVEAU;RCFACE_PR_STG_EN_COURS;RCFACE_PR_MST_EN_ERREUR;RCFACE_PR_MST_TRAITEE;RCFACE_PR_PUBLISHED_1;RCFACE_PR_PUBLISHED_2\"\n" +
                "     WITH line=\"v1\"\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        var ret = new CollectorWrapperFactory(propertyResolver, storyGrammar).build(story);
        assertNull(ret);
    }

    @Test
    void testWrongPattern() {
        String story = "RUN ON CRON 0 * * * *\n";

        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        CollectorWrapperFactory factory = new CollectorWrapperFactory(propertyResolver, storyGrammar);
        assertThrows(RuntimeException.class, () -> factory.build(story));
    }
}
