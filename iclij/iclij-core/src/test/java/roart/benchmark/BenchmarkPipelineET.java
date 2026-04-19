package roart.benchmark;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import roart.action.ActionThread;
import roart.aggregator.impl.IndicatorAggregator;
import roart.aggregator.impl.MLMulti;
import roart.category.AbstractCategory;
import roart.category.util.CategoryUtil;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MyDataSource;
import roart.common.model.StockDTO;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.webflux.WebFluxUtil;
import roart.controller.*;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringDS;
import roart.etl.db.Extract;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.Parameters;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLMeta;
import roart.model.data.StockData;
import roart.model.io.IO;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.ExtraReader;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties")
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
//@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
@SpringBootTest(classes = IclijController.class ) //(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, DbSpring.class, ConfigDb.class, SimConfig.class } )
public class BenchmarkPipelineET {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;

    IclijDbDao iclijDbDao;

    // no autowiring
    IclijConfig conf = null;

    private static final ObjectMapper mapper = JsonMapper.builder().build();

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

    Inmemory inmemory;

    private InmemoryFactory inmemoryFactory = new InmemoryFactory();

    private CommunicationFactory communicationFactory = new TestCommunicationFactory();

    private TestUtils testutils;

    @BeforeAll
    public void before() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "coreconfig", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.FALSE);
        conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        //conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.FALSE);

        dbDao = new DbDao(conf, dataSource);
        iclijDbDao = new IclijDbDao(iconf, dbSpringDS);

        webFluxUtil = new TestWebFluxUtil(conf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = new TestCuratorFramework();

        inmemory = inmemoryFactory.get(iconf);

        //DbDao coreDbDao = new DbDao(iconf, dataSource);
        io = new IO(iclijDbDao, dbDao, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil) webFluxUtil).setIo(io);
        ((TestCommunicationFactory) communicationFactory).setIo(io);
        ((TestCommunicationFactory) communicationFactory).setConfig(iconf);

        ac = new ActionThread(iconf, io);

        testutils = new TestUtils(iconf, io);
        //String content = "";
        //new Sim(iconf, dbDao, fileSystemDao).method((String) content, "sim", true);
        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

    }


    @Test
    public void test() throws Exception {
        String market = System.getenv("MARKET");
        conf.getConfigData().setMarket(market);
        List<String> disableList = new ArrayList<>();
        StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true);

        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMlclassify(false);
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMldynamic(true);

        IndicatorUtils iu = new IndicatorUtils();

        log.info("Stock data keys: {}", stockData.marketdatamap.keySet());

        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = getMapMap(conf, stockData, neuralnetcommand, iu, inmemory, 0);

        // Clear state
        testutils.cacheinvalidate();
        //inmemory.stat();

        for (int i = 0, batchSize = 128; i < 5; i++, batchSize *= 2) {
            // Recreate pipeline data for batched test
            // Test with batched mode
            conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);

            Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMapBatch = getMapMap(conf, stockData, neuralnetcommand, iu, inmemory, batchSize);

            boolean compared = new MLMulti(conf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, new SerialPipeline(), neuralnetcommand, stockData.stockdates, inmemory).compareMaps(mapMap, mapMapBatch);

            log.info("map batch {}", mapMapBatch);
            log.info("map no batch {}", mapMap);
            log.info("Compared {}", compared);
            assertTrue(compared, "Batched and non-batched mapMap should be equivalent");
        }
        //inmemory.stat();

        // Verify both completed successfully without errors
        log.info("Both batched and non-batched processing completed successfully");
        //assertEquals(true, inmemory.isEmpty());

    }

    private Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> getMapMap(IclijConfig conf, StockData stockData, NeuralNetCommand neuralnetcommand, IndicatorUtils iu, Inmemory inmemory, int batchSize) throws Exception {
        conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, batchSize);
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, batchSize);
        log.info("Running test with batch size: {}", iconf.wantsInmemoryPipelineBatchsize());
        long time = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        String uuid = UUID.randomUUID().toString();

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

        PipelineUtils.setPipelineMap(pipelinedata, id + "/" + uuid);
        pipelinedata = PipelineUtils.setPipelineMap(pipelinedata, inmemory, io.getCuratorClient());

        IndicatorAggregator mlMulti = new MLMulti(conf, stockData.catName, stockData.catName, stockData.cat, stockData.idNameMap, pipelinedata, neuralnetcommand, stockData.stockdates, inmemory);
        //mlMulti.putData();
        String key = stockData.catName;
        SerialPipeline datareader  = PipelineUtils.getPipelines(pipelinedata, key, inmemory);
        //PipelineUtils.getPipelineValue(datareaders, key, PipelineConstants.TRUNCFILLLIST, inmemory));
        //Map<String, double[][]>  base100FillListMap = PipelineUtils.sconvertMapdd(PipelineUtils.getPipelineValue(datareaders, key, PipelineConstants.TRUNCBASE100FILLLIST, inmemory)) ;
        Double threshold = 1.0;
        Map<IndicatorAggregator.SubType, MLMeta> metaMap = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = mlMulti.getMapMap(threshold, metaMap);

        //inmemory.stat();
        log.info("time {}", (System.currentTimeMillis() - time) / 1000);
        new PipelineThreadUtils(conf, inmemory, io.getCuratorClient()).cleanPipeline(id, uuid);

        return mapMap;
    }}
