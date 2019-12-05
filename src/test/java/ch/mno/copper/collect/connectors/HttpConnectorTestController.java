package ch.mno.copper.collect.connectors;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class HttpConnectorTestController {

    @GetMapping("/ping1")
    public String ping1() {
        return "pong1";
    }

    @PostMapping("/repeat")
    public String repeat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sent = IOUtils.toString(request.getInputStream());
        PrintWriter out = new PrintWriter(response.getOutputStream());

        // Read until blank line (end of HTTP Header)
        String str = ".";
        if (sent.contains("err404")) {
            out.println("HTTP/1.0 404 Not Found");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            out.println(""); // End of headers
        } else {
            // Send the response
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            out.println(""); // End of headers
        }

        return sent;
    }
}
