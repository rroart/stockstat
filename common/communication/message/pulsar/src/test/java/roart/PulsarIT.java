package roart;

import java.util.stream.IntStream;

import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.message.pulsar.Pulsar;
import roart.testdata.TestConfiguration;
import roart.testdata.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
@ComponentScan(basePackages = "roart.testdata")
@SpringBootTest(classes = TestConfiguration.class)
public class PulsarIT {
    //private static final String SERVICE_URL = "pulsar://kafka9:6650";
    private static final String TOPIC_NAME = "test-topic";
    private static final String SUBSCRIPTION_NAME = "subscription name";

    Pulsar pulsar;

    @Autowired
    private TestConfiguration config;
    
    @BeforeEach
    public void b() {
        pulsar = new Pulsar("PULSAR", String.class, TOPIC_NAME, new ObjectMapper(), true, true, false, config.getServerPulsar(), null);
    }

    @Test
    public void t() {
        pulsar.send("hi pulsar");
    }

    @Test
    public void t2() {
        String[] s = pulsar.receiveString();
        System.out.println(s[0]);
    }
}
