package ch.mno.copper.collect.connectors;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class HttpConnectorTest {

    final static int PORT = 35742;

    @Test
    public void test1() throws Exception {
        try (
                HttpConnector conn = new HttpConnector("localhost", PORT, "http");
                WebServer ws = new WebServer();
        ) {
            ws.start();
            String value = conn.get("/ping1");
            Assert.assertEquals("pong1", value);
        }
    }

    @Test
    public void test2() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", PORT + 1, "http")) {
            conn.get("/something");
            Assert.fail("Should raise an exception");
        } catch (ConnectorException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }


    // Miniserver from http://www.java2s.com/Code/Java/Network-Protocol/ASimpleWebServer.htm
    private static class WebServer implements Runnable, AutoCloseable {
        ServerSocket s;
        Thread thread;
        boolean stopAsked = false;

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
//            System.out.println("Webserver starting up on port " + PORT);
            try {
                // create the main server socket
                s = new ServerSocket(PORT);
            } catch (Exception e) {
                System.out.println("Error: " + e);
                return;
            }

//            System.out.println("Waiting for connection");
            while (!stopAsked) {
                try {
                    // wait for a connection
                    Socket remote = s.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                    PrintWriter out = new PrintWriter(remote.getOutputStream());

                    // Read until blank line (end of HTTP Header)
                    String str = ".";
                    String sent = "";
                    while (!str.equals("")) {
                        str = in.readLine();
                        sent += str + "\r\n";
                    }

                    // Send the response
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type: text/html");
                    out.println("Server: Bot");
                    out.println(""); // End of headers

                    // Send the HTML page
                    if (sent.contains("ping1")) {
                        out.println("pong1");
                    } else if (sent.contains("ping2")) {
                        out.println("pong2");
                    } else {
                        out.println("Unknown query: " + sent);
                    }
                    out.flush();
                    remote.close();
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }

            System.out.println("Stopping...");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Stopped");
        }


        @Override
        public void close() throws Exception {
            stopAsked = true;
            Thread.sleep(10);
            thread.interrupt();
        }
    }

}
