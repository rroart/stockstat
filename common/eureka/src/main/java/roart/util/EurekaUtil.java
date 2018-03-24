package roart.util;

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
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

public class EurekaUtil {

    private static Logger log = LoggerFactory.getLogger(EurekaUtil.class);

    public static EurekaClient eurekaClient = null;
    public static DiscoveryClient discoveryClient = null;

    public static EurekaClient initEurekaClient() {
        DiscoveryManager.getInstance().initComponent(
                new MyDataCenterInstanceConfig(),
                new DefaultEurekaClientConfig());
        eurekaClient = DiscoveryManager.getInstance()
                .getEurekaClient();

        if (eurekaClient != null) {
            System.out.println("euClient " + eurekaClient.getAllKnownRegions());
            List<Application> apps = eurekaClient.getApplications().getRegisteredApplications();
            for (Application app : apps) {
                System.out.println("currently available app " + app.getName()); 
            }
        }
        discoveryClient = DiscoveryManager.getInstance().getDiscoveryClient();		
        if (discoveryClient != null) {
            List<Application> apps = discoveryClient.getApplications().getRegisteredApplications();
            for (Application app : apps) {
                System.out.println("currently available app2 " + app.getName()); 
            }
        }
        try {
            // more example code:
            /*
			String vipAddress = "LUCENE";
			InstanceInfo nextServerInfo = DiscoveryManager.getInstance()
					.getEurekaClient()
					.getNextServerFromEureka(vipAddress, false);
			System.out.println("Found an instance of example service to talk to from eureka: "
					+ nextServerInfo.getVIPAddress() + ":" + nextServerInfo.getPort());

			System.out.println("healthCheckUrl: " + nextServerInfo.getHealthCheckUrl());
			System.out.println("override: " + nextServerInfo.getOverriddenStatus());

			System.out.println("Server Host Name "+ nextServerInfo.getHostName() + " at port " + nextServerInfo.getPort() );		

			System.out.println("conf " + discoveryClient.getEurekaClientConfig().getEurekaServerPort());
             */
        } catch (Exception e) {
            log.error("Cannot get an instance of example service to talk to from eureka");
        }
        return eurekaClient;
    }

    public static <T> T sendMe(Class<T> myclass, Object param, String appName, String path) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return sendMe(myclass, param, appName, path, objectMapper);
    }
    
    public static <T> T sendMe(Class<T> myclass, Object param, String appName, String path, ObjectMapper objectMapper) {

        String homePageUrl = null;
        log.info("homePagePre " + appName + " " + path);
        if (discoveryClient != null) {
            List<InstanceInfo> li = discoveryClient.getApplication(appName).getInstances();
            for (InstanceInfo ii : li) {
                log.info("homePage " + ii.getHomePageUrl());
            }
            List<InstanceInfo> li2 = eurekaClient.getApplication(appName).getInstances();
            for (InstanceInfo ii : li2) {
                log.info("homePage2 " + ii.getHomePageUrl());
            }
            if (!li.isEmpty()) {
                homePageUrl = li.get(0).getHomePageUrl();
            }
        } else {
            log.error("discoveryclient is null");
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");

        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(param, headers);
        String url = homePageUrl;
        RestTemplate rt = new RestTemplate();
        for (HttpMessageConverter<?> converter : rt.getMessageConverters()) {
            //System.out.println(converter.getClass().getName());
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                //log.info("setting object ignore");
                // TODO temp fix for extra duplicated arr in json
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setObjectMapper(objectMapper);
            }		
        }
        //System.out.println(rt.getMessageConverters().size());
        ResponseEntity<T> regr = rt.postForEntity(url + path, request, myclass);
        T result = regr.getBody();
        log.info("resultme " + regr.getHeaders().size() + " " + regr.toString());
        return result;
    }

    public static <T> T sendMe(Class<T> myclass, Object param, String url) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");

        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(param, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<T> regr = rt.postForEntity(url, request, myclass);
        T result = regr.getBody();
        log.info("resultme " + regr.getHeaders().size() + " " + regr.toString());
        return result;
    }

}
