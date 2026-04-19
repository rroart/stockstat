package roart.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
//import tools.jackson.databind.JsonSerializer;
import tools.jackson.databind.ObjectMapper;
//import tools.jackson.databind.SerializerProvider;
import tools.jackson.databind.json.JsonMapper;
//import roart.common.cache.MyCache;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.util.MemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import roart.iclij.config.IclijConfig;
import roart.spark.MLClassifySparkDS;
import roart.ml.common.MLClassifyDS;
import roart.ml.model.LearnTestClassifyDS;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.ml.spark.MLClassifySparkLORModel;
import roart.ml.spark.MLClassifySparkLSVCModel;
import roart.ml.spark.MLClassifySparkMLPCModel;
import roart.ml.spark.MLClassifySparkOVRModel;

@ComponentScan(basePackages = "roart.controller,roart.model,roart.common.config,roart.iclij.config")
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

    @RequestMapping(value = "/" + EurekaConstants.CLEAN,
            method = RequestMethod.POST)
    public LearnTestClassifyResult clean(@RequestBody LearnTestClassifyDS param)
            throws Exception {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        try {
            MLClassifyDS access = new MLClassifySparkDS(iclijConfig);            
            access.clean();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            //result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.LEARNTESTCLASSIFY,
            method = RequestMethod.POST)
    public LearnTestClassifyResult learntestclassify(@RequestBody LearnTestClassifyDS param)
            throws Exception {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        try {
            //param.model.setConf(iclijConfig);
            MLClassifyModel model = getModel(param.modelid);
            MLClassifyDS access = new MLClassifySparkDS(iclijConfig);            
            result = access.learntestclassify(param.nnconfigs, null, param.learnTestMap, model, param.size, param.outcomes, param.classifyMap, param.shortMap, param.path, param.filename, param.neuralnetcommand, param.mlmeta, param.classify);
            
            //result.setConfigData(iclijConfig.getConfigData());
            //System.out.println("configs " + result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            //result.setError(e.getMessage());
        }
        return result;
    }

    private MLClassifyModel getModel(int modelid) {
        switch (modelid) {
        case MLConstants.LOGISTICREGRESSION:
            return new MLClassifySparkLORModel(iclijConfig);
        case MLConstants.LINEARSUPPORTVECTORCLASSIFIER:
            return new MLClassifySparkLSVCModel(iclijConfig);
        case MLConstants.MULTILAYERPERCEPTRONCLASSIFIER:
            return new MLClassifySparkMLPCModel(iclijConfig);
        case MLConstants.ONEVSREST:
            return new MLClassifySparkOVRModel(iclijConfig);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        //DbDao.instance("hibernate");
        //DbDao.instance("spark");
        applicationContext = SpringApplication.run(ServiceController.class, args);
    }

    private static ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws InterruptedException, StreamReadException, DatabindException, IOException {        
        log.info("Using profile {}", activeProfile);
        IclijConfig instance = iclijConfig;
        String myservices = instance.getMyservices();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        // TODO not yet collision with other services
        // MyCache.setCache(instance.wantCache());
        // MyCache.setCacheTTL(instance.getCacheTTL());
        //new MemRunner().run();
    }

    public void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            log.debug(beanName);
        }
    }

    @RequestMapping(value = "cache/invalidate",
            method = RequestMethod.POST)
    public void cacheinvalidate()
            throws Exception {
        //MyCache.getInstance().invalidate();
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
    
    @Bean(name = "OBJECT_MAPPER_BEAN")
    public ObjectMapper jsonObjectMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    
    /*
    private static final class TripleSerializer extends JsonSerializer<Triple> {

        @Override
        public void serialize(Triple value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("left", value.getLeft());
            gen.writeObjectField("middle", value.getMiddle());
            gen.writeObjectField("right", value.getRight());
            gen.writeEndObject();
        }

    }
    */
}
