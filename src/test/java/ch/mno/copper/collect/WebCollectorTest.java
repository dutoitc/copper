package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.HttpResponseData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by xsicdt on 25/08/17.
 */
public class WebCollectorTest {

    @Test
    public void test() {
        String json =
                "{\n" +
                        "  \"_class\": \"hudson.model.ListView\",\n" +
                        "  \"description\": null,\n" +
                        "  \"jobs\": [\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-compile\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-compile/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-defcon\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-defcon/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-DeliverToCEI\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-DeliverToCEI/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-IN\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-IN/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-Sonar\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-Sonar/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-Tests\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-Tests/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"_class\": \"hudson.maven.MavenModuleSet\",\n" +
                        "      \"name\": \"BLOCK1-UT\",\n" +
                        "      \"url\": \"http://sisyphe:1234/job/BLOCK1-UT/\",\n" +
                        "      \"color\": \"blue\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"name\": \"BLOCK1\",\n" +
                        "  \"property\": [\n" +
                        "    \n" +
                        "  ],\n" +
                        "  \"url\": \"http://sisyphe:1234/view/BLOCK1/\"\n" +
                        "}";

//        net.minidev.json.JSONArray res2 = JsonPath.readInstant(json, "$['jobs'][?(@['name']=='ATEV-compile')]");
//        net.minidev.json.JSONArray res2 = JsonPath.readInstant(json, "");

        // TODO: json ko here, but ok in http://jsonpath.com/
        List<Pair<String, String>> valuesKept = new ArrayList<>();
        valuesKept.add(new ImmutablePair<>(".jobs[?(@.name=='BLOCK1-compile')].color", "ATEV_compile"));
        valuesKept.add(new ImmutablePair<>("responseCode", "code"));
        valuesKept.add(new ImmutablePair<>("contentType", "contentType"));
        valuesKept.add(new ImmutablePair<>("contentLength", "contentLength"));


        HttpResponseData<String> d = new HttpResponseData<>();
        d.setData(json);
        d.setResponseCode(200);
        d.setContentType("text/plain");
        d.setContentLength("123");

        List<String> res = WebCollector.extractValues(d, valuesKept);
        assertEquals(4, res.size());
        assertEquals("blue", res.get(0));
        assertEquals("200", res.get(1));
        assertEquals("text/plain", res.get(2));
        assertEquals("123", res.get(3));
    }

    @Test
    public void testRegexp() {
        String json = "{\"a\":{\"b\":{\n" +
                "      \"485\": {\n" +
                "        \"Version\": \"18.11.0\",\n" +
                "        \"State\": \"Active\",\n" +
                "        \"Symbolic Name\": \"RelanceTraitementOperation\",\n" +
                "        \"ID\": 485,\n" +
                "        \"Start Level\": 80,\n" +
                "        \"Name\": \"RelanceTraitementOperation\",\n" +
                "        \"Update Location\": \"mvn:ch.sisyphe.service.G_Technique/RelanceTraitementOperation/18.11.0\"\n" +
                "      },\n" +
                "      \"486\": {\n" +
                "        \"Version\": \"18.11.5\",\n" +
                "        \"State\": \"Active\",\n" +
                "        \"Symbolic Name\": \"WS_Chose_V3\",\n" +
                "        \"ID\": 486,\n" +
                "        \"Start Level\": 80,\n" +
                "        \"Name\": \"WS_Chose_V3\",\n" +
                "        \"Update Location\": \"mvn:ch.sisyphe.service.G_Technique/WS_Chose_V3/18.11.5\"\n" +
                "      },\n" +
                "      \"486\": {\n" +
                "        \"Version\": \"18.11.5\",\n" +
                "        \"State\": \"Active\",\n" +
                "        \"Symbolic Name\": \"WS_Bidule_V3\",\n" +
                "        \"ID\": 486,\n" +
                "        \"Start Level\": 80,\n" +
                "        \"Name\": \"WS_Bidule_V3\",\n" +
                "        \"Update Location\": \"mvn:ch.sisyphe.service.G_Technique\\/WS_Bidule_V3\\/18.11.5\"\n" +
                "      },\n" +
                "      \"487\": {\n" +
                "        \"Version\": \"18.11.0\",\n" +
                "        \"State\": \"Active\",\n" +
                "        \"Symbolic Name\": \"WS_Truc\",\n" +
                "        \"ID\": 487,\n" +
                "        \"Start Level\": 80,\n" +
                "        \"Name\": \"WS_NoticeRequestREEValidate\",\n" +
                "        \"Update Location\": \"mvn:ch.sisyphe.service.F_Publication.B_WebService/WS_Truc/18.11.0\"\n" +
                "      }\n" +
                "}}}";

        List<Pair<String, String>> valuesKept = new ArrayList<>();
        valuesKept.add(new ImmutablePair<>("regexp:WS_Chose_V3.(?<capture>\\d+\\.\\d+\\.\\d+)", "value"));
        //valuesKept.add(new ImmutablePair("$..*[?(@.Name=='WS_Chose_V3')].Version", "value2"));
        valuesKept.add(new ImmutablePair<>("regexp:WS_Bidule_V3..?(?<capture>\\[123]d\\.\\d+\\.\\d+)", "value3"));


        HttpResponseData<String> d = new HttpResponseData<>();
        d.setData(json);
        d.setResponseCode(200);
        d.setContentType("text/plain");
        d.setContentLength("123");

        List<String> res = WebCollector.extractValues(d, valuesKept);
        assertEquals(2, res.size());
        assertEquals("18.11.5", res.get(0));
        // assertEquals("18.11.5", res.get(1));
        assertEquals("?", res.get(1));

    }


}
