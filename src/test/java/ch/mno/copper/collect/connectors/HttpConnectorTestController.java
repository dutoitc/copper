package ch.mno.copper.collect.connectors;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class HttpConnectorTestController {

    @GetMapping("/ping1")
    public String ping1() {
        return "pong1";
    }

    @PostMapping("/repeat")
    public String repeat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String sent = IOUtils.toString(request.getInputStream());
        return sent;
    }

    @GetMapping(value = "/query1", produces = MediaType.TEXT_PLAIN_VALUE)
    public String query1() {
        return "Lorem ipsum ipsodec";
    }
}
