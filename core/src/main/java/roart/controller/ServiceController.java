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
            System.out.println("new market" + param.config.getMarket());
            System.out.println("new market" + param.config);
            System.out.println("new some " + param.config.configValueMap.get(ConfigConstants.DATABASESPARKSPARKMASTER));
			//getInstance().config(param.config);
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public ServiceResult getConfig(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.config = MyXMLConfig.getConfigInstance();
            System.out.println("configs " + result.config);
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.error = e.getMessage();
        }
        return result;
    }

	@RequestMapping(value = "/" + EurekaConstants.GETMARKETS,
			method = RequestMethod.POST)
	public ServiceResult getMarkets(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		try {
			result.markets = getInstance().getMarkets();
			System.out.println("markets "+ result.markets.size());
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

    @RequestMapping(value = "/" + EurekaConstants.GETDATES,
            method = RequestMethod.POST)
    public ServiceResult getDates(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.wantMaps) {
            maps = new HashMap<>();
        }
        try {
            getInstance().getDates( new MyMyConfig(param.config), maps);
            result.maps = maps;
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.error = e.getMessage();
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETSTOCKS,
			method = RequestMethod.POST)
	public ServiceResult getStocks(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		try {
			result.stocks = getInstance().getStocks(param.market,  new MyMyConfig(param.config));
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

	@RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
			method = RequestMethod.POST)
	public ServiceResult getContent(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		Map<String, Map<String, Object>> maps = null;
		if (param.wantMaps) {
		    maps = new HashMap<>();
		}
		try {
			result.list = getInstance().getContent( new MyMyConfig(param.config), maps);
			result.maps = maps;
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

	@RequestMapping(value = "/" + EurekaConstants.GETCONTENTSTAT,
			method = RequestMethod.POST)
	public ServiceResult getContentStat(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		try {
			result.list = getInstance().getContentStat( new MyMyConfig(param.config));
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

	@RequestMapping(value = "/" + EurekaConstants.GETCONTENTGRAPH,
			method = RequestMethod.POST)
	public ServiceResult getContentGraph(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		try {
			result.list = getInstance().getContentGraph( new MyMyConfig(param.config), param.guiSize);
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
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
			for (String union : param.ids) {
				String[] idsplit = union.split(",");
				Pair<String, String> pair = new Pair(idsplit[0], idsplit[1]);
				ids.add(pair);
			}
			result.list = getInstance().getContentGraph( new MyMyConfig(param.config), ids, param.guiSize);
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

    @RequestMapping(value = "/" + EurekaConstants.GETTESTRECOMMENDER,
            method = RequestMethod.POST)
    public ServiceResult getTestRecommender(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            MyMyConfig aConfig = new MyMyConfig(param.config);
            result.list = getInstance().getTestRecommender( aConfig);
            result.config = aConfig;
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
            result.error = e.getMessage();
        }
        return result;
    }

	public static void main(String[] args) throws Exception {
	    //DbDao.instance("hibernate");
        //DbDao.instance("spark");
		SpringApplication.run(ServiceController.class, args);
	}

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.GETMARKETS).allowedOrigins("http://localhost:19000");
            }
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer2() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.GETCONFIG).allowedOrigins("http://localhost:19000");
            }
        };
    }
    @Bean
    public WebMvcConfigurer corsConfigurer3() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/" + EurekaConstants.SETCONFIG).allowedOrigins("http://localhost:19000");
            }
        };
    }

 }
