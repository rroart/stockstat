package roart.common.communication.rest;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
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
import roart.common.util.MathUtil;
import roart.common.communication.model.Communication;
import roart.common.webflux.WebFluxUtil;

public class REST extends Communication {

    private Logger log = LoggerFactory.getLogger(REST.class);

    private String postfix;
    
    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    public REST(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection, Function<String, Boolean> storeMessage, WebFluxUtil webFluxUtil) {
        super(myname, myclass, service, mapper, send, receive, sendreceive, connection, storeMessage);
        if (webFluxUtil != null) {
            this.webFluxUtil = webFluxUtil;
        }
    }
    
    /*
    public <T> T sendMe(Class<T> myclass, Object param, String host, String port, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + host + ":" + port + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public <T> T sendCMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getHostname() + ":" + getPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public <T> T sendIMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public <T> T sendIMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public <T> T sendAMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public <T> T sendAMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    public <T> T sendMe(Class<T> myclass, Object param, String url) {
        return sendMeInner(myclass, param, url, null);
    }
    
    //@Override
    public <T> T sendReceive2not(Class<T> myclass, Object param) {
        String[] r = receiveString();
        String url = null; //getUrl();
        return sendMeInner(myclass, param, url, mapper);
    }
    */

    private String getUrl() {
        /*
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + service;
        if (service.startsWith("i")) {
            url = "http://" + getIHostname() + ":" + getIPort() + "/" + service;
        }
        return url;*/
        return connection + "/" + getSendService();
    }

    @Override
    public <T> T[] sendReceive(Object param) {
        String url = connection; // getUrl();
        T t = (T) webFluxUtil.sendMeInner(myclass, param, url, mapper);
        T[] ts = (T[]) Array.newInstance(myclass, 1);
        //Object[] ts = new Object[1];
        ts[0] = t;
        return ts;
    }

    public <T> T sendMeInner(Class<T> myclass, Object param, String url, ObjectMapper objectMapper) {
        long time = System.currentTimeMillis();
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
        log.info("resultme " + regr.getHeaders().size() + " " + regr.getHeaders().getContentLength() + " " + regr.toString());
        log.info("Rq time {}s for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), url);
        return result;
    }

    /*
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
    */

    @Override
    public void send(String s) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] receiveString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] receiveStringAndStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
