package roart.parse;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

public class SocketUtil {
    private static Logger log = LoggerFactory.getLogger(SocketUtil.class);

    public static void mylisten() {
        try {
            int portNumber = Integer.parseInt("5555");

            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}
