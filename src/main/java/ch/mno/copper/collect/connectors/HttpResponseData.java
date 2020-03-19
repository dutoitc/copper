package ch.mno.copper.collect.connectors;


/**
 * Created by xsicdt on 12/10/17.
 */
public class HttpResponseData<O> {

    private O o;
    private int responseCode;
    private String contentLength;
    private String contentType;

    public void setData(O o) {
        this.o = o;
    }

    public O getData() {
        return o;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
