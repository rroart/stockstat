package roart.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.db.thread.DatabaseThread;
import org.springframework.beans.factory.annotation.Autowired;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.ml.spark.MLClassifySparkAccess;
import roart.ml.common.MLClassifyAccess;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.common.config,roart.iclij.config")
@CrossOrigin
@RestController
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static List<String> taskList;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    IclijConfig iclijConfig;
    
    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETMODELS,
            method = RequestMethod.POST)
    public IclijServiceResult getmodels(@RequestBody IclijServiceParam param)
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

    @RequestMapping(value = "/" + EurekaConstants.GETNAME,
            method = RequestMethod.POST)
    public IclijServiceResult getname(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            MLClassifyAccess access = new MLClassifySparkAccess(iclijConfig);
            String name = access.getName();
            result.setConfigData(iclijConfig.getConfigData());
            System.out.println("configs " + result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETSHORTNAME,
            method = RequestMethod.POST)
    public IclijServiceResult getshortname(@RequestBody IclijServiceParam param)
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

    @RequestMapping(value = "/" + EurekaConstants.CLEAN,
            method = RequestMethod.POST)
    public IclijServiceResult clean(@RequestBody IclijServiceParam param)
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

    @RequestMapping(value = "/" + EurekaConstants.LEARNTESTCLASSIFY,
            method = RequestMethod.POST)
    public IclijServiceResult learntestclassify0(@RequestBody IclijServiceParam param)
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

    @RequestMapping(value = "/" + EurekaConstants.LEARNTESTCLASSIFY,
            method = RequestMethod.POST)
    public LearnTestClassifyResult learntestclassify(@RequestBody LearnTestClassify param)
            throws Exception {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        try {
            MLClassifyAccess access = new MLClassifySparkAccess(iclijConfig);
            //LearnTestClassifyResult result0 = access.learntestclassify(nnconfigs, null, learnTestMap, model, size, outcomes, classifyMap, shortMap, path, filename, param.getNeuralnetcommand(), mlmeta, param.getClassify());
            
            //result.setConfigData(iclijConfig.getConfigData());
            //System.out.println("configs " + result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            //result.setError(e.getMessage());
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
        String myservices = instance.getMyservices();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        new DatabaseThread().start();
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
