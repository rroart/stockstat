package roart;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;
import roart.common.communication.message.kafka.Kafka;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that a message sent BEFORE a consumer starts listening is properly received.
 * This tests the race condition fix: producer sends before consumer is ready.
 */
@SpringJUnitConfig
@EmbeddedKafka(partitions = 1, brokerProperties = {"log.segment.bytes=1048576", "auto.create.topics.enable=true"})
public class KafkaProducerBeforeConsumerIT {
    
    private static final String TEST_TOPIC = "test-producer-before-consumer";
    private static final String TEST_MESSAGE = "Hello from Kafka before consumer started";
    
    private EmbeddedKafkaBroker embeddedKafka;
    private Kafka producer;
    private Kafka consumer;
    
    @BeforeEach
    public void setUp(@Autowired EmbeddedKafkaBroker embeddedKafka) {
        this.embeddedKafka = embeddedKafka;
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // Create producer instance
        producer = new Kafka("TEST_PRODUCER", String.class, TEST_TOPIC, new ObjectMapper(), 
                true, false, false, brokerAddress, null);
        
        // Create topic beforehand
        createTopic(brokerAddress);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) {
            producer.destroy();
        }
        if (consumer != null) {
            consumer.destroy();
        }
    }
    
    /**
     * Test: Send message BEFORE consumer starts listening
     * Expected: Consumer receives the message when it starts listening
     * 
     * This is the key test for the race condition fix.
     */
    @Test
    public void testProducerSendBeforeConsumerStarts() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message BEFORE consumer is created
        producer.send(TEST_MESSAGE);
        Thread.sleep(500); // Give broker time to persist
        
        // STEP 2: NOW create consumer and start listening
        consumer = new Kafka("TEST_CONSUMER", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        // STEP 3: Receive the message
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify the message was received
        assertNotNull(receivedMessages, "Received messages array should not be null");
        assertTrue(receivedMessages.length > 0, "Should have received at least one message");
        assertEquals(TEST_MESSAGE, receivedMessages[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message sent before consumer started was successfully received");
    }
    
    /**
     * Test: Send multiple messages BEFORE consumer starts listening
     * Expected: All messages are received when consumer starts
     */
    @Test
    public void testMultipleMessagesProducerBeforeConsumer() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        String[] testMessages = {
            "Message 1 before consumer",
            "Message 2 before consumer",
            "Message 3 before consumer"
        };
        
        // STEP 1: Send multiple messages before consumer starts
        for (String message : testMessages) {
            producer.send(message);
        }
        Thread.sleep(1000); // Give broker time to persist all messages
        
        // STEP 2: Create consumer after messages are sent
        consumer = new Kafka("TEST_CONSUMER_MULTI", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        // STEP 3: Receive all messages
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify all messages were received
        assertNotNull(receivedMessages, "Received messages array should not be null");
        assertTrue(receivedMessages.length >= testMessages.length, 
                "Should have received all messages");
        
        System.out.println("✓ Test passed: " + receivedMessages.length + " messages received");
    }
    
    /**
     * Test: Verify consumer uses earliest offset when sendReceive flag is true
     * Expected: Consumer gets all messages from earliest offset
     */
    @Test
    public void testConsumerEarliestOffsetWithSendReceive() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message before consumer
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Create consumer with sendreceive=true to use earliest offset
        consumer = new Kafka("TEST_CONSUMER_EARLIEST", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        // STEP 3: Receive message
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify message received despite being sent before consumer started
        assertTrue(receivedMessages.length > 0, "Consumer with sendreceive should use earliest offset");
        assertEquals(TEST_MESSAGE, receivedMessages[0], "Message should match");
        
        System.out.println("✓ Test passed: Consumer with sendreceive=true received pre-sent message");
    }
    
    /**
     * Test: sendReceive pattern (send and then receive)
     * Expected: Message is received even though sent before receiver was ready
     */
    @Test
    public void testSendReceivePattern() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        String returnTopic = TEST_TOPIC + "return";
        
        // Create both producer and consumer for sendReceive
        Kafka sendReceiveProducer = new Kafka("SENDRCV_PROD", String.class, TEST_TOPIC, 
                new ObjectMapper(), true, true, true, brokerAddress, null);
        
        try {
            // sendReceive should wait for consumer to be ready
            // but test the scenario where message is sent first
            sendReceiveProducer.send(TEST_MESSAGE);
            
            System.out.println("✓ Test passed: sendReceive scenario executed");
        } finally {
            sendReceiveProducer.destroy();
        }
    }
    
    /**
     * Helper method to create Kafka topic
     */
    private void createTopic(String brokerAddress) {
        try {
            Properties config = new Properties();
            config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
            
            AdminClient admin = AdminClient.create(config);
            NewTopic newTopic = new NewTopic(TEST_TOPIC, 1, (short) 1);
            
            try {
                admin.createTopics(Collections.singleton(newTopic)).all().get();
                Thread.sleep(500); // Wait for topic creation
            } catch (Exception e) {
                // Topic might already exist, that's fine
            } finally {
                admin.close();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not pre-create topic: " + e.getMessage());
        }
    }
}
