package roart.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.util.ServiceConnectionUtil;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;

@SpringBootTest
public class OtherIT {

    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    @Autowired
    private IclijDbDao dbDao;

    @Autowired
    public IclijConfig iclijConfig;
    
    public void test0(String service) {
        String myservices = "{ \"TSTSR3\" : \"r\" }";
        String services = "{ \"TSTSR\" : \"camel\", \"TSTSR2\" : \"spring\", \"TSTSR3\" : \"kafka\", \"TSTSR4\" : \"pulsar\" }";
        String communications = "{ \"camel\" : \"rabbitmq://localhost:5672\", \"spring\" : \"localhost\", \"kafka\" : \"192.168.122.219:9092\", \"pulsar\" : \"pulsar://kafka9:6650\" }";
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, services, communications);
        System.out.println("scsc" + sc.getLeft() + " " + sc.getRight());
        ObjectMapper objectMapper = new ObjectMapper();
        //Communication comm = CommunicationFactory.get(communication, IclijServiceResult.class, service, objectMapper, false, true, false, connection);
        Communication c = new CommunicationFactory().get(sc.getLeft(), IclijServiceParam.class, service, objectMapper, false, true, false, sc.getRight(), null, webFluxUtil);
        ServiceControllerOther co = new ServiceControllerOther(myservices, services, communications, IclijServiceParam.class, iclijConfig, null);
        co.get(c);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end");
    }

    @Test
    public void test() {
        test0("TSTSR3");
        //test0("TSTSR4");
    }
}
