package ch.mno.copper.collect.connectors;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 30.01.2016.
 */
public class HttpConnector extends AbstractConnector {

    private CloseableHttpClient httpclient;
    private RequestConfig config;
    private HttpHost target;

    public HttpConnector(String hostname, int port, String scheme) {
        this(hostname, port, scheme, null, -1, null);
    }

    public HttpConnector(String hostname, int port, String scheme, String proxyHostname, int proxyPort, String proxyScheme) {
        httpclient = HttpClients.createDefault();
        target = new HttpHost(hostname, port, scheme);
        RequestConfig.Builder builder = RequestConfig.custom();

        if (proxyHostname!=null && proxyPort>-1) {
            HttpHost proxy = new HttpHost(hostname, port, scheme);
            builder.setProxy(proxy);
        }

        config = builder.build();
    }

    public String post(String uri, Map<String, String> values) throws ConnectorException {
        HttpPost post = new HttpPost(uri);
        final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry: values.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        post.setEntity(new UrlEncodedFormEntity(nvps, Charset.defaultCharset()));
        post.setConfig(config);

        try (CloseableHttpResponse response = httpclient.execute(target, post)){
            if (response.getStatusLine().getStatusCode()!=200) {
                return "Error " + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException("Exception: " + e.getMessage(), e);
        }
    }

    public String get(String uri) throws ConnectorException {
        HttpGet request = new HttpGet(uri);
        request.setConfig(config);

        try (CloseableHttpResponse response = httpclient.execute(target, request)){
            if (response.getStatusLine().getStatusCode()!=200) {
                return "Error " + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException("Exception: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            httpclient.close();
        } catch (IOException e) {
        }
    }


}
