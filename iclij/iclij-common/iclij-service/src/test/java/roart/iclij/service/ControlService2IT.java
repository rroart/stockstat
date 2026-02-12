package roart.iclij.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.CommunicationConstants;
import roart.common.util.ServiceConnectionUtil;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.model.io.IO;

public class ControlService2IT {

    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    @Autowired
    private IO io;

    private static final boolean SENDRECEIVE = true;
    private IclijConfig iclijConfig;
    public void t0(String service) {
        final String MISCSERVICES = "{ \"TSTSR\" : \"camel\", \"TSTSR2\" : \"spring\", \"TSTSR3\" : \"kafka\", \"TSTSR4\" : \"pulsar\" }";
        final String MISCCOMMUNICATIONS = "{ \"camel\" : \"rabbitmq://localhost:5672\", \"spring\" : \"localhost\", \"kafka\" : \"192.168.122.219:9092\", \"pulsar\" : \"pulsar://kafka9:6650\" }";
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, MISCSERVICES, MISCCOMMUNICATIONS);
        System.out.println("scsc" + sc.getLeft() + " " + sc.getRight());
        ObjectMapper objectMapper = new ObjectMapper();
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath(service);
        Communication c = new CommunicationFactory().get(sc.getLeft(), IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, sc.getRight(), null, webFluxUtil);
        System.out.println("zzz0");
        IclijServiceResult[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        //if (true) return;
        System.out.println("zzz"+result[0]);
        System.out.println(param.getWebpath());
        param.setWebpath(service);
        Communication c2 = new CommunicationFactory().get(sc.getLeft(), IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, sc.getRight(), null, webFluxUtil);
        IclijServiceResult[] result2 = new ControlService(iclijConfig, io).sendReceive(c2, param);
        System.out.println("zzz"+result2[0]);
        System.out.println(param.getWebpath());
    }
    // TODO @Test
    public void t() {
        t0("TSTSR3");
    }
    public <T> T sendReceive(Communication c, Object param) {
        T r = (T) c.sendReceive(param);
        return r;
    }
    private <T> T sendCMe(Class<T> myclass, IclijServiceParam param, String service) {
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, "{}", "{}");
        T[] result;// = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG        
        System.out.println("mnc"+myclass);
        Communication c = new CommunicationFactory().get(sc.getLeft(), myclass, service, new ObjectMapper(), true, true, true, sc.getRight(), null, webFluxUtil);
        param.setWebpath(c.getReturnService());
        //result = sendReceive(c, param);
        result = (T[]) c.sendReceive(param);
        return (T) result[0];
    }

    //@Test
    public void t0() {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(null);
        IclijServiceResult result = sendCMe(IclijServiceResult.class, param, "getconfig");
        //System.out.println("rr"+result.getMaps());
    }
    //@Test
    public void t2() {
        System.out.println("t2");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.SPRING, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "localhost", null, webFluxUtil);
        IclijServiceResult result[] = new ControlService(iclijConfig, io).sendReceive(c, param);
        System.out.println("zzz"+result[0]);
        System.out.println("t2" + param.getWebpath());
    }
    //@Test
    public void t3() {
        System.out.println("t3");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.KAFKA, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "kafka9:9092", null, webFluxUtil);
        IclijServiceResult[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        System.out.println("t3" + param.getWebpath());
    }
    //@Test
    public void t4() {
        System.out.println("t4");
        IclijServiceParam param = new IclijServiceParam();
        param.setWebpath("TST");
        ObjectMapper objectMapper = new ObjectMapper();
        Communication c = new CommunicationFactory().get(CommunicationConstants.PULSAR, IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, SENDRECEIVE, "pulsar://kafka9:6650", null, webFluxUtil);
        IclijServiceResult[] result = new ControlService(iclijConfig, io).sendReceive(c, param);
        System.out.println("t4" + param.getWebpath());
    }
}
