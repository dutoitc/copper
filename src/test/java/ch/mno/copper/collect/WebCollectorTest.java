package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.HttpResponseData;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        valuesKept.add(new ImmutablePair(".jobs[?(@.name=='BLOCK1-compile')].color", "ATEV_compile"));
        valuesKept.add(new ImmutablePair<>("responseCode", "code"));
        valuesKept.add(new ImmutablePair<>("contentType", "contentType"));
        valuesKept.add(new ImmutablePair<>("contentLength", "contentLength"));


        HttpResponseData<String> d = new HttpResponseData<>();
        d.setData(json);
        d.setResponseCode(200);
        d.setContentType("text/plain");
        d.setContentLength("123");

        List<String> res = WebCollector.extractValues(d, valuesKept);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("blue", res.get(0));
        Assert.assertEquals("200", res.get(1));
        Assert.assertEquals("text/plain", res.get(2));
        Assert.assertEquals("123", res.get(3));
    }



}
