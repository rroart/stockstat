package roart.async;

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
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.MyDataSource;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.webflux.WebFluxUtil;
import roart.controller.*;
import roart.core.service.ServiceControllerOther;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringDS;
import roart.filesystem.FileSystemDao;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.Parameters;
import roart.iclij.service.ControlService;
import roart.model.io.IO;
import roart.model.io.util.IOUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class AsyncET {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;

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

    IO io;

    Inmemory inmemory;

    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new CommunicationFactory();

    private TestUtils testutils;
    @BeforeAll
    public void before() throws Exception {
        iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
        ConfigMaps configMaps = IclijConfig.instanceC();
        //conf = new IclijConfig(configMaps, "coreconfig", null);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.FALSE);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCSERVICESREST, Boolean.FALSE);
        //conf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.FALSE);

        dbDao = new DbDao(iconf, dataSource);
        iclijDbDao = new IclijDbDao(iconf, dbSpringDS);

        webFluxUtil = new TestWebFluxUtil(iconf,null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = new TestCuratorFramework();

        inmemory = inmemoryFactory.get(iconf);

        //DbDao coreDbDao = new DbDao(iconf, dataSource);
        io = new IO(iclijDbDao, dbDao , webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
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
        myservices = new ServiceConnectionUtil().getMyServices(ServiceConstants.CORE, myservices);
        String services = iconf.getServices();
        String communications = iconf.getCommunications();
        log.info("Myservices {}", myservices);
        new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iconf.copy(), io).start();
        // TODO ...
        //new roart.machinelearning.service.ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, conf.copy(), io).start();
    }

    @Test
    public void test() {
        //if (true) return;
        String market = System.getenv("MARKET");
        iconf.getConfigData().setMarket(market);

        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(iconf.getConfigData());
        try {
            Thread.sleep(60000);
            log.info("Sending");
            IclijServiceResult r = new IOUtils(io, iconf, null).sendReceive(IclijServiceResult.class, param, EurekaConstants.GETCONFIG);
            log.info("ConfigData {}", r.getConfigData());
            assertNotNull(r.getConfigData());
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    @Test
    public void testOther() {
        String origserv = iconf.getServices();
        String[] comms = new String[] { "pulsar", "spring", "camel" };
        String myservices = "{ \"hello\" : \"\" }";
        String servicesPulsar = "{ \"hello\" : \"pulsar\" }";
        String servicesSpring = "{ \"hello\" : \"spring\" }";
        String servicesCamel = "{ \"hello\" : \"camel\" }";
        String communications = iconf.getCommunications();
        log.info("Myservices {}", myservices);
        Map<String, String> allservicesMap = Map.of("pulsar", servicesPulsar, "spring", servicesSpring, "camel", servicesCamel);
        allservicesMap = Map.of("camel", servicesCamel);
        allservicesMap = Map.of("spring", servicesSpring);
        for (Map.Entry<String, String> entry : allservicesMap.entrySet()) {
            String key = entry.getKey();
            String service = entry.getValue();
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iconf.getConfigData());
            try {
                String serv = origserv.replaceAll("kafka", key);
                iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCSERVICES, serv);
                new ServiceControllerOther(myservices, service, communications, IclijServiceParam.class, iconf.copy(), io).start();
                Thread.sleep(10000);
                log.info("Sending {}", key);
                IclijServiceResult r = new IOUtils(io, iconf, null).sendReceive(IclijServiceResult.class, param, ServiceConstants.HELLO);
                log.info("Return {} {}", key, r.getError());
                assertNotNull(r.getError());
            } catch (Exception e) {
                e.printStackTrace();;
            }

        }
    }
}
