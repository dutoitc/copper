import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xsicdt on 17/02/16.
 */
@Disabled
public class HttpTest {


    public static String post(String uri, Map<String, String> values) throws ConnectorException {
        HttpHost proxy = new HttpHost("localhost", 3128);
        CloseableHttpClient httpclient = HttpClientBuilder.create().setProxy(proxy).build();
        HttpHost target = new HttpHost(uri);


        HttpPost post = new HttpPost(uri);
        final List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> entry: values.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        post.setEntity(new UrlEncodedFormEntity(nvps, Charset.defaultCharset()));
//        post.setConfig(config);

        try (CloseableHttpResponse response = httpclient.execute(target, post)){
            if (response.getStatusLine().getStatusCode()!=200) {
                return "Error " + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException("Exception: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws ConnectorException {
        String url = "https://api.pushover.net/1/messages.json";

        Map<String, String> params = new HashMap<>();
        params.put("token","asEkV6yeh69w8fS8vxGo19eWq2bJjS");
        params.put("user", "uPCrexdCXkyWg5EirDomUBc5erxjWG");
        params.put("title", "aTitle");
        params.put("message", "aMessage");
        post(url, params);

    }


}
