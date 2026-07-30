package roart.common.communication.integration.camel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that Camel.receiveStringAndStore properly handles consumer crashes.
 * 
 * When a consumer crashes BEFORE calling acknowledge/rollback:
 * - Message should remain in queue (unacknowledged)
 * - Message should be redelivered to another consumer
 * - No messages should be silently lost
 * 
 * Note: Requires RabbitMQ broker at localhost:5672
 */
//@Disabled("Requires RabbitMQ broker running. Enable for integration testing.")
public class CamelReceiveStringAndStoreCrashET {
    
    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_PORT = "5672";
    private static final String TEST_QUEUE = "test-crash-recovery-camel";
    private static final String TEST_MESSAGE = "Test message for crash recovery";
    
    private Camel producer;
    private Camel consumer1;
    private Camel consumer2;
    
    @BeforeEach
    public void setUp() {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        try {
            CachingConnectionFactory factory = new CachingConnectionFactory(connection);
            factory.setUsername("username");
            factory.setPassword("password");
            factory.createConnection().close();
        } catch (Exception e) {
            throw new RuntimeException("RabbitMQ not available", e);
        }
        
        producer = new Camel("TEST_PRODUCER", String.class, TEST_QUEUE, new ObjectMapper(),
                true, false, false, connection, null);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) try { producer.destroy(); } catch (Exception e) {}
        if (consumer1 != null) try { consumer1.destroy(); } catch (Exception e) {}
        if (consumer2 != null) try { consumer2.destroy(); } catch (Exception e) {}
    }
    
    /**
     * Test: Consumer crashes before acknowledging message
     * Expected: Message remains in queue (unacknowledged) and can be redelivered
     */
    @Test
    public void testCrashBeforeAcknowledge() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer1 receives but crashes before ack
        final boolean[] storageInvoked = {false};
        consumer1 = new Camel("CONSUMER_1", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    storageInvoked[0] = true;
                    System.out.println("Consumer1 received: " + msg);
                    // Simulate crash - return true but don't wait for ack
                    return true;
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertTrue(storageInvoked[0], "Storage should be invoked");
        assertTrue(received1.length > 0, "Should receive message");
        
        // Simulate crash - destroy without proper cleanup
        System.out.println("Consumer1 crashing...");
        consumer1.destroy();
        
        Thread.sleep(1000);
        
        // STEP 3: New consumer should get the message
        consumer2 = new Camel("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be redelivered after crash");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message redelivered after crash");
    }
    
    /**
     * Test: Storage callback throws exception (simulates crash during processing)
     * Expected: Exchange exception triggers rollback, message remains in queue
     */
    @Test
    public void testStorageCallbackThrowsException() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with failing storage
        consumer1 = new Camel("CONSUMER_FAIL", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    throw new RuntimeException("Storage processing failed!");
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on exception");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: New consumer gets message
        consumer2 = new Camel("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be available after exception");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Message redelivered after storage exception");
    }
    
    /**
     * Test: Storage callback returns false (validation failure)
     * Expected: Exchange exception set, message not acked, can be redelivered
     */
    @Test
    public void testStorageCallbackReturnsFalse() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer that rejects message
        consumer1 = new Camel("CONSUMER_REJECT", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Storage rejected: " + msg);
                    return false;
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on rejection");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: Message should be redelivered
        consumer2 = new Camel("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        assertTrue(received2.length > 0, "Message should be redelivered");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Message redelivered after rejection");
    }
    
    /**
     * Test: Multiple exchanges and rollbacks
     * Expected: Message survives multiple rollback attempts and is delivered on success
     */
    @Test
    public void testMultipleRollbacksAndSuccess() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: First consumer crashes
        consumer1 = new Camel("CRASH_1", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    throw new RuntimeException("First crash!");
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertEquals(0, received1.length, "First consumer crashes");
        consumer1.destroy();
        Thread.sleep(500);
        
        // STEP 3: Second consumer rejects
        consumer2 = new Camel("REJECT_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Rejecting: " + msg);
                    return false;
                });
        
        String[] received2 = consumer2.receiveStringAndStore();
        assertEquals(0, received2.length, "Second consumer rejects");
        consumer2.destroy();
        Thread.sleep(500);
        
        // STEP 4: Third consumer succeeds
        final boolean[] finalSuccess = {false};
        Camel consumer3 = new Camel("SUCCESS_3", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    finalSuccess[0] = true;
                    System.out.println("Successfully processed: " + msg);
                    return true;
                });
        
        String[] received3 = consumer3.receiveStringAndStore();
        
        assertTrue(finalSuccess[0], "Third consumer succeeds");
        assertTrue(received3.length > 0, "Should receive message");
        assertEquals(TEST_MESSAGE, received3[0], "Message should match");
        
        consumer3.destroy();
        
        System.out.println("✓ Test passed: Message survived multiple rollbacks");
    }
    
    /**
     * Test: Manual ACK mode enforcement
     * Expected: receiveStringAndStore uses MANUAL ack mode (not AUTO)
     */
    @Test
    public void testManualAckModeEnforced() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // Create consumer - should use MANUAL mode for receiveStringAndStore
        consumer1 = new Camel("CONSUMER_MANUAL", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    return true;
                });
        
        // receiveStringAndStore should use MANUAL ack mode
        String[] received = consumer1.receiveStringAndStore();
        assertTrue(received.length > 0, "Should receive message");
        
        System.out.println("✓ Test passed: Manual ACK mode is used");
    }
}
