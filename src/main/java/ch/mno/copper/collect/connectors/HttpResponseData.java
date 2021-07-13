package ch.mno.copper.collect.connectors;


/**
 * Created by xsicdt on 12/10/17.
 */
public class HttpResponseData<O> {

    private O o;
    private int responseCode;
    private String contentLength;
    private String contentType;

    public O getData() {
        return o;
    }

    public void setData(O o) {
        this.o = o;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String toHTTP() {
        String body = o.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(responseCode).append("\r\n");
        sb.append("Content-Type: " + contentType + "\r\n");
        sb.append("Content-Length: " + body.length() + "\r\n");
        sb.append("\r\n").append(body);
        return sb.toString();
    }
}
