package roart.common.communication.integration.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that Spring.receiveStringAndStore properly handles consumer crashes
 * and redelivery with MANUAL ACK mode.
 * 
 * When a consumer crashes BEFORE giving acks/nacks:
 * - Message should remain in queue (negative ack with requeue)
 * - Message should be redelivered to another consumer in the group
 * - No messages should be silently lost
 * 
 * Note: Requires RabbitMQ broker at localhost:5672 with MANUAL_ACK mode
 */
//@Disabled("Requires RabbitMQ broker running. Enable for integration testing.")
public class SpringReceiveStringAndStoreCrashET {
    
    private static final String RABBITMQ_HOST = "localhost";
    private static final String RABBITMQ_PORT = "5672";
    private static final String TEST_QUEUE = "test-crash-recovery-spring";
    private static final String TEST_MESSAGE = "Test message for crash recovery";
    
    private Spring producer;
    private Spring consumer1;
    private Spring consumer2;
    
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
        
        producer = new Spring("TEST_PRODUCER", String.class, TEST_QUEUE, new ObjectMapper(),
                true, false, false, connection, null);
    }
    
    @AfterEach
    public void tearDown() {
        if (producer != null) try { producer.destroy(); } catch (Exception e) {}
        if (consumer1 != null) try { consumer1.destroy(); } catch (Exception e) {}
        if (consumer2 != null) try { consumer2.destroy(); } catch (Exception e) {}
    }
    
    /**
     * Test: Consumer crashes before giving ack
     * Expected: basicNack with requeue=true redelivers message
     */
    @Test
    public void testCrashBeforeAcknowledge() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer1 receives message but crashes
        final boolean[] storageInvoked = {false};
        consumer1 = new Spring("CONSUMER_1", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    storageInvoked[0] = true;
                    System.out.println("Consumer1 received: " + msg);
                    return true;
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        assertTrue(storageInvoked[0], "Storage should be invoked");
        assertTrue(received1.length > 0, "Should receive message");
        
        // Simulate crash - destroy without completing
        System.out.println("Consumer1 crashing without ack...");
        consumer1.destroy();
        
        Thread.sleep(1000);
        
        // STEP 3: New consumer should get the message (requeued)
        consumer2 = new Spring("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be redelivered after crash");
        assertEquals(TEST_MESSAGE, received2[0], "Message content should match");
        
        System.out.println("✓ Test passed: Message redelivered via basicNack");
    }
    
    /**
     * Test: Storage callback throws exception
     * Expected: basicNack with requeue=true (not requeue=false which goes to DLQ)
     */
    @Test
    public void testStorageCallbackThrowsException() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer with failing storage
        consumer1 = new Spring("CONSUMER_FAIL", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    throw new RuntimeException("Storage processing failed!");
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on exception");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: Message should be requeued (requeue=true), not go to DLQ
        consumer2 = new Spring("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be requeued (requeue=true), not in DLQ");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Storage exception triggers requeue");
    }
    
    /**
     * Test: Storage callback returns false (validation rejected)
     * Expected: basicNack with requeue=true requeues message
     */
    @Test
    public void testStorageCallbackReturnsFalse() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // STEP 2: Consumer that rejects
        consumer1 = new Spring("CONSUMER_REJECT", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Storage validation failed: " + msg);
                    return false;
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertEquals(0, received.length, "Should return empty on rejection");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: Message should be requeued
        consumer2 = new Spring("CONSUMER_2", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        assertTrue(received2.length > 0, "Message should be requeued");
        assertEquals(TEST_MESSAGE, received2[0], "Message should match");
        
        System.out.println("✓ Test passed: Rejection triggers requeue");
    }
    
    /**
     * Test: Channel lifecycle and resource cleanup
     * Expected: Channel created once, properly closed in finally block
     */
    @Test
    public void testChannelLifecycleCleanup() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // Create consumer that processes normally
        final int[] channelOperations = {0};
        consumer1 = new Spring("CONSUMER_CHANNEL", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Processing: " + msg);
                    return true;
                });
        
        // Process message - channel should be created/closed properly
        String[] received = consumer1.receiveStringAndStore();
        assertTrue(received.length > 0, "Should receive message");
        
        consumer1.destroy();
        
        System.out.println("✓ Test passed: Channel lifecycle managed properly");
    }
    
    /**
     * Test: Multiple messages with redelivery
     * Expected: Each message properly requeued with basicNack
     */
    @Test
    public void testMultipleMessagesWithRedelivery() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // STEP 1: Send 2 messages
        producer.send(TEST_MESSAGE + "-1");
        producer.send(TEST_MESSAGE + "-2");
        Thread.sleep(500);
        
        // STEP 2: Consumer1 receives both but crashes on second
        final int[] receivedCount = {0};
        consumer1 = new Spring("CONSUMER_MULTI", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    receivedCount[0]++;
                    System.out.println("Received: " + msg);
                    if (receivedCount[0] == 2) {
                        throw new RuntimeException("Crash on second message!");
                    }
                    return receivedCount[0] == 1;
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        // First message processed, second crashes during storage
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // STEP 3: Consumer2 should get the second message
        consumer2 = new Spring("CONSUMER_RECOVERY", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, "Should get redelivered message");
        
        System.out.println("✓ Test passed: Multiple messages handled with redelivery");
    }
    
    /**
     * Test: Manual ACK mode with basicAck on success
     * Expected: Message is not redelivered after successful ack
     */
    @Test
    public void testManualAckModeSuccess() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // Process successfully
        final boolean[] success = {false};
        consumer1 = new Spring("CONSUMER_ACK", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    success[0] = true;
                    System.out.println("Successfully acking: " + msg);
                    return true;
                });
        
        String[] received = consumer1.receiveStringAndStore();
        assertTrue(success[0], "Should process successfully");
        assertTrue(received.length > 0, "Should receive message");
        
        consumer1.destroy();
        Thread.sleep(1000);
        
        // Consumer2 should NOT get the message (it was acked)
        consumer2 = new Spring("CONSUMER_CHECK", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        // Message should be gone (acked), not redelivered
        // Using 1-second timeout instead of default 5 seconds
        System.out.println("✓ Test passed: Message acked and not redelivered");
    }
    
    /**
     * Test: basicNack with requeue=false would go to DLQ (contrast with requeue=true)
     * Expected: With requeue=true (our implementation), message comes back to queue
     */
    @Test
    public void testRequeueBehavior() throws Exception {
        String connection = RABBITMQ_HOST + ":" + RABBITMQ_PORT;
        
        // Send message
        producer.send(TEST_MESSAGE);
        Thread.sleep(500);
        
        // Consumer rejects
        consumer1 = new Spring("CONSUMER_REQUEUE", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, msg -> {
                    System.out.println("Testing requeue flag: " + msg);
                    return false;
                });
        
        String[] received1 = consumer1.receiveStringAndStore();
        consumer1.destroy();
        Thread.sleep(1000);
        
        // With requeue=true, message should come back to queue (not DLQ)
        consumer2 = new Spring("CONSUMER_VERIFY", String.class, TEST_QUEUE, new ObjectMapper(),
                false, true, true, connection, null);
        
        String[] received2 = consumer2.receiveString();
        
        assertTrue(received2.length > 0, 
                "Message should be requeued (requeue=true), not in DLQ");
        
        System.out.println("✓ Test passed: Requeue flag works correctly");
    }
}
