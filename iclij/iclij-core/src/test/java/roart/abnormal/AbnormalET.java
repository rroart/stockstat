package roart.abnormal;

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
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MyDataSource;
import roart.common.webflux.WebFluxUtil;
import roart.controller.*;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringDS;
import roart.etl.db.Extract;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.Parameters;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.model.io.IO;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
public class AbnormalET {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IclijConfig iconf = null;

    private IO io;

    IclijDbDao iclijDbDao;

    // no autowiring
    //IclijConfig conf = null;

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

    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new TestCommunicationFactory();

    private TestUtils testutils;

    private TestInmemory inmemory;

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

        dbDao = new DbDao(iconf, dataSource);
        iclijDbDao = new IclijDbDao(iconf, dbSpringDS);

        webFluxUtil = new TestWebFluxUtil(iconf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = new TestCuratorFramework();

        io = new IO(iclijDbDao, dbDao, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil) webFluxUtil).setIo(io);
        ((TestCommunicationFactory) communicationFactory).setIo(io);
        ((TestCommunicationFactory) communicationFactory).setConfig(iconf);

        ac = new ActionThread(iconf, io);

        testutils = new TestUtils(iconf, io);

        inmemory = (TestInmemory) io.getInmemoryFactory().get(iconf.getInmemoryServer(), iconf.getInmemoryHazelcast(), iconf.getInmemoryRedis());

    }

    // TODO marketbased abnorm use

    @Test
    public void test() throws Exception {
        log.info("Wants it {}", iconf.wantsInmemoryPipeline());
        String market = System.getenv("MARKET");
        log.info("Market {}", market);
        iconf.getConfigData().setMarket(market);
        List<String> disableList = new ArrayList<>();

        for (int days : new int[] { 180, 0 }) {

            iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, days);
            iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCMYDAYS, days);

            StockData stockData = new Extract(io.getDbDao()).getStockData(iconf, true);

            IndicatorUtils iu = new IndicatorUtils();
            ExtraReader extraReader = new ExtraReader(iconf, stockData.marketdatamap, 0, stockData);
            Map<String, StockData> extraStockDataMap = new IndicatorUtils().getExtraStockDataMap(iconf, io.getDbDao(), extraReader, true);
            log.info("grr" + stockData.marketdatamap.keySet() + " " + stockData.periodText + " " + extraStockDataMap.keySet());

            for (Double threshold : new Double[] { null, 3.0}) {
                iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCABNORMALCHANGE, threshold);
                handle(iu, stockData, extraStockDataMap, extraReader);
             }
        }
    }

    private void handle(IndicatorUtils iu, StockData stockData, Map<String, StockData> extraStockDataMap, ExtraReader extraReader) throws Exception {
        Pipeline[] datareaders = iu.getDataReaders(iconf, stockData.periodText,
                stockData.marketdatamap, stockData, extraStockDataMap, extraReader, inmemory);
        for (int i = 0; i < 2; i++) {
            Map<String, Double[][]> lm = ((DataReader) datareaders[i]).getListMap();
            if (lm != null) {
                log.info("lm size" + lm.size());
            }
        }
    }
}
