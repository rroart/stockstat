package roart.eureka.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.constants.EurekaConstants;

public class EurekaUtil {

    private static Logger log = LoggerFactory.getLogger(EurekaUtil.class);

    private static String postfix;
    
    public static <T> T sendMe(Class<T> myclass, Object param, String host, String port, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + host + ":" + port + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendCMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getHostname() + ":" + getPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendIMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendIMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    public static <T> T sendMe(Class<T> myclass, Object param, String url) {
        return sendMeInner(myclass, param, url, null);
    }
    
    public static <T> T sendMeInner(Class<T> myclass, Object param, String url, ObjectMapper objectMapper) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");

        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(param, headers);
        RestTemplate rt = new RestTemplate();
        if (objectMapper != null) {
            for (HttpMessageConverter<?> converter : rt.getMessageConverters()) {
                //System.out.println(converter.getClass().getName());
                if (converter instanceof MappingJackson2HttpMessageConverter) {
                    //log.info("setting object ignore");
                    // temp fix for extra duplicated arr in json
                    MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                    jsonConverter.setObjectMapper(objectMapper);
                }           
            }
        }
        ResponseEntity<T> regr = rt.postForEntity(url, request, myclass);
        T result = regr.getBody();
        log.info("resultme " + regr.getHeaders().size() + " " + regr.toString());
        return result;
    }

    public static String getIHostname() {
        String hostname = System.getenv(EurekaConstants.MYISERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYISERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getIPort() {
        String port = System.getenv(EurekaConstants.MYIPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYIPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    public static String getHostname() {
        String hostname = System.getenv(EurekaConstants.MYSERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYSERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getPort() {
        String port = System.getenv(EurekaConstants.MYPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

}
