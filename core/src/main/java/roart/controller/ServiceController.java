package roart.controller;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.config.MyXMLConfig;
import roart.db.DbDao;
import roart.model.GUISize;
import roart.model.ResultItem;
import roart.queue.MyExecutors;
import roart.service.ControlService;
import roart.service.ServiceParam;
import roart.service.ServiceResult;
import roart.util.EurekaConstants;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ControlService instance;

    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService();
        }
        return instance;
    }

    @RequestMapping(value = "/" + EurekaConstants.SETCONFIG,
            method = RequestMethod.POST)
    public ServiceResult configDb(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            System.out.println("new market" + param.getConfig().getMarket());
            System.out.println("new market" + param.getConfig());
            System.out.println("new some " + param.getConfig().getConfigValueMap().get(ConfigConstants.DATABASESPARKSPARKMASTER));
            //getInstance().config(param.config);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public ServiceResult getConfig(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setConfig(MyXMLConfig.getConfigInstance());
            System.out.println("configs " + result.getConfig());
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETMARKETS,
            method = RequestMethod.POST)
    public ServiceResult getMarkets(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setMarkets(getInstance().getMarkets());
            log.info("Marketsize {}", result.getMarkets().size());
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETDATES,
            method = RequestMethod.POST)
    public ServiceResult getDates(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            getInstance().getDates( new MyMyConfig(param.getConfig()), maps);
            result.setMaps(maps);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETSTOCKS,
            method = RequestMethod.POST)
    public ServiceResult getStocks(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setStocks(getInstance().getStocks(param.getMarket(),  new MyMyConfig(param.getConfig())));
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public ServiceResult getContent(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            result.setList(getInstance().getContent( new MyMyConfig(param.getConfig()), maps, disableList));
            result.setMaps(maps);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTSTAT,
            method = RequestMethod.POST)
    public ServiceResult getContentStat(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setList(getInstance().getContentStat( new MyMyConfig(param.getConfig())));
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTGRAPH,
            method = RequestMethod.POST)
    public ServiceResult getContentGraph(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setList(getInstance().getContentGraph( new MyMyConfig(param.getConfig()), param.getGuiSize()));
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTGRAPH2,
            method = RequestMethod.POST)
    public ServiceResult getContentGraph2(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            // TODO fix quick workaround for serialization
            Set<Pair<String,String>> ids = new HashSet<>();
            for (String union : param.getIds()) {
                String[] idsplit = union.split(",");
                Pair<String, String> pair = new Pair(idsplit[0], idsplit[1]);
                ids.add(pair);
            }
            result.setList(getInstance().getContentGraph( new MyMyConfig(param.getConfig()), ids, param.getGuiSize()));
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETEVOLVERECOMMENDER,
            method = RequestMethod.POST)
    public ServiceResult getEvolveRecommender(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            MyMyConfig aConfig = new MyMyConfig(param.getConfig());
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            Map<String, Map<String, Object>> maps = new HashMap<>();
            Map<String, Object> updateMap = new HashMap<>();
            maps.put("update", updateMap);
            result.setList(getInstance().getEvolveRecommender( aConfig, disableList, updateMap));
            result.setMaps(maps);
            result.setConfig(aConfig);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETEVOLVENN,
            method = RequestMethod.POST)
    public ServiceResult getTestML(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            MyMyConfig aConfig = new MyMyConfig(param.getConfig());
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            Set<String> ids = param.getIds();
            String ml = ids.iterator().next();
            Map<String, Map<String, Object>> maps = new HashMap<>();
            Map<String, Object> updateMap = new HashMap<>();
            maps.put("update", updateMap);
            result.setList(getInstance().getEvolveML( aConfig, disableList, updateMap, ml));
            result.setMaps(maps);
            result.setConfig(aConfig);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        //DbDao.instance("hibernate");
        //DbDao.instance("spark");
        MyExecutors.init(new MyMyConfig(MyXMLConfig.getConfigInstance()).getMLMPCpu());
        SpringApplication.run(ServiceController.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.GETMARKETS).allowedOrigins("http://localhost:19000");
                registry.addMapping("/" + EurekaConstants.GETMARKETS).allowedOrigins("http://localhost:3082");
            }
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer2() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.GETCONFIG).allowedOrigins("http://localhost:19000");
                registry.addMapping("/" + EurekaConstants.GETCONFIG).allowedOrigins("http://localhost:3082");
            }
        };
    }
    @Bean
    public WebMvcConfigurer corsConfigurer3() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.SETCONFIG).allowedOrigins("http://localhost:19000");
                registry.addMapping("/" + EurekaConstants.SETCONFIG).allowedOrigins("http://localhost:3082");
            }
        };
    }

}
