package roart.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.action.Action;
import roart.action.ActionThread;
import roart.action.LeaderRunner;
import roart.action.MainAction;
import roart.common.cache.MyCache;
import roart.common.constants.Constants;
import roart.common.util.MemUtil;
import roart.db.dao.IclijDbDao;
import roart.db.thread.DatabaseThread;
import roart.executor.MyExecutors;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.populate.PopulateThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@EnableJdbcRepositories("roart.common.springdata.repository")
@EnableDiscoveryClient
@SpringBootApplication
public class IclijController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;
    
    @Autowired
    private IclijDbDao dbDao;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public static List<String> taskList = new ArrayList<>();

    private LeaderRunner leaderRunnable = null;

    private Thread leaderWorker = null;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(IclijController.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException, JsonParseException, JsonMappingException, IOException {	    
        log.info("Using profile {}", activeProfile);
        log.info("Using profile {}", iclijConfig);
        IclijConfig instance = iclijConfig;
        ControlService.configCurator(iclijConfig);
        try {
            MyExecutors.initThreads("dev".equals(activeProfile));
            MyExecutors.init(new double[] { instance.mpServerCpu() } );
            String myservices = instance.getMyservices();
            String services = instance.getServices();
            String communications = instance.getCommunications();
            new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iclijConfig, dbDao).start();
            new PopulateThread(iclijConfig, dbDao).start();
            if (iclijConfig.wantDbHibernate()) {
                new DatabaseThread().start();
            }
            new ActionThread(iclijConfig, dbDao).start();
            new MemRunner().start();
            MyCache.setCache(instance.wantCache());
            MyCache.setCacheTTL(instance.getCacheTTL());
            startLeaderWorker();
            if (MainAction.wantsGoals(iclijConfig)) {        
                Action action = new MainAction(iclijConfig, dbDao);
                action.goal(null, null, null, iclijConfig);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Bean(name = "OBJECT_MAPPER_BEAN")
    public ObjectMapper jsonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(new JavaTimeModule())
                .build();
    }

    class MemRunner extends Thread {

        private static Logger log = LoggerFactory.getLogger(MemRunner.class);

        public static volatile int timeout = 3600;

        public void run() {
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));

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
    
    public void startLeaderWorker() {
        leaderRunnable = new LeaderRunner(iclijConfig, null);
        leaderWorker = new Thread(leaderRunnable);
        leaderWorker.setName("LeaderWorker");
        leaderWorker.start();
        log.info("starting leader worker");
    }

}
