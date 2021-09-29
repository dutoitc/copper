package ch.mno.copper.collect.connectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpResponseDataTest {

    @Test
    void testAll() {
        HttpResponseData<String> response = new HttpResponseData<>();
        response.setContentLength("184");
        response.setContentType("text/plain");
        response.setData("dummy payload");
        response.setResponseCode(200);
        assertEquals("HTTP/1.1 200\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 13\r\n" +
                "\r\n" +
                "dummy payload",response.toHTTP());
    }

}
