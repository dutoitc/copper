package ch.mno.copper.collect.connectors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
