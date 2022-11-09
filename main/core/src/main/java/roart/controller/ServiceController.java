package roart.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import roart.common.config.ConfigConstants;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.db.dao.DbDao;
import roart.db.thread.DatabaseThread;
import roart.eureka.util.EurekaUtil;
import roart.executor.MyExecutors;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.service.ControlService;
import roart.service.evolution.EvolutionService;
import roart.common.cache.MyCache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.openjdk.jol.vm.VM;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

@CrossOrigin
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ControlService instance;

    public static List<String> taskList;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService();
        }
        return instance;
    }

    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
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
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETMETAS,
            method = RequestMethod.POST)
    public ServiceResult getMetas(@RequestBody ServiceParam param)
            throws Exception {
        ServiceResult result = new ServiceResult();
        try {
            result.setMetas(getInstance().getMetas());
            log.info("Metasize {}", result.getMetas().size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
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
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            NeuralNetCommand neuralnetcommand = param.getNeuralnetcommand();
            result.setList(getInstance().getContent( new MyMyConfig(param.getConfig()), maps, disableList, neuralnetcommand));
            result.setMaps(maps);
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
            log.info("Length {}", JsonUtil.convert(maps).length());
            }
            //System.out.println(VM.current().details());
            //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
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
            log.error(Constants.EXCEPTION, e);
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
            Set<Pair<String,String>> ids = new HashSet<>();
            for (String union : param.getIds()) {
                String[] idsplit = union.split(",");
                Pair<String, String> pair = new ImmutablePair(idsplit[0], idsplit[1]);
                ids.add(pair);
            }
            result.setList(getInstance().getContentGraph( new MyMyConfig(param.getConfig()), ids, param.getGuiSize()));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
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
            Map<String, Object> scoreMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            maps.put("update", updateMap);
            maps.put("score", scoreMap);
            maps.put("result", resultMap);
            result.setList(new EvolutionService().getEvolveRecommender( aConfig, disableList, updateMap, scoreMap, resultMap));
            result.setMaps(maps);
            result.setConfig(aConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
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
            Map<String, Object> scoreMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            maps.put("update", updateMap);
            maps.put("score", scoreMap);
            maps.put("result", resultMap);
            NeuralNetCommand neuralnetcommand = param.getNeuralnetcommand();
            result.setList(new EvolutionService().getEvolveML( aConfig, disableList, updateMap, ml, neuralnetcommand, scoreMap, resultMap));
            result.setMaps(maps);
            result.setConfig(aConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        //DbDao.instance("hibernate");
        //DbDao.instance("spark");
        SpringApplication.run(ServiceController.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException, JsonParseException, JsonMappingException, IOException {        
        System.out.println("Using profile " + activeProfile);
        MyExecutors.initThreads("dev".equals(activeProfile));
        MyExecutors.init(new double[] { 0, new MyMyConfig(MyXMLConfig.getConfigInstance()).getMLMPCpu() } );
        MyMyConfig instance = new MyMyConfig(MyXMLConfig.getConfigInstance());
        String myservices = instance.getMyservices();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        new ServiceControllerOther(myservices, services, communications, ServiceParam.class).start();
        new DatabaseThread().start();
        new MemRunner().run();
        MyCache.setCache(instance.wantCache());
        MyCache.setCacheTTL(instance.getCacheTTL());
    }
    
    @RequestMapping(value = "cache/invalidate",
            method = RequestMethod.POST)
    public void cacheinvalidate()
            throws Exception {
        MyCache.getInstance().invalidate();
    }
    class MemRunner implements Runnable {

        private static Logger log = LoggerFactory.getLogger(MemRunner.class);

        public static volatile int timeout = 3600;

        public void run() {
            long[] mem = MemUtil.mem();
             log.info("MEM {}", MemUtil.print(mem));

             while (true) {
                try {
                    TimeUnit.SECONDS.sleep(600);
                } catch (/*Interrupted*/Exception e) {
                    // TODO Auto-generated catch block
                    log.error(Constants.EXCEPTION, e);
                }
            }
        }
    }
    
    @RequestMapping(value = "/" + EurekaConstants.GETTASKS,
            method = RequestMethod.POST)
    public List<String> getTasks()
            throws Exception {
        try {
            return ServiceController.taskList;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

}
