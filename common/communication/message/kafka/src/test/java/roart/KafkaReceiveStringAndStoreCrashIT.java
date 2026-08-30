package roart;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
 * Tests to verify that Kafka.receiveStringAndStore properly handles consumer crashes.
 * 
 * When a consumer crashes BEFORE calling acknowledge/nack:
 * - Message should remain in queue
 * - Message should be redelivered to another consumer
 * - No messages should be silently lost
 */
@Disabled
@SpringJUnitConfig
@EmbeddedKafka(partitions = 1, brokerProperties = {"log.segment.bytes=1048576", "auto.create.topics.enable=true"})
public class KafkaReceiveStringAndStoreCrashIT {
    
    private static final String TEST_TOPIC = "test-crash-recovery";
    private static final String TEST_MESSAGE = "Test message for crash recovery";
    
    private EmbeddedKafkaBroker embeddedKafka;
    private Kafka producer;
    private Kafka consumer1;
    private Kafka consumer2;
    
    @BeforeEach
    public void setUp(@Autowired EmbeddedKafkaBroker embeddedKafka) {
        this.embeddedKafka = embeddedKafka;
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        producer = new Kafka("TEST_PRODUCER", String.class, TEST_TOPIC, new ObjectMapper(), 
                true, false, false, brokerAddress, null);
        
        createTopic(brokerAddress);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) producer.destroy();
        if (consumer1 != null) try { consumer1.destroy(); } catch (Exception e) {}
        if (consumer2 != null) try { consumer2.destroy(); } catch (Exception e) {}
    }
    
    /**
     * Test: Consumer crashes before acknowledging message
     * Scenario: Storage succeeds but consumer thread dies before sending ack
     * Expected: Message is not acked and can be redelivered
     */
    @Test
    public void testCrashBeforeAcknowledge() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer1 receives and processes, but crashes BEFORE acking
        final boolean[] storageInvoked = {false};
        final boolean[] ackSent = {false};
        
        consumer1 = new Kafka("CONSUMER_1", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    storageInvoked[0] = true;
                    // Simulate crash - storage succeeds but no ack/nack sent
                    // In real scenario, thread would crash here
                    System.out.println("Received and stored: " + msg);
                    return true;
                });
        
        // STEP 3: Receive with storage but DON'T acknowledge
        String[] received1 = consumer1.receiveStringAndStore();
        
        assertTrue(storageInvoked[0], "Storage should be invoked");
        assertTrue(received1.length > 0, "Should receive message");
        
        // Simulate crash by destroying consumer without proper cleanup
        // In Kafka, if consumer doesn't call commit, next poll won't get the message
        consumer1.destroy();
        
        System.out.println("Consumer1 'crashed' - message should be redelivered");
        
        // STEP 4: New consumer tries to get the message
        // Due to unique consumer group, should get it again
        consumer2 = new Kafka("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        // With unique group ID in sendreceive mode, should get message again
        String[] received2 = consumer2.receiveString();
        
        // Should receive the message in consumer2 (message was not acked)
        assertTrue(received2.length > 0, 
                "Message should be redelivered to new consumer after crash");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message was redelivered after consumer crash");
    }
    
    /**
     * Test: Storage callback throws exception (simulates crash during processing)
     * Expected: Message should not be acknowledged and remains in queue
     */
    @Test
    public void testStorageCallbackThrowsException() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with failing storage callback
        consumer1 = new Kafka("CONSUMER_FAIL", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    // Simulate crash during storage processing
                    throw new RuntimeException("Storage processing failed!");
                });
        
        // STEP 3: Receive with storage - should handle exception gracefully
        String[] received = consumer1.receiveStringAndStore();
        
        // Should return empty array because message was not stored successfully
        assertEquals(0, received.length, 
                "Should return empty array when storage fails");
        
        System.out.println("Consumer with failing storage handled exception");
        
        // STEP 4: New consumer should be able to get message
        consumer2 = new Kafka("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        String[] received2 = consumer2.receiveString();
        
        // Message should be available for new consumer (was nacked by consumer1)
        assertTrue(received2.length > 0, 
                "Message should be available after storage exception");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message redelivered after storage exception");
    }
    
    /**
     * Test: Storage callback returns false (simulates validation failure)
     * Expected: Message should be negatively acknowledged and redelivered
     */
    @Test
    public void testStorageCallbackReturnsFalse() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with storage that rejects message
        consumer1 = new Kafka("CONSUMER_REJECT", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    System.out.println("Storage rejected message: " + msg);
                    return false; // Simulate validation failure
                });
        
        // STEP 3: Receive with storage
        String[] received = consumer1.receiveStringAndStore();
        
        // Should return empty because storage returned false
        assertEquals(0, received.length, 
                "Should return empty array when storage returns false");
        
        System.out.println("Consumer rejected message during validation");
        
        // STEP 4: New consumer should get the rejected message
        consumer2 = new Kafka("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be redelivered after rejection by first consumer");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message redelivered after validation failure");
    }
    
    /**
     * Test: Timeout during receiveStringAndStore
     * Expected: Consumer returns empty, message remains in queue
     */
    @Test
    public void testTimeoutDuringReceive() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // Consumer waits for message but none available
        consumer1 = new Kafka("CONSUMER_TIMEOUT", String.class, TEST_TOPIC + "-empty", 
                new ObjectMapper(), false, true, true, brokerAddress, msg -> {
                    return true;
                });
        
        // Should timeout and return empty
        long startTime = System.currentTimeMillis();
        String[] received = consumer1.receiveStringAndStore();
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertEquals(0, received.length, "Should return empty on timeout");
        assertTrue(elapsed < 70000, "Should timeout reasonably (60s timeout)");
        
        System.out.println("✓ Test passed: Timeout handled correctly (" + elapsed + "ms)");
    }
    
    /**
     * Test: Multiple crashes and redeliveries
     * Expected: Message survives multiple consumer crashes and is eventually delivered
     */
    @Test
    public void testMultipleCrashesAndRedeliveries() throws Exception {
        String brokerAddress = embeddedKafka.getBrokersAsString();
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: First consumer crashes
        consumer1 = new Kafka("CRASH_1", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    throw new RuntimeException("First crash!");
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertEquals(0, received1.length, "First consumer should get nothing (crashed)");
        consumer1.destroy();
        
        // STEP 3: Second consumer crashes
        consumer2 = new Kafka("CRASH_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    return false; // Reject and crash
                });
        
        String[] received2 = consumer2.receiveStringAndStore();
        assertEquals(0, received2.length, "Second consumer should get nothing (rejected)");
        consumer2.destroy();
        
        // STEP 4: Third consumer successfully processes
        final boolean[] finalStorageSuccess = {false};
        Kafka consumer3 = new Kafka("SUCCESS_3", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, brokerAddress, msg -> {
                    finalStorageSuccess[0] = true;
                    System.out.println("Successfully processed: " + msg);
                    return true;
                });
        
        String[] received3 = consumer3.receiveStringAndStore();
        
        assertTrue(finalStorageSuccess[0], "Final consumer should process successfully");
        assertTrue(received3.length > 0, "Should receive message on third attempt");
        assertEquals(TEST_MESSAGE, received3[0], "Message should match");
        
        consumer3.destroy();
        
        System.out.println("✓ Test passed: Message survived multiple crashes and was delivered");
    }
    
    private void createTopic(String brokerAddress) {
        try {
            Properties config = new Properties();
            config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
            AdminClient admin = AdminClient.create(config);
            try {
                admin.createTopics(Collections.singleton(
                    new NewTopic(TEST_TOPIC, 1, (short) 1))).all().get();
                Thread.sleep(500);
            } catch (Exception e) {
                // Topic might exist
            } finally {
                admin.close();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not pre-create topic: " + e.getMessage());
        }
    }
}
