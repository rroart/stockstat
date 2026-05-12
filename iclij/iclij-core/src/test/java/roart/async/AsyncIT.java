package roart.async;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import roart.action.ActionThread;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.MyDataSource;
import roart.common.queue.QueueElement;
import roart.common.util.ServiceConnectionUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.controller.*;
import roart.core.service.ServiceControllerOther;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.model.Parameters;
import roart.iclij.service.ControlService;
import roart.model.io.IO;
import roart.model.io.util.IOUtils;
import roart.testdata.TestConstants;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

//@Disabled
@EmbeddedKafka // did not work (ports = { 9092 })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties")
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
public class AsyncIT {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    //, SpringAboveBelowRepository.class

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;

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

    Inmemory inmemory;

    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new CommunicationFactory();

    private TestUtils testutils;
    TestDataSource[][] periodDataSources;

    @BeforeAll
    public void before() throws Exception {
        String brokers = kafkaEmbedded.getBrokersAsString();
        System.out.println("brokers" + brokers);
        //String[] split = brokers.split(":");
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCCOMMUNICATIONS, "{ \"kafka\" : \"" + brokers + "\"}");
        System.out.println("before" + System.getProperty("config") + " "+ System.getProperty("coreconfig"));
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCSERVICESREST, Boolean.FALSE);
        log.info("Wants {}", iconf.wantsInmemoryPipeline());
        ConfigMaps configMaps = IclijConfig.instanceC();
        //conf = new IclijConfig(configMaps, "coreconfig", null);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCCOMMUNICATIONS, "{ \"kafka\" : \"" + brokers + "\"}");
        //conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.FALSE);
        //String market = TestConstants.MARKET;
        //dataSource = new TestDataSource(conf, new TimeUtil().convertDate2("2024.01.01"), new TimeUtil().convertDate2("2025.01.01"), market, 26, false, Constants.INDEXVALUECOLUMN, false, new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null);

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

        CuratorFramework curatorClient = new TestCuratorFramework();

        inmemory = inmemoryFactory.get(iconf);

        io = new IO(iclijDbDao, dbDao, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil)webFluxUtil).setIo(io);
        //((CommunicationFactory)communicationFactory).setIo(io);
        //((CommunicationFactory)communicationFactory).setConfig(iconf);

        ac = new ActionThread(iconf, io);

        testutils = new TestUtils(iconf, io);
        //String content = "";
        //new Sim(iconf, dbDao, fileSystemDao).method((String) content, "sim", true);
        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

        String myservices = iconf.getMyservices();
        log.info("myservices {}", myservices);
        myservices = new ServiceConnectionUtil().getMyServices(ServiceConstants.CORE, myservices);
        log.info("myservices {}", myservices);
        String services = iconf.getServices();
        log.info("services {}", services);
        String communications = iconf.getCommunications();
        new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iconf.copy(), io).start();
    }

    @Test
    public void test() throws Exception {
        //if (true) return;
        iconf.getConfigData().setMarket(TestConstants.MARKET);
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        QueueElement element = new QueueElement();
        InmemoryMessage msg = inmemory.send(ServiceConstants.SIMFILTER + UUID.randomUUID(), param, null);
        //element.setOpid(ServiceConstants.SIM);
        element.setMessage(msg);
        //IclijServiceParam serviceparam = new IclijServiceParam();
        //element.setParam(serviceparam);
        log.info("icsev" + iconf.getServices());
        new IOUtils(io, iconf, null).send(EurekaConstants.GETCONFIG, element);
        Thread.sleep(10000);
        //new IOUtils(io, iconf, null).sendReceive(EurekaConstants.GETCONFIG, element, null);
        IclijServiceResult r = new IOUtils(io, iconf, null).sendReceive(IclijServiceResult.class, param, EurekaConstants.GETCONFIG);
        log.info("ConfigData {}", r.getConfigData());
        assertNotNull(r.getConfigData());

    }
}
