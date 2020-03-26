package roart.common.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InetUtil {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean checkMe(String host, int port, int timeout) {
        int exitStatus = 1 ;

        Socket s = null;
        String reason = null ;
        try {
            s = new Socket();
            s.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(host, port);
            s.connect(sa, timeout);
        } catch (IOException e) {
            if ( e.getMessage().equals("Connection refused")) {
                reason = "port " + port + " on " + host + " is closed.";
            };
            if ( e instanceof UnknownHostException ) {
                reason = "node " + host + " is unresolved.";
            }
            if ( e instanceof SocketTimeoutException ) {
                reason = "timeout while attempting to reach node " + host + " on port " + port;
            }
        } finally {
            if (s != null) {
                if ( s.isConnected()) {
                    log.info("Port " + port + " on " + host + " is reachable!");
                    exitStatus = 0;
                } else {
                    log.info("Port " + port + " on " + host + " is not reachable; reason: " + reason );
                }
                try {
                    s.close();
                } catch (IOException e) {
                }
                return s.isConnected();
            }
            return false;
        }
    }

    public String handleServer(String serverurl) {
        String server = serverurl.substring(7);
        String[] serversplit = server.split(":");
        String host = serversplit[0];
        Integer port = serversplit.length > 1 ? Integer.valueOf(serversplit[1]) : 80;
        boolean up = checkMe(host, port, 500);
        if (up) {
            return serverurl;
        }
        return null;
    }
    
    public List<String> getServers(String serverString) {
        String[] servers = serverString.split(",");
        // not parallelStream yet, pri list
        return Arrays.asList(servers).stream().map(server -> handleServer(server)).filter(server -> server != null).collect(Collectors.toList());
    }


}