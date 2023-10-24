package roart.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

//import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import roart.common.cache.MyCache;
import roart.common.config.ConfigConstantMaps;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.PipelineData;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.db.thread.DatabaseThread;
import roart.executor.MyExecutors;
import roart.service.ControlService;
import roart.service.evolution.EvolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import roart.db.dao.DbDao;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.common.config,roart.iclij.config")
@EnableJdbcRepositories("roart.common.springdata.repository")
@CrossOrigin
@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ControlService instance;

    public static List<String> taskList;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    IclijConfig iclijConfig;
    
    @Autowired
    DbDao dao;

    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService(dao);
        }
        return instance;
    }

    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/" + EurekaConstants.SETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult configDb(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            System.out.println("new market" + param.getConfigData().getMarket());
            System.out.println("new market" + param.getConfigData());
            System.out.println("new some " + param.getConfigData().getConfigValueMap().get(ConfigConstants.DATABASESPARKSPARKMASTER));
            //getInstance().config(param.config);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getConfig(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setConfigData(iclijConfig.getConfigData());
            System.out.println("configs " + result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETMARKETS,
            method = RequestMethod.POST)
    public IclijServiceResult getMarkets(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
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
    public IclijServiceResult getMetas(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
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
    public IclijServiceResult getDates(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            getInstance().getDates( new IclijConfig(param.getConfigData()), maps);
            result.setMaps(maps);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETSTOCKS,
            method = RequestMethod.POST)
    public IclijServiceResult getStocks(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setStocks(getInstance().getStocks(param.getMarket(),  new IclijConfig(param.getConfigData())));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public IclijServiceResult getContent(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
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
            PipelineData[] pipelinedata = new PipelineData[0];
            result.setList(getInstance().getContent( new IclijConfig(param.getConfigData()), maps, disableList, neuralnetcommand,  pipelinedata));
            result.setMaps(maps);
            result.setPipelineData(pipelinedata);
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
                //log.info("Length {}", JsonUtil.convert(maps).length());
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
    public IclijServiceResult getContentStat(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setList(getInstance().getContentStat( new IclijConfig(param.getConfigData())));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTGRAPH,
            method = RequestMethod.POST)
    public IclijServiceResult getContentGraph(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setList(getInstance().getContentGraph( new IclijConfig(param.getConfigData()), param.getGuiSize()));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTGRAPH2,
            method = RequestMethod.POST)
    public IclijServiceResult getContentGraph2(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            Set<Pair<String,String>> ids = new HashSet<>();
            for (String union : param.getIds()) {
                String[] idsplit = union.split(",");
                Pair<String, String> pair = new ImmutablePair(idsplit[0], idsplit[1]);
                ids.add(pair);
            }
            result.setList(getInstance().getContentGraph( new IclijConfig(param.getConfigData()), ids, param.getGuiSize()));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETEVOLVERECOMMENDER,
            method = RequestMethod.POST)
    public IclijServiceResult getEvolveRecommender(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            IclijConfig aConfig = new IclijConfig(param.getConfigData());
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
            result.setList(new EvolutionService(dao).getEvolveRecommender( aConfig, disableList, updateMap, scoreMap, resultMap));
            result.setMaps(maps);
            result.setConfigData(aConfig.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETEVOLVENN,
            method = RequestMethod.POST)
    public IclijServiceResult getTestML(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            IclijConfig aConfig = new IclijConfig(param.getConfigData());
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
            result.setList(new EvolutionService(dao).getEvolveML( aConfig, disableList, updateMap, ml, neuralnetcommand, scoreMap, resultMap));
            result.setMaps(maps);
            result.setConfigData(aConfig.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        //DbDao.instance("hibernate");
        //DbDao.instance("spark");
        applicationContext = SpringApplication.run(ServiceController.class, args);
        displayAllBeans();
    }

    private static ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws InterruptedException, JsonParseException, JsonMappingException, IOException {        
        log.info("Using profile {}", activeProfile);
        IclijConfig instance = iclijConfig;
        MyExecutors.initThreads("dev".equals(activeProfile));
        MyExecutors.init(new double[] { 0, iclijConfig.getMLMPCpu() } );
        String myservices = instance.getMyservices();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iclijConfig.copy(), dao).start();
        if (iclijConfig.wantDbHibernate()) {
            new DatabaseThread().start();
        }
        MyCache.setCache(instance.wantCache());
        MyCache.setCacheTTL(instance.getCacheTTL());
        //new MemRunner().run();
    }

    public static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println(beanName);
        }
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
    
    @RequestMapping(value = "/" + EurekaConstants.GETTASKS + "d",
            method = RequestMethod.POST)
    public List<String> dbMigrate()
            throws Exception {
        try {
            DbDao daoout = new DbDao(iclijConfig, null);
            return null;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }
    
    //@Bean
    public ObjectMapper getJacksonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.findAndRegisterModules();
        //objectMapper.configure(
        //        com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return objectMapper;
    }
}
