package ch.mno.copper.collect.connectors;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
    private HttpHost target;

    public HttpConnector(String hostname, int port, String scheme) {
        this(hostname, port, scheme, null, -1, null, null, null);
    }

    public HttpConnector(String hostname, int port, String username, String password) {
        this(hostname, port, null, null, -1, null, username, password);
    }

    public HttpConnector(String hostname, int port, String scheme, String proxyHostname, int proxyPort, String proxyScheme, String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (proxyHostname!=null) {
            HttpHost proxy = new HttpHost(proxyHostname, proxyPort, proxyScheme);
            httpClientBuilder.setProxy(proxy);
        }
        if (username!=null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }

        httpclient = httpClientBuilder.build();
        target = new HttpHost(hostname, port, scheme);
    }

    public String post(String uri, Map<String, String> values) throws ConnectorException {


        HttpPost post = new HttpPost(uri);
        final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
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

    public String get(String uri) throws ConnectorException {

        HttpGet request = new HttpGet(uri);

        try (CloseableHttpResponse response = httpclient.execute(target, request)){
            if (response.getStatusLine().getStatusCode()!=200) {
                return "Error " + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException("Exception: " + e.getMessage(), e);
        }
    }

    public HttpResponseData<String> get2(String uri) throws ConnectorException {

        HttpGet request = new HttpGet(uri);

        try (CloseableHttpResponse response = httpclient.execute(target, request)){

            HttpResponseData<String> data = new HttpResponseData<>();
            if (response.getStatusLine().getStatusCode()!=200) {
                data.setData("Error " + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase());
            } else {
                data.setData(EntityUtils.toString(response.getEntity()).trim());
            }
            data.setResponseCode(response.getStatusLine().getStatusCode());

            Header lastHeader = response.getLastHeader("Content-Length");
            if (lastHeader!=null) {
                data.setContentLength(lastHeader.getValue());
            }


            Header lastHeader1 = response.getLastHeader("Content-Type");
            if (lastHeader1!=null) {
                data.setContentType(lastHeader1.getValue());
            }

            return data;
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
