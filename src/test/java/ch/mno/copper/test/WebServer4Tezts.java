package ch.mno.copper.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * ping1 -> pong1
 * ping2 -> pong2
 * repeat -> repeat header
 * err404 -> return error 404
 */
// Miniserver from http://www.java2s.com/Code/Java/Network-Protocol/ASimpleWebServer.htm
public class WebServer4Tezts implements Runnable, AutoCloseable {
    ServerSocket s;
    Thread thread;
    boolean stopAsked = false;
    boolean running = false;
    int port;
    String uuid = UUID.randomUUID().toString();

    /**
     * Port 0 means dynamic port. use getPort()
     */
    public WebServer4Tezts(int port) {
        this.port = port;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
        for (int i=0; i<5 && port==0; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (port==0) {
            throw new RuntimeException("Cannot get HTTP Port dynamic in 500ms");
        }
    }

    @Override
    public void run() {
//            System.out.println("Webserver starting up on port " + PORT);
        int nbRetry = 5;
        boolean ok = false;
        while (!ok && nbRetry-- > 0) {
            try {
                // create the main server socket
                s = new ServerSocket(port);
                if (port == 0) {
                    port = s.getLocalPort();
                }
                ok = true;
            } catch (Exception e) {
                if (e.getMessage().contains("Address already in use")) {
                    System.out.println("[WebServer4Tests " + uuid + "]: Address already in use, waiting some time and retrying");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.err.println("[WebServer4Tests " + uuid + "]: Error: " + e + ", retrying");
                }
            }
        }
        if (!ok) {
            System.err.println("[WebServer4Tests " + uuid + "]: Error: Cannot bind to " + port);
            return;
        }
        System.out.println("[WebServer4Tests " + uuid + "]: Successfully bound to " + port);

//            System.out.println("Waiting for connection");
        running = true;
        while (!stopAsked) {
            try {
                // wait for a connection
                Socket remote = s.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                PrintWriter out = new PrintWriter(remote.getOutputStream());

                // Read until blank line (end of HTTP Header)
                String str = ".";
                StringBuilder sentSB = new StringBuilder();
                while (str != null && !str.equals("")) {
                    str = in.readLine();
                    sentSB.append(str).append("\r\n");
                }

                String sent = sentSB.toString();
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

                    // Send the HTML page
                    if (sent.contains("ping1")) {
                        out.println("pong1");
                    } else if (sent.contains("ping2")) {
                        out.println("pong2");
                    } else if (sent.contains("repeat")) {
                        out.println(sent);
                    } else {
                        out.println("Unknown query: " + sent);
                    }
                }
                out.flush();
                remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
        running = false;

        System.out.println("[WebServer4Tests" + uuid + "]: Stopping...");
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[WebServer4Tests" + uuid + "]: Stopped");
    }

    public int getPort() {
        return port;
    }

    @Override
    public void close() throws Exception {
        stopAsked = true;
        running = false;
        Thread.sleep(10);
        thread.interrupt();
        System.out.println("[WebServer4Tests" + uuid + "]: Webserver at port " + port + ": close");
    }

    public boolean isRunning() {
        return running;
    }
}