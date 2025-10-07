package roart.simtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.action.ActionThread;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.model.IncDecDTO;
import roart.common.model.MyDataSource;
import roart.common.model.SimDataDTO;
import roart.common.springdata.repository.AboveBelowRepository;
import roart.common.springdata.repository.ActionComponentRepository;
import roart.common.springdata.repository.ConfigRepository;
import roart.common.springdata.repository.ContRepository;
import roart.common.springdata.repository.IncDecRepository;
import roart.common.springdata.repository.MLMetricsRepository;
import roart.common.springdata.repository.MemoryRepository;
import roart.common.springdata.repository.MetaRepository;
import roart.common.springdata.repository.RelationRepository;
import roart.common.springdata.repository.SimDataRepository;
import roart.common.springdata.repository.SpringAboveBelowRepository;
import roart.common.springdata.repository.SpringActionComponentRepository;
import roart.common.springdata.repository.SpringConfigRepository;
import roart.common.springdata.repository.SpringContRepository;
import roart.common.springdata.repository.SpringIncDecRepository;
import roart.common.springdata.repository.SpringMLMetricsRepository;
import roart.common.springdata.repository.SpringMemoryRepository;
import roart.common.springdata.repository.SpringMetaRepository;
import roart.common.springdata.repository.SpringRelationRepository;
import roart.common.springdata.repository.SpringSimDataRepository;
import roart.common.springdata.repository.SpringSimRunDataRepository;
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.SpringTimingBLRepository;
import roart.common.springdata.repository.SpringTimingRepository;
import roart.common.springdata.repository.StockRepository;
import roart.common.springdata.repository.TimingBLRepository;
import roart.common.springdata.repository.TimingRepository;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.simtest.ConfigDb;
import roart.controller.IclijController;
import roart.controller.TestCommunicationFactory;
import roart.controller.TestCuratorFramework;
import roart.controller.TestDataSource;
import roart.controller.TestInmemory;
import roart.controller.TestInmemoryFactory;
import roart.controller.TestUtils;
import roart.controller.TestUtils2;
import roart.controller.TestWebFluxUtil;
import roart.db.dao.CoreDataSource;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpring;
import roart.db.spring.DbSpringDS;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebDataJson;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
import roart.testdata.TestConstants;
import roart.testdata.TestData;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

@TestInstance(Lifecycle.PER_CLASS)
//@ComponentScan(basePackages = "roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
//@EnableJdbcRepositories("roart.common.springdata.repository")
@SpringJUnitConfig //(SimConfig.class)
//@EnableAutoConfiguration 
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = SimIT.SimConfiguration.class)//{ IclijConfig.class, /*IclijDbDao.class,*/ DbDao.class, CoreDataSource.class, DbSpringDS.class, DbSpring.class, ConfigI.class, ConfigDb.class, SimConfig.class, SpringAboveBelowRepository.class, NamedParameterJdbcTemplate.class } )
@SpringBootTest(classes = IclijController.class ) //(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, DbSpring.class, ConfigDb.class, SimConfig.class } )
public class SimIT {
    private Logger log = LoggerFactory.getLogger(this.getClass());
/*
    @Configuration
    static class SimConfiguration {
        @Bean
        public IclijConfig iclijConfig() {
            // Return a mock or minimal config for context loading
            return mock(IclijConfig.class);
        }
        @Bean
        public IO io() {
            // Return a mock or minimal IO for context loading
            return mock(IO.class);
        }
        
    }
*/
    @Autowired
    private IclijConfig iconf = null;

    @Autowired
    private JdbcRepositoriesAutoConfiguration jc;
    
    //@Autowired
    private IO io;

    IclijDbDao iclijDbDao;
    
    // no autowiring
    IclijConfig conf = null;
   
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Autowired
    private MyDataSource dataSource;

    @Autowired
    private DbSpringDS dbSpringDS;
    
    DbDao dbDao;
    
    WebFluxUtil webFluxUtil;
    
    FileSystemDao fileSystemDao;
    
    Parameters parameters;
    
    ActionThread ac;
    
    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new TestCommunicationFactory();

    private TestUtils testutils;

    private TestUtils2 testutils2;
    
    private TestInmemory inmemory;
    
    @BeforeAll
    public void before() throws Exception {
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "config2", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());

        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

        dbDao = new DbDao(conf, dataSource);
        iclijDbDao = new IclijDbDao(iconf, dbSpringDS);
        
        webFluxUtil = new TestWebFluxUtil(conf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = new TestCuratorFramework();
        
        io = new IO(iclijDbDao, dbDao, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil)webFluxUtil).setIo(io);
        ((TestCommunicationFactory)communicationFactory).setIo(io);
        ((TestCommunicationFactory)communicationFactory).setConfig(iconf);
        
        ac = new ActionThread(iconf, io);
        
        testutils = new TestUtils(iconf, io);
        testutils2 = new TestUtils2(iconf, io);
        
        //String content = "";
        //new Sim(iconf, dbDao, fileSystemDao).method((String) content, "sim", true);
        
        inmemory = (TestInmemory) io.getInmemoryFactory().get(iconf.getInmemoryServer(), iconf.getInmemoryHazelcast(), iconf.getInmemoryRedis());

    }

    @Test
    public void testSim() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = testutils.getSimConfigDefault();
        String market = System.getenv("MARKET");
        log.info("Market {}", market);
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getSimulateInvestMarket(simConfig, market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
        WebDataJson webdata = result.getWebdatajson();
        Map<String, Object> updatemap = result.getWebdatajson().getUpdateMap();
        if (updatemap.containsKey("empty")) {
            return;
        }
        List<Double> capital = new ArrayList<>();
        for (Object node : (List<?>) updatemap.get("plotcapital")) {
            capital.add(((Double)node));
        }
        if (capital.isEmpty()) {
            return;
        }
        double[] capitalArr = capital.stream().mapToDouble(Double::doubleValue).toArray();
        double[] geom = geometricSpace(capitalArr[0], capitalArr[capitalArr.length - 1], capitalArr.length);
        double pearson = 0, spearman = 0, kendalltau = 0;
        if (capitalArr.length >= 2) {
            pearson = new PearsonsCorrelation().correlation(capitalArr, geom);
            spearman = new SpearmansCorrelation().correlation(capitalArr, geom);
            kendalltau = new KendallsCorrelation().correlation(capitalArr, geom);
        }
        FileOutputStream out = new FileOutputStream("file.txt");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Capital: %.2f\n", capitalArr[capitalArr.length - 1]) + "\n");
        sb.append("Last stocks: " + updatemap.get("laststocks") + "\n");
        sb.append(String.format("Pearson: %.2f, Spearman: %.2f, KendallTau: %.2f\n", pearson, spearman, kendalltau) + "\n");
        // Plotting can be done using JFreeChart or similar if needed
        // Print other info as needed
        for (Object x : (List<?>) updatemap.get("stockhistory")) {
            sb.append(x + "\n");
        }
        for (Object x : (List<?>) updatemap.get("sumhistory")) {
            sb.append(x + "\n");
        }
        List<?> tradestocks = (List<?>) updatemap.get("tradestocks");
        for (int i = 0; i < Math.min(10, tradestocks.size()); i++) {
            sb.append(tradestocks.get(i) + "\n");
        }
        // sb.append(webdata.keySet());
        //sb.append(webdata.get("timingMap"));
        sb.append(updatemap.get("startdate") + "\n");
        sb.append(updatemap.get("enddate") + "\n");
        boolean intervalwhole = true;
        if (intervalwhole) {
            sb.append(updatemap.get("scores") + "\n");
            sb.append(updatemap.get("stats") + "\n");
            sb.append(updatemap.get("minmax") + "\n");
        }
        sb.append(updatemap.get("lastbuysell") + "\n");
        String str = sb.toString();
        log.info("File {}", str);
        Files.write(Path.of("/tmp/" + UUID.randomUUID() + ".txt"), str.getBytes());
    }
    
    void m(Map<String, Object> updatemap) {

    }

    private static double[] geometricSpace(double start, double end, int num) {
        double[] arr = new double[num];
        double ratio = Math.pow(end / start, 1.0 / (num - 1));
        arr[0] = start;
        for (int i = 1; i < num; i++) {
            arr[i] = arr[i - 1] * ratio;
        }
        return arr;
    }

}