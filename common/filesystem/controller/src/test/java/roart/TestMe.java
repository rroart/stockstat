package roart;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

public class TestMe {

    @Test
    public void test() throws SocketException {
        System.out.println("here");
        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(n)) {    
            System.out.printf("Display name: %s\n", netint.getDisplayName());
            System.out.printf("Name: %s\n", netint.getName());
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                System.out.printf("InetAddress: %s\n", inetAddress);
            }
            System.out.printf("\n");
    }
    }
}
