package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by xsicdt on 29/02/16.
 */
public class WebCollectorWrapperTest {

    private StoryGrammar storyGrammar;

    @Before
    public void init() throws FileNotFoundException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    public void test() {
            String jmx = "GIVEN COLLECTOR WEB WITH url=http://localhost:1530/ws/infra/status\n" +
                    "    KEEP status AS WEB_STATUS\n" +
                    "    KEEP lastReload AS WEB_LAST_RELOAD\n" +
                    "WHEN CRON */5 7-18 * * 1-5\n" +
                    "THEN STORE VALUES\n";
        WebCollectorWrapper wrapper = WebCollectorWrapper.buildCollector(storyGrammar, jmx);
        Assert.assertEquals(2, wrapper.valuesKept.size());
        Assert.assertEquals("status", wrapper.valuesKept.get(0).getKey());
        Assert.assertEquals("WEB_STATUS", wrapper.valuesKept.get(0).getValue());
        Assert.assertEquals("lastReload", wrapper.valuesKept.get(1).getKey());
        Assert.assertEquals("WEB_LAST_RELOAD", wrapper.valuesKept.get(1).getValue());
        Assert.assertEquals("[status, lastReload]", StringUtils.join(wrapper.getAs()));

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
    public void test2() {
        String jmx = "GIVEN COLLECTOR WEB WITH url=http://dummy_hostname:8040/jolokia/exec/org.apache.karaf:type=bundles,name=trun/list\n" +
                "    KEEP $.value[?(/value.WS_Services$/.test(@.Name))].Version AS value_VERSION\n" +
                "THEN STORE VALUES";
        WebCollectorWrapper wrapper = WebCollectorWrapper.buildCollector(storyGrammar, jmx);

        // Local wrapper test
        try {
            Map<String, String> res = wrapper.execute();
            String status = res.get("WEB_STATUS");
            String lastReload = res.get("WEB_LAST_RELOAD");
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTemp() {
//        String jsonPath = "$.value[?(/app.WS_Services$/.test(@.Name))].Version";
//        String jsonPath = "$['value'][?(@['Name']=='app.WS_Services')].Version";
        String jsonPath = "$.value.*.[?(@['Name']=='app.WS_Services')].Version";

//        "$['jobs'][?(@['name']=='ATEV-compile')]
        String json="{" +
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