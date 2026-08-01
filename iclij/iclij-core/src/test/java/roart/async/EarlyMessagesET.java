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
import org.springframework.test.context.TestPropertySource;
import roart.action.ActionThread;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.MyDataSource;
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
import roart.model.io.IO;
import roart.model.io.util.IOUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitConfig
@SpringBootTest(classes = IclijController.class)
public class EarlyMessagesET {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;

    IclijDbDao iclijDbDao;

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
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.FALSE);
        iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCSERVICESREST, Boolean.FALSE);

        dbDao = new DbDao(iconf, dataSource);
        iclijDbDao = new IclijDbDao(iconf, dbSpringDS);

        webFluxUtil = new TestWebFluxUtil(iconf, null);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = new TestCuratorFramework();

        inmemory = inmemoryFactory.get(iconf);

        io = new IO(iclijDbDao, dbDao, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil) webFluxUtil).setIo(io);

        ac = new ActionThread(iconf, io);

        testutils = new TestUtils(iconf, io);
        MyCache.setCache(iconf.wantCache());
        MyCache.setCacheTTL(iconf.getCacheTTL());

        String myservices = iconf.getMyservices();
        myservices = new ServiceConnectionUtil().getMyServices(ServiceConstants.CORE, myservices);
        String services = iconf.getServices();
        String communications = iconf.getCommunications();
        log.info("Myservices {}", myservices);
        new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iconf.copy(), io).start();
    }

    @Test
    public void testEarlyMessagesWithDifferentCommunications() {
        String origserv = iconf.getServices();
        String[] comms = new String[] { "pulsar", "spring", "camel" };
        comms = new String[] { "spring", "camel" };
        comms = new String[] { "camel" };
        String myservices = "{ \"hello\" : \"\" }";
        String servicesPulsar = "{ \"hello\" : \"pulsar\" }";
        String servicesSpring = "{ \"hello\" : \"spring\" }";
        String servicesCamel = "{ \"hello\" : \"camel\" }";
        String communications = iconf.getCommunications();
        
        log.info("Testing early messages with different communication mechanisms: {}", comms);
        Map<String, String> allservicesMap = Map.of("camel", servicesCamel);
        allservicesMap = Map.of("spring", servicesSpring);
        
        for (Map.Entry<String, String> entry : allservicesMap.entrySet()) {
            String key = entry.getKey();
            String service = entry.getValue();
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iconf.getConfigData());
            try {
                String serv = origserv.replaceAll("kafka", key);
                iconf.getConfigData().getConfigValueMap().put(ConfigConstants.MISCSERVICES, serv);
                
                // Send message BEFORE service is started (early message)
                log.info("Sending early message before consumer starts listening for {}", key);
                IclijServiceResult rEarly = new IOUtils(io, iconf, null).sendReceive(IclijServiceResult.class, param, ServiceConstants.HELLO);
                
                // Now start the service controller
                log.info("Starting service controller for {}", key);
                new ServiceControllerOther(myservices, service, communications, IclijServiceParam.class, iconf.copy(), io).start();
                
                // Wait for consumer to be ready
                Thread.sleep(10000);
                
                // Send message AFTER service is started (normal message)
                log.info("Sending message after consumer started for {}", key);
                IclijServiceResult r = new IOUtils(io, iconf, null).sendReceive(IclijServiceResult.class, param, ServiceConstants.HELLO);
                
                log.info("Early message result {} - error: {}", key, rEarly.getError());
                log.info("Normal message result {} - error: {}", key, r.getError());
                assertNotNull(r.getError());
            } catch (Exception e) {
                log.error("Test failed for communication {}", key, e);
                e.printStackTrace();
            }
        }
    }
}
