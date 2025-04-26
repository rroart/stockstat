package roart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.ArrayList;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.action.ActionThread;
import roart.action.LeaderRunner;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.model.ActionComponentItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.db.dao.CoreDataSource;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
import roart.queue.PipelineThread;
import roart.testdata.TestConstants;
import roart.common.model.IncDecItem;
import roart.common.model.MyDataSource;
import roart.common.model.SimDataItem;
import roart.testdata.TestData;

@TestInstance(Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
public class InmemoryPipelineTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;
    
    IclijDbDao iclijDbDao = mock(IclijDbDao.class);
    
    // no autowiring
    IclijConfig conf = null;
   
    MyDataSource dataSource;
    
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    DbDao dbDao;
    
    WebFluxUtil webFluxUtil;
    
    FileSystemDao fileSystemDao;
    
    Parameters parameters;
    
    ActionThread ac;
    
    IO io;

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

        String market = TestConstants.MARKET;
        String start = "2024.01.01";
        String end = "2025.01.01";
        TestDataSource dataSource1 = new TestDataSource(conf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), market, 26, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null);
        TestDataSource dataSource2 = new TestDataSource(conf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), TestConstants.MARKET2, 20, false, Constants.PRICECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, "impid");
        dataSource = new TestDataSources(List.of(dataSource1, dataSource2));

        dbDao = new DbDao(iconf, dataSource);
        
        webFluxUtil = new TestWebFluxUtil(conf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        List<IncDecItem> incdecs = new TestData(iconf).incdec(dataSource.getAll(market, iconf, true));
        doReturn(incdecs).when(iclijDbDao).getAllIncDecs(any(), any(), any(), any());
        
        List<SimDataItem> sims = new TestData(iconf).getSimData(market, TimeUtil.convertDate2(start), TimeUtil.convertDate2(end), iconf, 100);
        doReturn(sims).when(iclijDbDao).getAllSimData(any(), any(), any());
        
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
    public void test() throws Exception {
        //new LeaderRunner(iconf, null, io).run();
    }
    
    @Test
    public void testMachineLearning() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.MACHINELEARNING, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testFindProfit() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testFindProfitARI() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testFindProfitPredictor() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.PREDICTOR, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testEvolve() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testEvolveARI() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testImproveProfit() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testImproveProfitMLI() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLINDICATOR, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testCrosstest() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.CROSSTEST, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testFilter() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEFILTER, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testAboveBelow() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testSim() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = testutils.getSimConfigDefault();
        String market = TestConstants.MARKET;
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
    }

    @Test
    public void testSim2() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = testutils.getSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-01-01");
        simConfig.setEnddate("2025-01-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getSimulateInvestMarket2(simConfig, market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testAutoSim() throws Exception {
        AutoSimulateInvestConfig simConfig = testutils.getAutoSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getAutoSimulateInvestMarket(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testImproveSim() throws Exception {
        SimulateInvestConfig simConfig = testutils.getImproveSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getImproveSimulateInvest(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //System.out.println("map" + result.getWebdatajson().getUpdateMap());
        //System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testImproveAutoSim() throws Exception {
        AutoSimulateInvestConfig simConfig = testutils.getImproveAutoSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-07-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getImproveAutoSimulateInvest(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //System.out.println("map" + result.getWebdatajson().getUpdateMap());
        //System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }
    
    /*
    @Test
    public void getVerify() throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        testutils2.getVerify(param);
    }

    @Test
    public void getFindProfitMarket() throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        testutils2.getFindProfitMarket(param);
    }
    
    @Test
    public void getImproveAboveBelowMarketTest() throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        testutils2.getImproveAboveBelowMarket(param);
    }
    
    @Test
    public void getImproveProfitMarket() throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        testutils2.getImproveProfitMarket(param);
    }
    */
}
