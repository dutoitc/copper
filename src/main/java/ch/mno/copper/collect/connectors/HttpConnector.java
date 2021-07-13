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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 30.01.2016.
 */
public class HttpConnector extends AbstractConnector {

    private static final Logger LOG = LoggerFactory.getLogger(HttpConnector.class);
    public static final String EXCEPTION = "Exception: ";
    public static final String ERROR = "Error ";

    private CloseableHttpClient httpclient;
    private HttpHost target;
    private final String connInfo;

    public HttpConnector(String hostname, int port, String scheme) {
        this(hostname, port, scheme, null, -1, null, null, null);
    }


    public HttpConnector(String hostname, int port, String scheme, String username, String password) {
        this(hostname, port, scheme, null, -1, null, username, password);
    }

    public HttpConnector(String hostname, int port, String scheme, String proxyHostname, int proxyPort, String proxyScheme, String username, String password) {
        connInfo = String.format("%s %s %s / %s %s %s / %s %s...", hostname, port, scheme, proxyHostname, proxyPort, proxyScheme, username, password==null?"":password.subSequence(0,3));

        var httpClientBuilder = HttpClientBuilder.create();
        if (proxyHostname != null) {
            var proxy = new HttpHost(proxyHostname, proxyPort, proxyScheme);
            httpClientBuilder.setProxy(proxy);
        }
        if (username != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            var credentials
                    = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }

        // Trust all certificate, as Copper do only read values
        try {
            var builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (x509Certificates, s) -> true); // Trust everyone
            var sslsf = new SSLConnectionSocketFactory(builder.build());
            httpClientBuilder.setSSLSocketFactory(sslsf);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        httpclient = httpClientBuilder.build();
        target = new HttpHost(hostname, port, scheme);
    }

    public String post(String uri, Map<String, String> values) throws ConnectorException {
        var post = new HttpPost(uri);
        final List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        post.setEntity(new UrlEncodedFormEntity(nvps, Charset.defaultCharset()));

        try (CloseableHttpResponse response = httpclient.execute(target, post)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                return ERROR + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException(EXCEPTION + e.getMessage(), e);
        }
    }

    public String get(String uri) throws ConnectorException {

        var request = new HttpGet(uri);

        try (CloseableHttpResponse response = httpclient.execute(target, request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorResponse(uri, response);
                return  ERROR + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase();
            }
            return EntityUtils.toString(response.getEntity()).trim();
        } catch (IOException e) {
            throw new ConnectorException(EXCEPTION + e.getMessage(), e);
        }
    }

    private void logErrorResponse(String uri, CloseableHttpResponse response) throws IOException {
        LOG.error("Error reading get on {}: {} {}", uri, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        if (LOG.isErrorEnabled()) {
            LOG.error(EntityUtils.toString(response.getEntity()));
        }
        LOG.error("  - connInfo {}", connInfo);
        for (Header header: response.getAllHeaders()) {
            LOG.error("  - {} {}", header.getName(), header.getValue());
        }
    }

    public HttpResponseData<String> get2(String uri) throws ConnectorException {

        var request = new HttpGet(uri);

        try (CloseableHttpResponse response = httpclient.execute(target, request)) {

            HttpResponseData<String> data = new HttpResponseData<>();
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorResponse(uri, response);
                data.setData(ERROR + response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase());
            } else {
                data.setData(EntityUtils.toString(response.getEntity()).trim());
            }
            data.setResponseCode(response.getStatusLine().getStatusCode());

            var lastHeader = response.getLastHeader("Content-Length");
            if (lastHeader != null) {
                data.setContentLength(lastHeader.getValue());
            }


            var lastHeader1 = response.getLastHeader("Content-Type");
            if (lastHeader1 != null) {
                data.setContentType(lastHeader1.getValue());
            }

            return data;
        } catch (IOException e) {
            LOG.error(EXCEPTION + e.getMessage(), e);
            throw new ConnectorException(EXCEPTION + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            httpclient.close();
        } catch (IOException e) {
            // Nothing yet
        }
    }


}
