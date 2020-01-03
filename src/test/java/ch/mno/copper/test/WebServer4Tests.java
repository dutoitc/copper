package ch.mno.copper.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ping1 -> pong1
 * ping2 -> pong2
 * repeat -> repeat header
 * err404 -> return error 404
 */
// Miniserver from http://www.java2s.com/Code/Java/Network-Protocol/ASimpleWebServer.htm
    public class WebServer4Tests implements Runnable, AutoCloseable {
        ServerSocket s;
        Thread thread;
        boolean stopAsked = false;
    int port;

    public WebServer4Tests(int port) {
        this.port = port;
    }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
//            System.out.println("Webserver starting up on port " + PORT);
            int nbRetry=5;
            boolean ok = false;
            while (!ok && nbRetry-->0) {
                try {
                    // create the main server socket
                    s = new ServerSocket(port);
                    ok = true;
                } catch (Exception e) {
                    if (e.getMessage().contains("Address already in use")) {
                        System.out.println("Address already in use, waiting some time and retrying");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        System.err.println("Error: " + e + ", retrying");
                    }
                }
            }
            if (!ok) {
                System.err.println("Error: Cannot bind to " + port);
                return;
            }
            System.out.println("Successfully bound to " + port);

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
                    while (str!=null && !str.equals("")) {
                        str = in.readLine();
                        sent += str + "\r\n";
                    }

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
            System.out.println("Webserver stopped at port " + port);
        }
    }