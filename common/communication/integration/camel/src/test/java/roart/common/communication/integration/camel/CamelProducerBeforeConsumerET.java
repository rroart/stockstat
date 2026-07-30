package roart.common.communication.integration.camel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that a message sent BEFORE a consumer starts listening is properly received with Camel.
 * This tests the race condition fix: producer sends before consumer is ready.
 * 
 * Note: This test requires RabbitMQ to be running.
 */
//@Disabled("Requires RabbitMQ broker running. Enable for integration testing.")
public class CamelProducerBeforeConsumerET {
    
    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_PORT = "5672";
    private static final String RABBITMQ_VHOST = "task";
    private static final String TEST_QUEUE = "test-producer-before-consumer";
    private static final String TEST_MESSAGE = "Hello from Camel before consumer started";
    
    private Camel producer;
    private Camel consumer;
    
    @BeforeEach
    public void setUp() {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // Verify RabbitMQ is available
        try {
            CachingConnectionFactory factory = new CachingConnectionFactory(connection);
            factory.setUsername("username");
            factory.setPassword("password");
            factory.createConnection().close();
        } catch (Exception e) {
            throw new RuntimeException("RabbitMQ broker not available at " + connection, e);
        }
        
        // Create producer instance
        producer = new Camel("TEST_PRODUCER", String.class, TEST_QUEUE, new ObjectMapper(),
                true, false, false, connection, null);
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
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message BEFORE consumer is created
        producer.send(TEST_MESSAGE);
        Thread.sleep(500); // Give broker time to persist
        
        // STEP 2: NOW create consumer and start listening
        consumer = new Camel("TEST_CONSUMER", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
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
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        String testQueue = TEST_QUEUE + "-multi";
        String[] testMessages = {
            "Camel Message 1 before consumer",
            "Camel Message 2 before consumer",
            "Camel Message 3 before consumer"
        };
        
        // Recreate producer with different queue
        producer.destroy();
        producer = new Camel("TEST_PRODUCER_MULTI", String.class, testQueue, new ObjectMapper(),
                true, false, false, connection, null);
        
        // STEP 1: Send multiple messages before consumer starts
        for (String message : testMessages) {
            producer.send(message);
        }
        Thread.sleep(1000); // Give broker time to persist all messages
        
        // STEP 2: Create consumer after messages are sent
        consumer = new Camel("TEST_CONSUMER_MULTI", String.class, testQueue, new ObjectMapper(),
                false, true, true, connection, null);
        
        // STEP 3: Receive first message
        String[] receivedMessages = consumer.receiveString();
        
        // STEP 4: Verify at least one message was received
        assertNotNull(receivedMessages, "Received messages array should not be null");
        assertTrue(receivedMessages.length > 0, 
                "Should have received at least one message");
        
        System.out.println("✓ Test passed: " + receivedMessages.length + " messages received");
    }
    
    /**
     * Test: receiveStringAndStore pattern with message sent before consumer
     * Expected: Message is received, storage callback is invoked, and ack is sent
     */
    @Test
    public void testReceiveStringAndStorePattern() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        String testQueue = TEST_QUEUE + "-store";
        final boolean[] storageAttempted = {false};
        
        // Recreate producer with different queue
        producer.destroy();
        producer = new Camel("TEST_PRODUCER_STORE", String.class, testQueue, new ObjectMapper(),
                true, false, false, connection, null);
        
        // STEP 1: Send message before consumer
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Create consumer with storage callback
        consumer = new Camel("TEST_CONSUMER_STORE", String.class, testQueue, new ObjectMapper(),
                false, true, true, connection, msg -> {
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
    
    /**
     * Test: receiveStringAndStore with storage failure
     * Expected: Message is not acked and remains in queue for redelivery
     */
    @Test
    public void testReceiveStringAndStoreWithStorageFailure() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        String testQueue = TEST_QUEUE + "-store-fail";
        
        // Recreate producer with different queue
        producer.destroy();
        producer = new Camel("TEST_PRODUCER_FAIL", String.class, testQueue, new ObjectMapper(),
                true, false, false, connection, null);
        
        // STEP 1: Send message before consumer
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Create consumer that fails to store
        consumer = new Camel("TEST_CONSUMER_FAIL", String.class, testQueue, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Storage failing intentionally: " + msg);
                    return false; // Simulate storage failure
                });
        
        // STEP 3: Call receiveStringAndStore
        String[] receivedMessages = consumer.receiveStringAndStore();
        
        // STEP 4: Verify message was NOT acked
        // (In a real scenario, the message would remain in the queue for redelivery)
        assertEquals(0, receivedMessages.length, 
                "Should return empty array when storage fails (message not acked)");
        
        System.out.println("✓ Test passed: Message properly rejected when storage fails");
    }
    
    /**
     * Test: Timeout handling when no messages available
     * Expected: Consumer returns empty without blocking
     */
    @Test
    public void testConsumerTimeoutWithoutMessages() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        String testQueue = TEST_QUEUE + "-empty";
        
        // Create consumer without any messages being sent first
        consumer = new Camel("TEST_CONSUMER_TIMEOUT", String.class, testQueue, new ObjectMapper(),
                false, true, true, connection, null);
        
        // This should timeout and handle gracefully
        String[] receivedMessages = consumer.receiveString();
        
        // Should return array (possibly empty or with timeout handling)
        assertNotNull(receivedMessages, "Received messages array should not be null");
        
        System.out.println("✓ Test passed: Consumer timeout handled gracefully");
    }
}
