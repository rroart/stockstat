package roart.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

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
import roart.queue.PipelineThread;
import roart.queue.QueueThread;
import roart.model.io.IO;

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
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public static List<String> taskList = new ArrayList<>();

    private LeaderRunner leaderRunnable = null;

    private Thread leaderWorker = null;

    private ControlService instanceC;

    @Autowired
    private IO io;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(IclijController.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException, StreamReadException, DatabindException, IOException {	    
        if (System.getenv("test") != null) return;
        log.info("Using profile {}", activeProfile);
        log.info("Using profile {}", iclijConfig);
        IclijConfig instance = iclijConfig;
        try {
            MyExecutors.initThreads("dev".equals(activeProfile));
            MyExecutors.init(new double[] { instance.mpServerCpu() } );
            String myservices = instance.getMyservices();
            String services = instance.getServices();
            String communications = instance.getCommunications();
            new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iclijConfig, io).start();
            new PopulateThread(iclijConfig, io).start();
            if (iclijConfig.wantDbHibernate()) {
                new DatabaseThread().start();
            }
            new ActionThread(iclijConfig, io).start();
            new MemRunner().start();
            MyCache.setCache(instance.wantCache());
            MyCache.setCacheTTL(instance.getCacheTTL());
            startLeaderWorker();
            
            getInstance();
            // duplicated in pipelinethread
            String path = "/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + Constants.LIVE + "/" + instanceC.id;
            try {
                io.getCuratorClient().create().creatingParentsIfNeeded().forPath(path, new byte[0]);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            new QueueThread(iclijConfig, instanceC, io).start();
            new PipelineThread(iclijConfig, instanceC, io).start();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Bean(name = "OBJECT_MAPPER_BEAN")
    public ObjectMapper jsonObjectMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
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
        leaderRunnable = new LeaderRunner(iclijConfig, null, io);
        leaderWorker = new Thread(leaderRunnable);
        leaderWorker.setName("LeaderWorker");
        leaderWorker.start();
        log.info("starting leader worker");
    }

    private ControlService getInstance() {
        if (instanceC == null) {
            instanceC = new ControlService(iclijConfig, io);
        }
        return instanceC;
    }

}
