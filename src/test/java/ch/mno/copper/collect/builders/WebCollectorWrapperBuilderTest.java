package ch.mno.copper.collect.builders;

import ch.mno.copper.AbstractWebPortSpringTest;
import ch.mno.copper.collect.wrappers.WebCollectorWrapper;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by xsicdt on 29/02/16.
 */
class WebCollectorWrapperBuilderTest extends AbstractWebPortSpringTest {

    private StoryGrammar storyGrammar;
    private WebCollectorWrapperBuilder builder;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        Mockito.when(propertyResolver.getProperty("user")).thenReturn("username");
        Mockito.when(propertyResolver.getProperty("pass")).thenReturn("password");
        builder = new WebCollectorWrapperBuilder(storyGrammar, propertyResolver);
    }

    @Test
    void test() {
        String jmx = "GIVEN COLLECTOR WEB WITH url=http://localhost:1530/ws/infra/status\n" +
                "    KEEP status AS WEB_STATUS\n" +
                "    KEEP lastReload AS WEB_LAST_RELOAD\n" +
                "WHEN CRON */5 7-18 * * 1-5\n" +
                "THEN STORE VALUES\n";
        WebCollectorWrapper wrapper = builder.buildCollector(jmx);
        List<Pair<String, String>> valuesKept = wrapper.getValuesKept();
        assertEquals(2, valuesKept.size());
        assertEquals("status", valuesKept.get(0).getKey());
        assertEquals("WEB_STATUS", valuesKept.get(0).getValue());
        assertEquals("lastReload", valuesKept.get(1).getKey());
        assertEquals("WEB_LAST_RELOAD", valuesKept.get(1).getValue());
        assertEquals("[status, lastReload]", StringUtils.join(wrapper.getAs()));

        /*
        // Local wrapper test
        try {
            Map<String, String> res = wrapper.execute();
            String status = res.get("WEB_STATUS");
            String lastReload = res.get("WEB_LAST_RELOAD");
            System.out.println("Values: " + status + "," + lastReload);
        } catch (ConnectorException e) {
            e.printStackTrace();
        }*/
    }


    @Test
    void test2() {
        String jmx = "GIVEN COLLECTOR WEB WITH url=http://hostname:8040/jolokia/exec/org.apache.karaf:type=bundles,name=trun/list,user=myuser,password=mypass\n" +
                "    KEEP $.value[?(/value.WS_Services$/.test(@.Name))].Version AS value_VERSION\n" +
                "THEN STORE VALUES";
        WebCollectorWrapper wrapper = builder.buildCollector(jmx);

        // Local wrapper test
        List<Pair<String, String>> valuesKept = wrapper.getValuesKept();
        assertEquals("$.value[?(/value.WS_Services$/.test(@.Name))].Version", valuesKept.get(0).getKey());
        assertEquals("value_VERSION", valuesKept.get(0).getValue());
        assertEquals("myuser", wrapper.getUsername());
        assertEquals("mypass", wrapper.getPassword());
        assertEquals("http://hostname:8040/jolokia/exec/org.apache.karaf:type=bundles,name=trun/list", wrapper.getUrl());
    }


    @Test
    void testLocal() {
        String jmx = "GIVEN COLLECTOR WEB WITH url=http://localhost:" + port + "/ping1\n" +
                "    KEEP responseCode AS WEB_STATUS\n" +
                "    KEEP body AS BODY\n" +
                "WHEN CRON */5 7-18 * * 1-5\n" +
                "THEN STORE VALUES\n";
        WebCollectorWrapper wrapper = builder.buildCollector(jmx);

        // Local wrapper test
        Map<String, String> res = wrapper.execute();
        String status = res.get("WEB_STATUS");
        String body = res.get("BODY");
        System.out.println("Values: " + status + "," + body);
        assertEquals("pong1", body);
        assertEquals("200", status);
    }


    @Test
    @Disabled("to be reworked")
    void testTemp() {
//        String jsonPath = "$.value[?(/app.WS_Services$/.test(@.Name))].Version";
//        String jsonPath = "$['value'][?(@['Name']=='app.WS_Services')].Version";
        String jsonPath = "$.value.*.[?(@['Name']=='app.WS_Services')].Version";

//        "$['jobs'][?(@['name']=='ATEV-compile')]
        String json = "{" +
                "  \"timestamp\": 1503928437,\n" +
                "  \"status\": 200,\n" +
                "  \"request\": {\n" +
                "    \"operation\": \"list\",\n" +
                "    \"mbean\": \"org.apache.karaf:name=trun,type=bundles\",\n" +
                "    \"type\": \"exec\"\n" +
                "  },\n" +
                "  \"value\": {\n" +
                "    \"0\": {\n" +
                "      \"Name\": \"app.WS_Services\",\n" +
                "      \"Blueprint\": \"\",\n" +
                "      \"State\": \"ACTIVE\",\n" +
                "      \"Start Level\": 0,\n" +
                "      \"ID\": 0,\n" +
                "      \"Version\": \"3.8.0.v20120529-1548\"\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "      \"Name\": \"org.ops4j.pax.url.mvn\",\n" +
                "      \"Blueprint\": \"\",\n" +
                "      \"State\": \"ACTIVE\",\n" +
                "      \"Start Level\": 5,\n" +
                "      \"ID\": 1,\n" +
                "      \"Version\": \"1.3.7\"\n" +
                "    }}}";
        net.minidev.json.JSONArray res = JsonPath.read(json, jsonPath);
        System.out.println(res.toJSONString());
    }

}