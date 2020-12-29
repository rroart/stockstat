package roart.common.util;

import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.constants.CommunicationConstants;
import roart.common.constants.EurekaConstants;

public class ServiceConnectionUtil {

    public Pair<String, String> getCommunicationConnection(String service, String services, String communications) {
        Map<String, String> serviceMap = JsonUtil.convert(services, Map.class);
        Map<String, String> communicationsMap = JsonUtil.convert(communications, Map.class);
        String communication = serviceMap.get(service);
        if (communication == null) {
            communication = CommunicationConstants.REST;
        }
        String connection = communicationsMap.get(communication);
        if (connection == null) {
            connection = "localhost";
        }
        if (communication.equals(CommunicationConstants.REST)) {
            connection = getUrl(service);
        }
        return new ImmutablePair<>(communication, connection);
    }

    public String getAHostname() {
        String hostname = System.getenv(EurekaConstants.MYASERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYASERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public String getAPort() {
        String port = System.getenv(EurekaConstants.MYAPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYAPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    public String getIHostname() {
        String hostname = System.getenv(EurekaConstants.MYISERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYISERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public String getIPort() {
        String port = System.getenv(EurekaConstants.MYIPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYIPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    public String getHostname() {
        String hostname = System.getenv(EurekaConstants.MYSERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYSERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public String getPort() {
        String port = System.getenv(EurekaConstants.MYPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    private String getUrl(String service) {
        String url = "http://" + getHostname() + ":" + getPort() + "/" + service;
        if (service.startsWith("i")) {
            url = "http://" + getAHostname() + ":" + getAPort() + "/" + service;
        }
        return url;
    }
}
