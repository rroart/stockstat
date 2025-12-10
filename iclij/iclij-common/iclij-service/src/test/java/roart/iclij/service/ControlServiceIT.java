package roart.iclij.service;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.ServiceConnectionUtil;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.model.io.IO;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.testdata.TestConfiguration;
import roart.testdata.TestUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringJUnitConfig
@TestPropertySource("file:${user.dir}/../../../config/test/application.properties") 
@ComponentScan(basePackages = "roart.testdata")
@SpringBootTest(classes = { TestConfiguration.class, TestUtils.class } )
public class ControlServiceIT {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private static final boolean SENDRECEIVE = false;
    private IclijConfig iclijConfig;
    
    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    @Autowired
    private TestUtils testUtils;
    
    @Autowired
    private IO io;

    @Test
    public void t() {
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.CAMEL, IclijServiceParam.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "rabbitmq://localhost:5672", null, webFluxUtil);
        IclijServiceParam[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        log.info("zzz"+result[0]);
        log.info(param.getWebpath());
    }
    public <T> T sendReceive(Communication c, Object param) {
        T r = (T) c.sendReceive(param);
        return r;
    }
    private <T> T sendCMe(Class<T> myclass, IclijServiceParam param, String service) {
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, "{}", "{}");
        T[] result;// = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG        
        log.info("mnc"+myclass);
        Communication c = new CommunicationFactory().get(sc.getLeft(), myclass, service, new ObjectMapper(), true, true, true, sc.getRight(), null, webFluxUtil);
        param.setWebpath(c.getReturnService());
        //result = sendReceive(c, param);
        result = (T[]) c.sendReceive(param);
        return (T) result[0];
    }

    @Test
    public void t0() {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(null);
        IclijServiceResult result = sendCMe(IclijServiceResult.class, param, "getconfig");
        //log.info("rr"+result.getMaps());
    }
    //@Test
    public void t2() {
        log.info("t2");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.SPRING, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "localhost", this::print, webFluxUtil);
        IclijServiceResult result[] = new ControlService(iclijConfig, io).sendReceive(c, param);
        log.info("zzz"+result[0]);
        log.info("t2" + param.getWebpath());
    }
    //@Test
    public void t3() {
        log.info("t3");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.KAFKA, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "kafka9:9092", this::print, webFluxUtil);
        IclijServiceResult[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        log.info("t3" + param.getWebpath());
    }
    //@Test
    public void t4() {
        log.info("t4");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.PULSAR, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "pulsar://kafka9:6650", this::print, webFluxUtil);
        IclijServiceResult[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        log.info("t4" + param.getWebpath());
    }
    
    @Test
    public void comms() {
        int counter = 0;
        List<String> comms = List.of(CommunicationConstants.KAFKA, CommunicationConstants.PULSAR, CommunicationConstants.CAMEL, CommunicationConstants.SPRING);
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        for (String comm : comms) {
            Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(ServiceConstants.SIMAUTO, testUtils.createServicesAsString(comm), testUtils.createCommunicationAsString());
            Communication c = new CommunicationFactory().get(sc.getLeft(), String.class, comm, objectMapper, true, true, false, sc.getRight(), null, webFluxUtil);
            //Communication c = CommunicationFactory.get(comm, IclijServiceParam.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "rabbitmq://localhost:5672", func);
            c.send("Counter " + comm + " " + counter++);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            String[] result = c.receive();
            log.info("zzz"+result[0]);
            log.info(param.getWebpath());
        }
    }

    @Test
    public void commsZk() {
        int counter = 0;
        List<Function<String, Boolean>> funcs = List.of(this::exception, this::noprint, this::print);
        List<String> commsall = List.of(CommunicationConstants.KAFKA, CommunicationConstants.PULSAR, CommunicationConstants.CAMEL, CommunicationConstants.SPRING);
        List<String> commp = List.of(CommunicationConstants.PULSAR);
        //List<String> commse = List.of(CommunicationConstants.CAMEL, CommunicationConstants.SPRING);
        List<String> commc = List.of(CommunicationConstants.CAMEL);
        List<String> commsp = List.of(CommunicationConstants.SPRING);
        List<String> commk = List.of(CommunicationConstants.KAFKA);
        List<String> comms = commp;
        IclijServiceParam param = new IclijServiceParam();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String comm : comms) {
            for (int i = 0; i < 3; i++) {
                Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(ServiceConstants.SIMAUTO, testUtils.createServicesAsString(comm), testUtils.createCommunicationAsString());
                Communication c = new CommunicationFactory().get(sc.getLeft(), String.class, comm, objectMapper, true, true, false, sc.getRight(), this::print, webFluxUtil);
                //Communication c = CommunicationFactory.get(comm, IclijServiceParam.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "rabbitmq://localhost:5672", func);
                c.send("Counter " + comm + " " + counter++);
            }
        }
        for (String comm : comms) {
            int i = 0;
            for (Function<String, Boolean> func : funcs) {
                Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(ServiceConstants.SIMAUTO, testUtils.createServicesAsString(comm), testUtils.createCommunicationAsString());
                Communication c = new CommunicationFactory().get(sc.getLeft(), String.class, comm, objectMapper, true, true, false, sc.getRight(), func, webFluxUtil);
                //Communication c = CommunicationFactory.get(comm, IclijServiceParam.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "rabbitmq://localhost:5672", func);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                try {
                    String[] result = c.receiveAndStore();
                    log.info("Received " + i + " " + result.length + " " + Arrays.asList(result));
                } catch (Exception e) {
                    log.info("Exception " + e.getMessage());
                }
                i++;
           }
        }
    }

    public boolean print(String string) {
        log.info("print " + string);
        return true;
    }

    public boolean noprint(String string) {
        log.info("noprint " + string);
        return false;
    }

    public boolean exception(String string) {
        throw new RuntimeException("Exception " + string);
    }
}
