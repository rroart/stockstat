package roart.testdata;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.common.constants.CommunicationConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.stereotype.Component;

//@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../config/test/application.properties") 
@Component
public class TestUtils {
    
    @Autowired
    private TestConfiguration config;
    
    public Map<String, String> createServiceMap(String value) {
        Map<String, String> map = new HashMap<>();
        map.put(ServiceConstants.EVOLVEFILTERABOVEBELOW, value);
        map.put(ServiceConstants.EVOLVEFILTEREVOLVE, value);
        map.put(ServiceConstants.EVOLVEFILTERFILTER, value);
        map.put(ServiceConstants.EVOLVEFILTERPROFIT, value);
        map.put(ServiceConstants.SIMAUTO, value);
        map.put(ServiceConstants.SIMFILTER, value);
        return map;
    }
    
    public String createServicesAsString(String value) {
        return JsonUtil.convert(createServiceMap(value));
    }

    public Map<String, String> createCommunicationMap() {
        Map<String, String> map = new HashMap<>();
        map.put(CommunicationConstants.CAMEL, config.getServerCamel());
        map.put(CommunicationConstants.KAFKA, config.getServerKafka());
        map.put(CommunicationConstants.PULSAR, config.getServerPulsar());
        map.put(CommunicationConstants.SPRING, config.getServerSpring());
        return map;
    }

    public String createCommunicationAsString() {
        return JsonUtil.convert(createCommunicationMap());
    }

}
