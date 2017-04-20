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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import roart.config.MyPropertyConfig;
import roart.db.DbDao;
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
			getInstance().config(param.config);
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
            result.config = MyPropertyConfig.instance();
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

    @RequestMapping(value = "/" + EurekaConstants.GETSTOCKS,
			method = RequestMethod.POST)
	public ServiceResult getStocks(@RequestBody ServiceParam param)
			throws Exception {
		ServiceResult result = new ServiceResult();
		try {
			result.stocks = getInstance().getStocks(param.market);
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
		try {
			result.list = getInstance().getContent(param.config);
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
			result.list = getInstance().getContentStat(param.config);
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
			result.list = getInstance().getContentGraph(param.config, param.guiSize);
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
			result.list = getInstance().getContentGraph(param.config, ids, param.guiSize);
		} catch (Exception e) {
			log.error(roart.util.Constants.EXCEPTION, e);
			result.error = e.getMessage();
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
	    DbDao.instance("hibernate");
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
