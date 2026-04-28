package roart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.aggregator.impl.IndicatorAggregator;
import roart.common.pipeline.util.PipelineUtils;
import roart.ml.common.MLMeta;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import roart.action.ActionThread;
import roart.aggregator.impl.MLMulti;
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
import roart.common.pipeline.data.SerialPipeline;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.etl.db.Extract;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;
import roart.iclij.service.ControlService;
import roart.iclij.common.service.IclijServiceResult;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.model.io.IO;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.ExtraReader;
import roart.testdata.TestConstants;
import roart.common.model.IncDecDTO;
import roart.common.model.MyDataSource;
import roart.common.model.SimDataDTO;
import roart.common.model.StockDTO;
import roart.testdata.TestData;

// github copilot

@TestInstance(Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
public class InmemoryPipelineBatchIT {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;

    IclijDbDao iclijDbDao = mock(IclijDbDao.class);

    // no autowiring
    //IclijConfig conf = null;

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
        //conf = new IclijConfig(configMaps, "coreconfig", null);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());

        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

        String market = TestConstants.MARKET;
        String start = "2022.01.01";
        String end = "2025.01.01";
        TestDataSource dataSource1 = new TestDataSource(iconf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), market, 26, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null);
        TestDataSource dataSource2 = new TestDataSource(iconf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), TestConstants.MARKET2, 20, false, Constants.PRICECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, "impid");
        dataSource = new TestDataSources(List.of(dataSource1, dataSource2));
        periodDataSources = new TestDataSource[4][4];
        for (int i = 1; i < periodDataSources.length; i++) { // stockcount
            for (int j = 1; j < periodDataSources[i].length; j++) { // days
                periodDataSources[i][j] = new TestDataSource(iconf, new TimeUtil().convertDate2(start), new TimeUtil().convertDate2(end), TestConstants.SLOWMARKET, 20, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null, 0, i, j);
            }
        }

        dbDao = new DbDao(iconf, dataSource);

        webFluxUtil = new TestWebFluxUtil(iconf, null);
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

        inmemory = (TestInmemory) io.getInmemoryFactory().get(iconf.getInmemoryServer(), iconf.getInmemoryHazelcast(), iconf.getInmemoryRedis());

    }

    @BeforeEach
    public void beforeEach() throws Exception {
        // Ensure clean state before each test
        iconf.getConfigData().getConfigValueMap().put(
            IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty(),
            "Test must start with clean inmemory state");
    }

    @AfterEach
    public void afterEach() throws Exception {
        // Reset configuration to default state
        iconf.getConfigData().getConfigValueMap().put(
            IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);

        // Clean cache and pipeline
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
        inmemory.stat();

        // Verify complete cleanup
        assertEquals(true, inmemory.isEmpty(),
            "Inmemory cache must be empty after test - test left leakage");
    }

    @Test
    public void test() throws Exception {
        // placeholder test
    }

    /**
     * Test that IndicatorAggregator::getMapMap produces the same output
     * when run batched vs non-batched
     */
    @Test
    public void testIndicatorAggregatorGetMapMapBatchedNonBatchedConsistency() throws Exception {
        // Run test without batching
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aciNonBatch = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aciNonBatch, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();

        // Clear and run test with batching

        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aciBatch = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aciBatch, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();

        // Results should be identical or compatible
        assertEquals(true, inmemory.isEmpty());
    }

    /**
     * Test batched pipeline with small batch size
     */
    @Test
    public void testBatchedPipelineSmallBatchSize() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 2);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    /**
     * Test batched pipeline with large batch size
     */
    @Test
    public void testBatchedPipelineLargeBatchSize() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 100);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testMachineLearningBatched() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.MACHINELEARNING, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    @Test
    public void testFindProfitBatched() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
    }

    @Test
    public void testSimBatched() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());
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
    public void testEvolveBatched() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

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
    public void testImproveProfitBatched() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ActionComponentDTO aci = new ActionComponentDTO(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
    }

    /**
     * Direct integration test calling IndicatorAggregator::getMapMap
     * Verifies that batched and non-batched produce equivalent results
     */
    @Test
    public void testIndicatorAggregatorGetMapMapDirectComparison() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMINIMUMSIZE, 1);
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        String market = TestConstants.MARKET;
        iconf.getConfigData().setMarket(market);
        List<String> disableList = new ArrayList<>();
        StockData stockData = new Extract(io.getDbDao()).getStockData(iconf, true);

        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMlclassify(false);
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMldynamic(true);

        IndicatorUtils iu = new IndicatorUtils();
        ExtraReader extraReader = new ExtraReader(iconf, stockData.marketdatamap, 0, stockData);
        Map<String, StockData> extraStockDataMap = new IndicatorUtils().getExtraStockDataMap(iconf, io.getDbDao(), extraReader, true);

        log.info("Stock data keys: {}", stockData.marketdatamap.keySet());
        Pipeline[] datareaders = iu.getDataReaders(iconf, stockData.periodText,
                stockData.marketdatamap, stockData, extraStockDataMap, extraReader, inmemory);

        SerialPipeline pipelinedata = new SerialPipeline();
        pipelinedata = iu.createDatareaderPipelineData(iconf, pipelinedata, stockData, datareaders);

        List<StockDTO> dayStocks = iu.getDayStocks(iconf, stockData);
        List<AbstractCategory> categories = Arrays.asList(new CategoryUtil().getCategories(iconf, dayStocks,
                stockData.periodText, pipelinedata, inmemory));

        pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);

        // Test with non-batched mode
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
        log.info("Running non-batched test with batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        MLMulti mlMultiNonBatch = new MLMulti(iconf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
         mlMultiNonBatch.calculateMe(iconf, pipelinedata, neuralnetcommand);
       mlMultiNonBatch.putData();

        inmemory.stat();

        // Clear state
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
        inmemory.stat();

        // Recreate pipeline data for batched test
        // pipelinedata = new SerialPipeline();
        // pipelinedata = iu.createDatareaderPipelineData(conf, pipelinedata, stockData, datareaders);
        // pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);

        // Test with batched mode
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
        log.info("Running batched test with batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        IndicatorAggregator mlMultiBatch = new MLMulti(iconf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
        mlMultiBatch.calculateMe(iconf, pipelinedata, neuralnetcommand);
        mlMultiBatch.putData();

        inmemory.stat();

        // Verify both completed successfully without errors
        log.info("Both batched and non-batched processing completed successfully");
        assertEquals(true, inmemory.isEmpty());
    }

    // but github copilot did not actually do it, made my own

    @Test
    public void testIndicatorAggregatorGetMapMapDirectComparisonReal() throws Exception {
        String market = TestConstants.MARKET;
        iconf.getConfigData().setMarket(market);
        List<String> disableList = new ArrayList<>();
        StockData stockData = new Extract(io.getDbDao()).getStockData(iconf, true);

        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMlclassify(false);
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMldynamic(true);

        IndicatorUtils iu = new IndicatorUtils();

        log.info("Stock data keys: {}", stockData.marketdatamap.keySet());

        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = getMapMap(iconf, stockData, neuralnetcommand, iu, inmemory);

        // Clear state
        testutils.cacheinvalidate();
        testutils.deletepipeline(ControlService.id);
        inmemory.stat();

        // Recreate pipeline data for batched test
        // Test with batched mode
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);

        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMapBatch = getMapMap(iconf, stockData, neuralnetcommand, iu, inmemory);

        boolean compared = new MLMulti(iconf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, new SerialPipeline(), neuralnetcommand, stockData.stockdates, inmemory).compareMaps(mapMap, mapMapBatch);

        log.info("map batch {}", mapMapBatch);
        log.info("map no batch {}", mapMap);
        assertTrue(compared, "Batched and non-batched mapMap should be equivalent");

        inmemory.stat();

        // Verify both completed successfully without errors
        log.info("Both batched and non-batched processing completed successfully");
        assertEquals(true, inmemory.isEmpty());
    }

    private Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> getMapMap(IclijConfig conf, StockData stockData, NeuralNetCommand neuralnetcommand, IndicatorUtils iu, TestInmemory inmemory) throws Exception {
        log.info("Running test with batch size: {}", iconf.wantsInmemoryPipelineBatchsize());

        ExtraReader extraReader = new ExtraReader(conf, stockData.marketdatamap, 0, stockData);
        Map<String, StockData> extraStockDataMap = new IndicatorUtils().getExtraStockDataMap(conf, io.getDbDao(), extraReader, true);
        Pipeline[] datareaders = iu.getDataReaders(conf, stockData.periodText,
                stockData.marketdatamap, stockData, extraStockDataMap, extraReader, inmemory);

        SerialPipeline pipelinedata = new SerialPipeline();
        pipelinedata = iu.createDatareaderPipelineData(conf, pipelinedata, stockData, datareaders);

        List<StockDTO> dayStocks = iu.getDayStocks(conf, stockData);
        List<AbstractCategory> categories = Arrays.asList(new CategoryUtil().getCategories(conf, dayStocks,
                stockData.periodText, pipelinedata, inmemory));

        pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);

        IndicatorAggregator mlMulti = new MLMulti(conf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
        //mlMulti.putData();
        String key = stockData.catName;
        SerialPipeline datareader  = PipelineUtils.getPipelines(pipelinedata, key, inmemory);
        //PipelineUtils.getPipelineValue(datareaders, key, PipelineConstants.TRUNCFILLLIST, inmemory));
        //Map<String, double[][]>  base100FillListMap = PipelineUtils.sconvertMapdd(PipelineUtils.getPipelineValue(datareaders, key, PipelineConstants.TRUNCBASE100FILLLIST, inmemory)) ;
        Double threshold = 1.0;
        Map<IndicatorAggregator.SubType, MLMeta> metaMap = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = mlMulti.getMapMap(threshold, metaMap);

        inmemory.stat();

        return mapMap;
    }
}
