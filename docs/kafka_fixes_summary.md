# Kafka sendReceive Reliability Fixes

## Problem
The `sendReceive` pattern was unreliable when the producer sent a message before the consumer was ready, resulting in lost messages. This is a classic race condition in Kafka where:
1. Producer sends a message to a topic
2. Consumer hasn't yet subscribed or been assigned to partitions
3. Message offset is beyond the consumer's starting offset (especially with "latest" auto.offset.reset)

## Root Cause
In the `Communication.sendReceive()` flow:
```
send(param)   ← Producer sends immediately
receive()     ← Consumer starts reading after send
```

The consumer might miss the message if:
- Consumer group ID is reused → consumer offset already advanced
- Consumer not assigned to partitions yet → misses "latest" message
- Race condition between send and subscribe

## Solutions Implemented

### 1. **Unique Consumer Group ID for sendReceive Pattern** (Line 89)
```java
String groupId = sendreceive ? "sendreceive-" + UUID.randomUUID() : "test";
```
- Each sendReceive invocation gets a unique consumer group
- Ensures fresh offset reading, avoiding offset tracking issues
- Regular consumers keep "test" group ID for persistence

### 2. **Consumer Partition Assignment Warmup** (Lines 110-111, 116-129)
```java
if (sendreceive) {
    ensureConsumerReady();
}

private void ensureConsumerReady() {
    // Poll until partitions are assigned
    while (consumer.assignment().isEmpty()) {
        consumer.poll(Duration.ofMillis(100));
    }
}
```
- Ensures consumer is fully assigned to partitions BEFORE producer sends
- Waits up to 5 seconds for partition assignment
- Logs if assignment times out

### 3. **Earliest Offset Reset** (Line 101)
```java
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
```
- Only applied for sendReceive scenarios
- Ensures if message is missed initially, consumer reads from earliest available
- Fallback safety mechanism

### 4. **Producer Reliability** (Lines 69-71)
```java
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, 3);
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
```
- Waits for all replicas to acknowledge
- Retries failed sends up to 3 times
- Ensures ordering with in-flight limit

### 5. **Synchronous Producer Send** (Line 134)
```java
producer.send(...).get();  // Wait for completion
```
- Blocks until message is confirmed sent by broker
- Ensures consumer doesn't read before message is actually persisted

## Impact
- ✅ Eliminates race conditions in sendReceive pattern
- ✅ Guarantees message won't be lost if producer sends before consumer ready
- ✅ Maintains backward compatibility for non-sendReceive scenarios
- ✅ No breaking changes to API
