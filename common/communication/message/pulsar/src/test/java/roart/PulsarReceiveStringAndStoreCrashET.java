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
 * Tests to verify that Pulsar.receiveStringAndStore properly handles consumer crashes.
 * 
 * When a consumer crashes BEFORE calling acknowledge/negativeAcknowledge:
 * - Message should remain in queue
 * - Message should be redelivered to another consumer
 * - No messages should be silently lost
 * 
 * Note: Requires Pulsar broker at pulsar://localhost:6650
 */
//@Disabled("Requires Pulsar broker running. Enable for integration testing.")
public class PulsarReceiveStringAndStoreCrashET {
    
    private static final String PULSAR_SERVICE_URL = "pulsar://localhost:6650";
    private static final String TEST_TOPIC = "persistent://public/default/test-crash-recovery";
    private static final String TEST_MESSAGE = "Test message for crash recovery";
    
    private Pulsar producer;
    private Pulsar consumer1;
    private Pulsar consumer2;
    
    @BeforeEach
    public void setUp() {
        try {
            PulsarClient testClient = PulsarClient.builder()
                    .serviceUrl(PULSAR_SERVICE_URL)
                    .build();
            testClient.close();
        } catch (PulsarClientException e) {
            throw new RuntimeException("Pulsar broker not available", e);
        }
        
        producer = new Pulsar("TEST_PRODUCER", String.class, TEST_TOPIC, new ObjectMapper(),
                true, false, false, PULSAR_SERVICE_URL, null);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) try { producer.destroy(); } catch (Exception e) {}
        if (consumer1 != null) try { consumer1.destroy(); } catch (Exception e) {}
        if (consumer2 != null) try { consumer2.destroy(); } catch (Exception e) {}
    }
    
    /**
     * Test: Consumer crashes before acknowledging message
     * Expected: Message remains in queue and is redelivered
     */
    @Test
    public void testCrashBeforeAcknowledge() throws Exception {
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer1 receives but doesn't ack
        final boolean[] storageInvoked = {false};
        consumer1 = new Pulsar("CONSUMER_1", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    storageInvoked[0] = true;
                    System.out.println("Consumer1 received and stored: " + msg);
                    return true;
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertTrue(storageInvoked[0], "Storage should be invoked");
        assertTrue(received1.length > 0, "Should receive message");
        
        // Simulate crash - destroy without proper ack
        System.out.println("Consumer1 crashing...");
        consumer1.destroy();
        
        Thread.sleep(1000); // Wait for broker to handle crash
        
        // STEP 3: New consumer gets the message (not acked)
        consumer2 = new Pulsar("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be redelivered after consumer crash");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message redelivered after crash");
    }
    
    /**
     * Test: Storage callback throws exception
     * Expected: Message is negatively acknowledged and can be redelivered
     */
    @Test
    public void testStorageCallbackThrowsException() throws Exception {
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with failing storage
        consumer1 = new Pulsar("CONSUMER_FAIL", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    throw new RuntimeException("Storage failed!");
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on storage exception");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: New consumer should get message
        consumer2 = new Pulsar("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        String[] received2 = consumer2.receiveString();
        assertTrue(received2.length > 0, "Message should be available for retry");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Message redelivered after storage exception");
    }
    
    /**
     * Test: Storage callback returns false
     * Expected: Message is negatively acknowledged and redelivered
     */
    @Test
    public void testStorageCallbackReturnsFalse() throws Exception {
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with storage that rejects
        consumer1 = new Pulsar("CONSUMER_REJECT", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    System.out.println("Storage rejected: " + msg);
                    return false;
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on rejection");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: Message should be redelivered
        consumer2 = new Pulsar("CONSUMER_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        String[] received2 = consumer2.receiveString();
        assertTrue(received2.length > 0, "Message should be redelivered");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Message redelivered after rejection");
    }
    
    /**
     * Test: Timeout on receive with no messages
     * Expected: Returns empty without hanging
     */
    @Test
    public void testTimeoutWithNoMessages() throws Exception {
        consumer1 = new Pulsar("CONSUMER_TIMEOUT", String.class, TEST_TOPIC + "-empty",
                new ObjectMapper(), false, true, true, PULSAR_SERVICE_URL, msg -> true);
        
        long startTime = System.currentTimeMillis();
        String[] received = consumer1.receiveStringAndStore();
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertEquals(0, received.length, "Should return empty on timeout");
        assertTrue(elapsed < 10000, "Should timeout quickly");
        
        System.out.println("✓ Test passed: Timeout handled correctly (" + elapsed + "ms)");
    }
    
    /**
     * Test: Multiple consumer crashes with eventual success
     * Expected: Message persists through multiple failures and is delivered on success
     */
    @Test
    public void testMultipleCrashesEventualSuccess() throws Exception {
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: First consumer crashes
        consumer1 = new Pulsar("CRASH_1", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    throw new RuntimeException("Crash!");
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertEquals(0, received1.length, "First consumer returns empty on crash");
        consumer1.destroy();
        Thread.sleep(500);
        
        // STEP 3: Second consumer rejects
        consumer2 = new Pulsar("REJECT_2", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> false);
        
        String[] received2 = consumer2.receiveStringAndStore();
        assertEquals(0, received2.length, "Second consumer rejects");
        consumer2.destroy();
        Thread.sleep(500);
        
        // STEP 4: Third consumer succeeds
        final boolean[] finalSuccess = {false};
        Pulsar consumer3 = new Pulsar("SUCCESS_3", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> {
                    finalSuccess[0] = true;
                    return true;
                });
        
        String[] received3 = consumer3.receiveStringAndStore();
        
        assertTrue(finalSuccess[0], "Third consumer processes successfully");
        assertTrue(received3.length > 0, "Should receive message");
        assertEquals(TEST_MESSAGE, received3[0], "Message should match");
        
        consumer3.destroy();
        
        System.out.println("✓ Test passed: Message survived multiple crashes");
    }
    
    /**
     * Test: Negative acknowledge behavior
     * Expected: negativeAcknowledge triggers redelivery
     */
    @Test
    public void testNegativeAcknowledgeBehavior() throws Exception {
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer1 processes and rejects (nack sent in receiveStringAndStore)
        consumer1 = new Pulsar("CONSUMER_NACK", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, msg -> false); // Will be nacked
        
        String[] received1 = consumer1.receiveStringAndStore();
        consumer1.destroy();
        
        Thread.sleep(1000); // Wait for redelivery
        
        // STEP 3: Consumer2 should get the same message
        consumer2 = new Pulsar("CONSUMER_RETRY", String.class, TEST_TOPIC, new ObjectMapper(),
                false, true, true, PULSAR_SERVICE_URL, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, "Message should be redelivered via nack");
        assertEquals(TEST_MESSAGE, received2[0], "Content should match");
        
        System.out.println("✓ Test passed: Negative acknowledge triggers redelivery");
    }
}
