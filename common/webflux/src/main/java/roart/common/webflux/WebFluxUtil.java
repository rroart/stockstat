package roart.common.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import roart.common.constants.EurekaConstants;
import roart.common.util.MathUtil;

public class WebFluxUtil {
    private static Logger log = LoggerFactory.getLogger(WebFluxUtil.class);

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
    
    public static <T> T sendAMe(Class<T> myclass, Object param, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendAMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    public static <T> T sendMe(Class<T> myclass, Object param, String url) {
        return sendMeInner(myclass, param, url, null);
    }
    
    public static <T> T sendMeInner(Class<T> myclass, Object param, String url, ObjectMapper objectMapper) {
        long time = System.currentTimeMillis();
        if (objectMapper != null) {
            ExchangeStrategies jacksonStrategy = ExchangeStrategies.builder()
                    .codecs(config -> {
                        config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                        config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    }).build();
            WebClient.builder().exchangeStrategies(jacksonStrategy);
        }
        WebClient webClient = WebClient.create();
        T result = webClient
                .mutate()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(-1))
                        .build())
                .build()
                .post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromObject(param))
                .retrieve()
                .bodyToMono(myclass)
                .onErrorMap(Exception::new)
                .block();
        
        //Mono.just
        //log.info("resultme " + regr.getHeaders().size() + " " + regr.getHeaders().getContentLength() + " " + regr.toString());
        log.info("Rq time {}s for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), url);
        return result;
    }

    public static String getAHostname() {
        String hostname = System.getenv(EurekaConstants.MYASERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYASERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getAPort() {
        String port = System.getenv(EurekaConstants.MYAPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYAPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
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
