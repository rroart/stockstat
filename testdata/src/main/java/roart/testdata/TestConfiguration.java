package roart.testdata;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class TestConfiguration {

    @Value("${" + TestConstants.SERVER_CAMEL + "}")
    private String serverCamel;

    @Value("${" +TestConstants.SERVER_KAFKA + "}")
    private String serverKafka;

    @Value("${" +TestConstants.SERVER_PULSAR + "}")
    private String serverPulsar;

    @Value("${" +TestConstants.SERVER_SPRING + "}")
    private String serverSpring;

    public String getServerCamel() {
        return serverCamel;
    }

    public String getServerKafka() {
        return serverKafka;
    }

    public String getServerPulsar() {
        return serverPulsar;
    }

    public String getServerSpring() {
        return serverSpring;
    }
     
}
