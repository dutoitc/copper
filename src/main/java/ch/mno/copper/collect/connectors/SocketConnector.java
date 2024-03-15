package ch.mno.copper.collect.connectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * Created by dutoitc on 30.01.2016.
 */
@Slf4j
public class SocketConnector extends AbstractConnector {


    private String host;
    private int port;
    private int timeoutMSec;
    public enum CONNECTION_CHECK { OK, UNKNOWN_HOST, IO_EXCEPTION, UNKNOWN }
    private Exception lastException; // Not thread-safe

    public SocketConnector(String host, int port, int timeoutMSec) {
        this.host = host;
        this.port = port;
        this.timeoutMSec = timeoutMSec;
    }

    /** Not thread-safe */
    public Exception getLastException() {
        return lastException;
    }

    public CONNECTION_CHECK checkConnection() {
        // create a socket
        InetAddress inteAddress = null;
        try (Socket socket = new Socket())
        {
            inteAddress = InetAddress.getByName(host);
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);

            // this method will block no more than timeout ms.
            socket.connect(socketAddress, timeoutMSec);
            if (socket.isConnected()) {
                return CONNECTION_CHECK.OK;
            }
            return CONNECTION_CHECK.UNKNOWN;
        } catch (UnknownHostException e) {
            lastException = e;
            return CONNECTION_CHECK.UNKNOWN_HOST;
        } catch (IOException e) {
            lastException = e;
            log.error("IOException connecting to {}: {}, resolved to {}", host, port, (inteAddress==null?"null":inteAddress.getHostName()));
            return CONNECTION_CHECK.IO_EXCEPTION;
        }
    }

}