package roart.evolvetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

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
import roart.common.model.MyDataSource;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.controller.IclijController;
import roart.controller.TestCommunicationFactory;
import roart.controller.TestCuratorFramework;
import roart.controller.TestInmemory;
import roart.controller.TestInmemoryFactory;
import roart.controller.TestWebFluxUtil;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringDS;
import roart.etl.db.Extract;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.Parameters;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.model.io.IO;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.ExtraReader;
import roart.testdata.TestConstants;

@TestInstance(Lifecycle.PER_CLASS)
//@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = IclijController.class ) //(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
public class EvolveIT {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;
    
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
    
    IO io;

    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new TestCommunicationFactory();

    private TestInmemory inmemory;
    
    @BeforeAll
    public void before() throws Exception {
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "config2", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.FALSE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        log.info("keys" + conf.getConfigData().getConfigValueMap().get(ConfigConstants.AGGREGATORSUSECURVE));
        log.info("keys" + conf.getConfigData().getConfigValueMap().get(ConfigConstants.AGGREGATORSUSECONFUSION));
        log.info("keys" + conf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNINGUSEBINARY));
        log.info("Use curve {}", conf.wantAggregatorsUsecurve());
        log.info("Use confusion {}", conf.wantAggregatorsUseConfusion());
        log.info("Use binary {}", conf.wantUseBinary());
        //log.info("Use binary {}", iconf.wantUseBinary());

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
        
        inmemory = (TestInmemory) io.getInmemoryFactory().get(iconf.getInmemoryServer(), iconf.getInmemoryHazelcast(), iconf.getInmemoryRedis());

    }

    @Test
    public void test() throws Exception {
        //new LeaderRunner(iconf, null, io).run();
    }
    
    @Test
    public void testMachineLearning() throws Exception {
        String market = System.getenv("MARKET");
        String component = System.getenv("COMPONENT");
        String subcomponent = System.getenv("SUBCOMPONENT");
        log.info("Market {}", market);
        ActionComponentDTO aci = new ActionComponentDTO(market, IclijConstants.MACHINELEARNING, component, subcomponent, 0, JsonUtil.convert(parameters));
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
    public void testDataset() throws Exception {
        String market = System.getenv("MARKET");
        String component = System.getenv("COMPONENT");
        String subcomponent = System.getenv("SUBCOMPONENT");
        log.info("Market {}", market);
        ActionComponentDTO aci = new ActionComponentDTO(market, IclijConstants.DATASET, component, subcomponent, 0, JsonUtil.convert(parameters));
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
        String market = System.getenv("MARKET");
        String component = System.getenv("COMPONENT");
        String subcomponent = System.getenv("SUBCOMPONENT");
        log.info("Market {}", market);
        ActionComponentDTO aci = new ActionComponentDTO(market, IclijConstants.EVOLVE, component, subcomponent, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        inmemory.stat();
        assertEquals(true, inmemory.isEmpty());
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
}
