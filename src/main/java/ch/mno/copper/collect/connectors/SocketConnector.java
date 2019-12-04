package ch.mno.copper.collect.connectors;

import java.io.IOException;
import java.net.*;

/**
 * Created by dutoitc on 30.01.2016.
 */
public class SocketConnector extends AbstractConnector {

    private String host;
    private int port;
    private int timeoutMSec;
    public enum CONNECTION_CHECK { OK, UNKNOWN_HOST, IO_EXCEPTION, UNKNOWN }

    public SocketConnector(String host, int port, int timeoutMSec) {
        this.host = host;
        this.port = port;
        this.timeoutMSec = timeoutMSec;
    }


    public CONNECTION_CHECK checkConnection() {
        // create a socket
        Socket socket = null;
        try {
            InetAddress inteAddress = InetAddress.getByName(host);
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);

            socket = new Socket();

            // this method will block no more than timeout ms.
            socket.connect(socketAddress, timeoutMSec);
            if (socket.isConnected()) {
                return CONNECTION_CHECK.OK;
            }
            return CONNECTION_CHECK.UNKNOWN;
        } catch (UnknownHostException e) {
            return CONNECTION_CHECK.UNKNOWN_HOST;
        } catch (IOException e) {
            return CONNECTION_CHECK.IO_EXCEPTION;
        } finally {
            if (socket!=null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // Pass
                }
            }
        }
    }

}