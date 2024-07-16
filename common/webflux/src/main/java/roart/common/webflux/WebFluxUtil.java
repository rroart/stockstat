package roart.common.webflux;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import roart.common.constants.EurekaConstants;
import roart.common.util.MathUtil;

public class WebFluxUtil {
    private static Logger log = LoggerFactory.getLogger(WebFluxUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static int retry = 3;
    
    private static int interval = 60;

    public static <T> T sendMe(Class<T> myclass, Object param, String host, String port, String path) {
        String url = "http://" + host + ":" + port + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendCMe(Class<T> myclass, Object param, String path) {
        String url = "http://" + getHostname() + ":" + getPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendSMe(Class<T> myclass, Object param, String path) {
        String url = "http://" + getSHostname() + ":" + getSPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendMMe(Class<T> myclass, Object param, String path) {
        String url = "http://" + getMHostname() + ":" + getMPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendIMe(Class<T> myclass, Object param, String path) {
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendIMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getIHostname() + ":" + getIPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendAMe(Class<T> myclass, Object param, String path) {
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }
    
    public static <T> T sendAMe(Class<T> myclass, Object param, String path, ObjectMapper objectMapper) {
        String url = "http://" + getAHostname() + ":" + getAPort() + "/" + path;
        return sendMeInner(myclass, param, url, objectMapper);
    }

    public static <T> T sendEMe(Class<T> clazz, Object param, String path) {
        String url = "http://" + getEHostname() + ":" + getEPort() + "/" + path;
        return sendMeInner(clazz, param, url, objectMapper);
    }

    public static <T> T sendMe(Class<T> myclass, Object param, String url) {
        return sendMeInner(myclass, param, url, null);
    }
    
    public static <T> T sendMe(Class<T> myclass, String url, Object param, String path) {
        return sendMeInner(myclass, param, url + path, null);
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
        WebClient webClient = WebClient
                .builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(-1))
                        .build())
                .clientConnector(new ReactorClientHttpConnector())
                .build();
        ResponseSpec response = webClient
                .post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(param))
                .retrieve();
        Mono<T> mono = response
                .bodyToMono(myclass)
                .onErrorMap(Exception::new);
        T result;
        if (retry == 0) {
            result = mono
                    .block();
        } else {
            result = mono
                    .retryWhen(Retry.fixedDelay(retry, Duration.ofSeconds(interval)))
                    .block();
        }
        //int len = response.bodyToMono(String.class).onErrorMap(Exception::new).block().length();
        
        webClient.delete();
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

    public static String getSHostname() {
        String hostname = System.getenv(EurekaConstants.MYSSERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYSSERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getSPort() {
        String port = System.getenv(EurekaConstants.MYSPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYSPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    public static String getEHostname() {
        String hostname = System.getenv(EurekaConstants.MYESERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYESERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getEPort() {
        String port = System.getenv(EurekaConstants.MYEPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYEPORT);
        }
        if (port == null) {
            port = EurekaConstants.HTTP;
        }
        return port;
    }

    public static String getMHostname() {
        String hostname = System.getenv(EurekaConstants.MYMSERVER.toUpperCase());
        if (hostname == null) {
            hostname = System.getProperty(EurekaConstants.MYMSERVER);
        }
        if (hostname == null) {
            hostname = EurekaConstants.LOCALHOST;
        }
        return hostname;
    }
    
    public static String getMPort() {
        String port = System.getenv(EurekaConstants.MYMPORT.toUpperCase());
        if (port == null) {
            port = System.getProperty(EurekaConstants.MYMPORT);
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
