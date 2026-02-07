package roart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import roart.action.ActionThread;
import roart.action.LeaderRunner;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.util.AggregatorUtils;
import roart.category.AbstractCategory;
import roart.category.util.CategoryUtil;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.ActionComponentDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.constants.SimConstants;
import roart.db.dao.CoreDataSource;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.etl.db.Extract;
import roart.filesystem.FileSystemDao;
import roart.iclij.component.SimulateInvestComponent;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.category.AbstractCategory;
import roart.model.io.IO;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.impl.ExtraReader;
import roart.queue.PipelineThread;
import roart.testdata.TestConstants;
import roart.common.model.IncDecDTO;
import roart.common.model.MyDataSource;
import roart.common.model.SimDataDTO;
import roart.common.model.StockDTO;
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
    
    private static final ObjectMapper mapper = JsonMapper.builder().build();

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
    
    TestDataSource[][] periodDataSources;
    
    @BeforeAll
    public void before() throws Exception {
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "coreconfig", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());

        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

        String market = TestConstants.MARKET;
        String start = "2022.01.01";
        String end = "2025.01.01";
        TestDataSource dataSource1 = new TestDataSource(conf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), market, 26, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null);
        TestDataSource dataSource2 = new TestDataSource(conf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), TestConstants.MARKET2, 20, false, Constants.PRICECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, "impid");
        dataSource = new TestDataSources(List.of(dataSource1, dataSource2));
        periodDataSources = new TestDataSource[4][4];
        for (int i = 1; i < periodDataSources.length; i++) { // stockcount
            for (int j = 1; j < periodDataSources[i].length; j++) { // days
                periodDataSources[i][j] = new TestDataSource(conf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), TestConstants.SLOWMARKET, 20, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null, 0, i, j);
            }
        }
        
        dbDao = new DbDao(iconf, dataSource);
        
        webFluxUtil = new TestWebFluxUtil(conf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        List<IncDecDTO> incdecs = new TestData(iconf).incdec(dataSource.getAll(market, iconf, true));
        doReturn(incdecs).when(iclijDbDao).getAllIncDecs(any(), any(), any(), any());
        
        List<SimDataDTO> sims = new TestData(iconf).getSimData(market, TimeUtil.convertDate2(start), TimeUtil.convertDate2(end), iconf, 100);
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
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.MACHINELEARNING, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
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
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
    }

    @Test
    public void testFindProfitARI() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
    }

    @Test
    public void testFindProfitPredictor() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.PREDICTOR, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
    }

    @Test
    public void testEvolve() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
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
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
    }

    @Test
    public void testImproveProfit() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
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
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLINDICATOR, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    // TODO already
    @Test
    public void testCrosstest() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.CROSSTEST, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testFilter() throws Exception {
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.IMPROVEFILTER, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
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
        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
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
    public void testSimEvent() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = testutils.getSimConfigDefault();
        String market = TestConstants.SLOWMARKET;
        simConfig.setStocks(2);
        simConfig.setInterval(1);
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        
        DbDao origDbDao = io.getDbDao();
        DbDao dbDao = new DbDao(iconf, periodDataSources[2][1]); // stock 2 day 1
        io.setDbDao(dbDao);        

        IclijServiceResult result = null;
        try {
            simConfig.setBuyweight(false);
            result = testutils.getSimulateInvestMarket(simConfig, market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        try {
            simConfig.setBuyweight(true);
            result = testutils.getSimulateInvestMarket(simConfig, market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
        io.setDbDao(origDbDao);
    }

    @Test
    public void testSimWithDbid() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        String market = TestConstants.MARKET;
        SimDataDTO simData = iclijDbDao.getAllSimData(market, null, null).get(0);
        String dbid = "" + simData.getDbid();
        doReturn(simData).when(iclijDbDao).getSimData(dbid);
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getSimulateInvestMarketDbid(simConfig, market, dbid);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testSimWithDbidAndMod() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        String market = TestConstants.MARKET;
        SimDataDTO simData = iclijDbDao.getAllSimData(market, null, null).get(0);
        String dbid = "" + simData.getDbid();
        doReturn(simData).when(iclijDbDao).getSimData(dbid);
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        simConfig.setStocks(3);
        IclijServiceResult result = null;
        try {
            result = testutils.getSimulateInvestMarketDbid(simConfig, market, dbid);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
        if (!((List) result.getWebdatajson().getUpdateMap().get(SimConstants.LASTSTOCKS)).isEmpty()) {
            assertEquals(simConfig.getStocks(), ((List) result.getWebdatajson().getUpdateMap().get(SimConstants.LASTSTOCKS)).size());
         }
    }

    @Test
    public void testSimWithDbidAndModMethod() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        String market = TestConstants.MARKET;
        SimDataDTO simData = iclijDbDao.getAllSimData(market, null, null).get(0);
        String dbid = "" + simData.getDbid();
        doReturn(simData).when(iclijDbDao).getSimData(dbid);
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        simConfig.setStocks(13);
        SimulateInvestConfig newSimConfig = null;
        try {
            newSimConfig = new SimulateInvestComponent().getSimConfigByDbidAndMerge(io, iconf, simConfig, dbid);
            log.info("newSimConfig {}", newSimConfig.asValuedMap());
       } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        assertEquals(13, newSimConfig.getStocks());
    }

    @Test
    public void testSimRun() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        SimulateInvestConfig simConfig = testutils.getSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2023-01-01");
        simConfig.setEnddate("2024-01-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getSimulateInvestRunMarket(simConfig, market);
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
        assertEquals(false, inmemory.isEmpty());
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testImproveSimTwice() throws Exception {
        SimulateInvestConfig simConfig = testutils.getImproveSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = testutils.getImproveSimulateInvest(market, simConfig);
            log.info("First done");
            simConfig = testutils.getImproveSimConfigDefault();
            simConfig.setStartdate("2024-11-01");
            simConfig.setEnddate("2024-12-01");
            result = testutils.getImproveSimulateInvest(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //System.out.println("map" + result.getWebdatajson().getUpdateMap());
        //System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
        assertEquals(false, inmemory.isEmpty());
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
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
        assertEquals(false, inmemory.isEmpty());
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
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

    @Test
    public void testSome() throws Exception {
        conf.getConfigData().setMarket(TestConstants.MARKET);
        List<String> disableList = new ArrayList<>();
        StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true);
        
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMlclassify(false);
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMldynamic(true);
        
        IndicatorUtils iu = new IndicatorUtils();
        ExtraReader extraReader = new ExtraReader(conf, stockData.marketdatamap, 0, stockData);
        Map<String, StockData> extraStockDataMap = new IndicatorUtils().getExtraStockDataMap(conf, io.getDbDao(), extraReader, true);

        log.info("grr" + stockData.marketdatamap.keySet() + " " + stockData.periodText + " " + extraStockDataMap.keySet());
        Pipeline[] datareaders = iu.getDataReaders(conf, stockData.periodText,
                stockData.marketdatamap, stockData, extraStockDataMap, extraReader);

        /*
        pipelinedata = iu.createPipeline(conf, disableList, pipelinedata, categories, aggregates, stockData,
                datareaders);
        */
        
        // pipelinedata from datareaders and new meta

        PipelineData[] pipelinedata = new PipelineData[0];
        pipelinedata = iu.createDatareaderPipelineData(conf, pipelinedata, stockData, datareaders);

        // for categories and adding to pipelinedata

        List<StockDTO> dayStocks = iu.getDayStocks(conf, stockData);
        
        List<AbstractCategory> categories = Arrays.asList(new CategoryUtil().getCategories(conf, dayStocks,
                stockData.periodText, pipelinedata, inmemory));
        
        // add all indicators for the category

        pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);

        // for aggregates and adding to the pipeline

        /*
        Aggregator[] aggregates = Arrays.asList(getAggregates(conf, stockData.periodText,
                stockData.marketdatamap, categories.toArray(new AbstractCategory[0]), pipelinedata , disableList, stockData.catName, stockData.cat, stockData.stockdates, inmemory));
*/
        //new MLIndicator(conf, stockData.catName, stockData.catName, stockData.cat, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
        new MLMulti(conf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
        /*
        Aggregator[] aggregates = new AggregatorUtils().getAggregates(conf, pipelinedata,
                disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand, stockData.stockdates, inmemory);
        PipelineData data = aggregates[8].putData();
        */
        
        //log.info("wants " + conf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        //log.info("wants " + iconf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        //log.info("wants " + conf.getConfigData().getConfigValueMap());

    }

    @Test
    public void testWantUseCurve() throws Exception {
        boolean want = conf.wantAggregatorsUsecurve();
        System.out.println("want " + want);
    }
}
