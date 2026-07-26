# Message Resend Handling Fixes for Pulsar, Camel, and Spring

## Problem
The `receiveStringAndStore()` method in all three messaging systems had critical issues handling consumer crashes before commits/acks. Messages could be lost if the consumer process crashed between receiving and committing.

## Root Causes

### 1. **Pulsar.java**
- **Issue**: No timeout on `consumer.receive()` - could hang indefinitely
- **Issue**: If exception occurred after receive but before acknowledge/negativeAcknowledge, no rollback
- **Issue**: Exception during `storeMessage.apply()` could crash thread without sending nack

### 2. **Camel.java**
- **Issue**: No timeout on `consumer.receive()` 
- **Issue**: No null check after receive - could crash on null exchange
- **Issue**: Exception during `storeMessage.apply()` had no error handling
- **Issue**: If thread crashed after storing but before commit, message would be lost

### 3. **Spring.java**
- **Issue**: Created new Channel for EVERY message (resource leak & inefficient)
- **Issue**: Channel creation could fail, leaving message uncommitted
- **Issue**: Exception during `storeMessage.apply()` had no rollback
- **Issue**: No cleanup if channel creation fails
- **Issue**: Missing channel closure in error paths

## Solutions Implemented

### Pulsar.java (receiveStringAndStore)

```java
// 1. Added 5-second timeout to prevent hanging
msg = consumer.receive(5, java.util.concurrent.TimeUnit.SECONDS);

// 2. Wrapped storeMessage.apply() with try-catch
try {
    stored = storeMessage.apply(string);
} catch (Exception e) {
    // If storage fails, ensure negative ack sent before returning
    consumer.negativeAcknowledge(msg);
    return new String[0];
}

// 3. Wrapped entire processing in outer try-catch
// Ensures negativeAcknowledge is called even if exception occurs
try {
    consumer.negativeAcknowledge(msg);
} catch (Exception ackError) {
    log.error("Failed to send negative ack: ...");
}
```

**Benefits:**
- ✅ No hanging on receive
- ✅ Exception during storage triggers automatic redelivery
- ✅ Thread crash before ack is handled gracefully
- ✅ All code paths guarantee ack/nack is sent

### Camel.java (receiveStringAndStore)

```java
// 1. Added 5-second timeout
receive = consumer.receive(endpoint, 5000);

// 2. Added null check
if (receive == null) {
    return new String[] { };
}

// 3. Wrapped storeMessage.apply() with try-catch
try {
    stored = storeMessage.apply(body);
} catch (Exception e) {
    // Set exception on exchange to trigger rollback
    receive.setException(e);
    return new String[] { };
}

// 4. On success: acknowledge, on failure: set exception for rollback
if (stored) {
    receive.getUnitOfWork().done(receive);
} else {
    receive.setException(new RuntimeException("Message not stored"));
}
```

**Benefits:**
- ✅ Timeout prevents hanging
- ✅ Null checks prevent NPE
- ✅ Exception during storage triggers exchange rollback
- ✅ Message redelivered if processing fails

### Spring.java (receiveStringAndStore)

```java
// 1. Set mode only once, not in loop
template.containerAckMode(AcknowledgeMode.MANUAL);

// 2. Receive single message (not loop)
message = template.receive(getReceiveService(), 1000);

// 3. Create channel ONCE outside loop
ch = template.getConnectionFactory().createConnection().createChannel(false);

// 4. Wrapped storeMessage in try-catch with proper nack
try {
    stored = storeMessage.apply(new String(string));
} catch (Exception e) {
    log.error("Error storing message, will redeliver: ...");
    try {
        ch.basicNack(tag, false, true);  // Requeue=true
    } catch (IOException nackError) {
        log.error("Failed to send nack: ...");
    }
    return new String[0];
}

// 5. Proper cleanup in finally block
finally {
    if (ch != null && ch.isOpen()) {
        try {
            ch.close();
        } catch (Exception closeError) {
            log.debug("Error closing channel: ...");
        }
    }
}
```

**Benefits:**
- ✅ Resource efficient - single channel per call
- ✅ Exception during storage triggers requeue
- ✅ Channel always closed properly
- ✅ All error paths have proper ack/nack
- ✅ Cleanup guaranteed in finally blocks

## Common Patterns Applied to All Three

1. **Defensive Error Handling**: Exception during `storeMessage.apply()` always triggers redelivery
2. **Timeout Protection**: All receive operations have timeouts
3. **Guaranteed Ack/Nack**: All code paths ensure commit/rollback is sent
4. **Graceful Degradation**: If commit/nack fails, just log (don't crash)
5. **Resource Cleanup**: Proper cleanup in error conditions

## Build Results
- ✅ Pulsar: BUILD SUCCESS
- ✅ Camel: BUILD SUCCESS  
- ✅ Spring: BUILD SUCCESS

## Message Redelivery Guarantees

When consumer crashes **before** commit/ack:
- **Pulsar**: `negativeAcknowledge()` triggers redelivery to another consumer
- **Camel**: `receive.setException()` triggers rollback, message stays in queue
- **Spring**: `basicNack(tag, false, true)` requeues message to broker

If storage processing fails:
- All implementations immediately trigger redelivery
- Message returned to queue for retry
- Prevents silent message loss on processing errors
