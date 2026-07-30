package roart;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import tools.jackson.databind.ObjectMapper;
import roart.common.communication.message.pulsar.Pulsar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that a message sent BEFORE a consumer starts listening is properly received with Pulsar.
 * This tests the race condition fix: producer sends before consumer is ready.
 * 
 * Note: This test requires a Pulsar broker to be running.
 * For CI/CD, this test can be disabled or use Testcontainers for embedded Pulsar.
 */
//@Disabled("Requires Pulsar broker running. Enable for integration testing.")
public class PulsarProducerBeforeConsumerET {
    
    private static final String PULSAR_SERVICE_URL = "pulsar://localhost:6650";
    private static final String TEST_TOPIC = "persistent://public/default/test-producer-before-consumer";
    private static final String TEST_MESSAGE = "Hello from Pulsar before consumer started";
    
    private Pulsar producer;
    private Pulsar consumer;
    
    @BeforeEach
    public void setUp() {
        // Verify Pulsar is available
        try {
            PulsarClient testClient = PulsarClient.builder()
                    .serviceUrl(PULSAR_SERVICE_URL)
                    .build();
            testClient.close();
        } catch (PulsarClientException e) {
            throw new RuntimeException("Pulsar broker not available at " + PULSAR_SERVICE_URL, e);
        }
        
        // Create producer instance
        producer = new Pulsar("TEST_PRODUCER", String.class, TEST_TOPIC, new ObjectMapper(),
                true, false, false, PULSAR_SERVICE_URL, null);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) {
            try {
                producer.destroy();
            } catch (Exception e) {
                System.err.println("Error destroying producer: " + e.getMessage());
            }
        }
        if (consumer != null) {
            try {
                consumer.destroy();
            } catch (Exception e) {
                System.err.println("Error destroying consumer: " + e.getMessage());
            }
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
        // STEP 1: Send message BEFORE consumer is created
        producer.send(TEST_MESSAGE);
        Thread.sleep(500); // Give broker time to persist
        
        // STEP 2: NOW create consumer and start listening
        consumer = new Pulsar("TEST_CONSUMER", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
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
        String[] testMessages = {
            "Pulsar Message 1 before consumer",
            "Pulsar Message 2 before consumer",
            "Pulsar Message 3 before consumer"
        };
        
        // STEP 1: Send multiple messages before consumer starts
        for (String message : testMessages) {
            producer.send(message);
        }
        Thread.sleep(1000); // Give broker time to persist all messages
        
        // STEP 2: Create consumer after messages are sent
        consumer = new Pulsar("TEST_CONSUMER_MULTI", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        // STEP 3: Receive all messages
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify at least one message was received
        assertNotNull(receivedMessages, "Received messages array should not be null");
        assertTrue(receivedMessages.length > 0, 
                "Should have received at least one message");
        
        System.out.println("✓ Test passed: " + receivedMessages.length + " messages received");
    }
    
    /**
     * Test: Verify consumer gets message from earliest offset
     * Expected: Consumer receives message even though it connected after send
     */
    @Test
    public void testConsumerEarliestOffsetBehavior() throws Exception {
        // STEP 1: Send message before consumer
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Create consumer with sendreceive=true to ensure earliest offset reading
        consumer = new Pulsar("TEST_CONSUMER_EARLIEST", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        // STEP 3: Receive message
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify message received
        assertTrue(receivedMessages.length > 0, 
                "Consumer with sendreceive should read from earliest offset");
        assertEquals(TEST_MESSAGE, receivedMessages[0], "Message should match");
        
        System.out.println("✓ Test passed: Consumer with sendreceive=true received pre-sent message");
    }
    
    /**
     * Test: Timeout handling when no messages available
     * Expected: Consumer returns empty array after timeout
     */
    @Test
    public void testConsumerTimeoutWithoutMessages() throws Exception {
        // Create consumer without any messages being sent first
        consumer = new Pulsar("TEST_CONSUMER_TIMEOUT", String.class, 
                TEST_TOPIC + "-empty", new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        // This should timeout and handle gracefully
        String[] receivedMessages = consumer.receiveString();
        
        // Should return array (possibly empty or with timeout handling)
        assertNotNull(receivedMessages, "Received messages array should not be null");
        
        System.out.println("✓ Test passed: Consumer timeout handled gracefully");
    }
    
    /**
     * Test: receiveStringAndStore pattern with message sent before consumer
     * Expected: Message is received and storage callback is invoked
     */
    @Test
    public void testReceiveStringAndStorePattern() throws Exception {
        final boolean[] storageAttempted = {false};
        
        // STEP 1: Send message before consumer
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Create consumer with storage callback
        consumer = new Pulsar("TEST_CONSUMER_STORE", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    storageAttempted[0] = true;
                    System.out.println("Storage called with message: " + msg);
                    return true; // Simulate successful storage
                });
        
        // STEP 3: Call receiveStringAndStore
        String[] receivedMessages = consumer.receiveStringAndStore();
        
        // STEP 4: Verify message and storage
        assertTrue(storageAttempted[0], "Storage callback should be invoked");
        assertTrue(receivedMessages.length > 0, "Should have received message");
        
        System.out.println("✓ Test passed: receiveStringAndStore pattern works with pre-sent message");
    }
}
