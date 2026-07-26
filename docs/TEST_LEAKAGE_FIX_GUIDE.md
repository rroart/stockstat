# Test Leakage - Fix Implementation Guide

## Overview
This document provides specific fixes for test leakage issues in both `InmemoryPipelineBatchIT` and `InmemoryPipelineIT`.

---

## Fix 1: Add @AfterEach Cleanup Method

### For InmemoryPipelineBatchIT.java

**Add after the `@BeforeAll` before() method (around line 177):**

```java
@AfterEach
public void afterEach() throws Exception {
    // Reset configuration to default state
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);

    // Clean cache and pipeline
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();

    // Verify complete cleanup
    assertEquals(true, inmemory.isEmpty(),
        "Inmemory cache must be empty after test - test left leakage");
}
```

### For InmemoryPipelineIT.java

**Add after @BeforeAll before() method (around line 177):**

```java
@AfterEach
public void afterEach() throws Exception {
    // Clean cache and pipeline
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();

    // Verify complete cleanup
    assertEquals(true, inmemory.isEmpty(),
        "Inmemory cache must be empty after test - test left leakage");
}
```

---

## Fix 2: Remove Redundant Cleanup from Individual Tests

### Tests to Simplify in InmemoryPipelineIT

Remove redundant cleanup code since `@AfterEach` will handle it:

**In `testImproveSim()` (lines 497-501):**
```java
// REMOVE THIS:
assertEquals(false, inmemory.isEmpty());
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();
assertEquals(true, inmemory.isEmpty());

// KEEP ONLY THIS:
assertEquals(false, inmemory.isEmpty());
// @AfterEach will handle the cleanup
```

**In `testImproveSimTwice()` (lines 523-527):**
```java
// REMOVE THIS:
assertEquals(false, inmemory.isEmpty());
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();
assertEquals(true, inmemory.isEmpty());

// KEEP ONLY THIS:
assertEquals(false, inmemory.isEmpty());
// @AfterEach will handle the cleanup
```

**In `testImproveAutoSim()` (lines 544-548):**
```java
// REMOVE THIS:
assertEquals(false, inmemory.isEmpty());
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();
assertEquals(true, inmemory.isEmpty());

// KEEP ONLY THIS:
assertEquals(false, inmemory.isEmpty());
// @AfterEach will handle the cleanup
```

---

## Fix 3: InmemoryPipelineBatchIT - Remove Redundant Cleanup

### In testIndicatorAggregatorGetMapMapBatchedNonBatchedConsistency() (lines 206-213)

**Before:**
```java
inmemory.stat();

// Clear and run test with batching
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();

iconf.getConfigData().getConfigValueMap().put(
    IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
```

**After:**
```java
inmemory.stat();

// Clear and run test with batching
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();

iconf.getConfigData().getConfigValueMap().put(
    IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 5);
// @AfterEach will handle final cleanup
```

---

## Fix 4: Improve Consistency in Assertions

### Add Missing @BeforeEach to Reset State

**Add to both test classes before @Test methods:**

```java
@BeforeEach
public void beforeEach() throws Exception {
    // Ensure clean state before each test
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();
    assertEquals(true, inmemory.isEmpty(),
        "Test must start with clean inmemory state");
}
```

---

## Fix 5: Handle DbDao State in InmemoryPipelineIT

### Improve testSimEvent() (lines 351-374)

**Add try-finally to ensure restoration:**

```java
DbDao origDbDao = io.getDbDao();
try {
    DbDao dbDao = new DbDao(iconf, periodDataSources[2][1]); // stock 2 day 1
    io.setDbDao(dbDao);

    IclijServiceResult result = null;
    try {
        simConfig.setBuyweight(false);
        result = testutils.getSimulateInvestMarket(simConfig, market);
    } catch (Exception e) {
        log.error(Constants.EXCEPTION, e);
    }
    // ... rest of test
} finally {
    io.setDbDao(origDbDao);  // Guaranteed restoration
}
```

---

## Fix 6: Static ActionThread State Management

### For Both Classes

Add method to clear ActionThread queues:

```java
@BeforeEach
protected void clearActionThreadQueues() {
    // If ActionThread.queue and ActionThread.queued are static collections:
    try {
        // Clear static state if needed
        // ActionThread.queue.clear();
        // ActionThread.queued.clear();
    } catch (Exception e) {
        log.warn("Could not clear ActionThread static state", e);
    }
}
```

---

## Complete Proposed Changes

### For InmemoryPipelineBatchIT.java

1. Add after line 177 (after @BeforeAll method):
```java
@BeforeEach
public void beforeEach() throws Exception {
    // Ensure clean state before each test
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();
    assertEquals(true, inmemory.isEmpty(),
        "Test must start with clean inmemory state");
}

@AfterEach
public void afterEach() throws Exception {
    // Reset configuration to default state
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);

    // Clean cache and pipeline
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();

    // Verify complete cleanup
    assertEquals(true, inmemory.isEmpty(),
        "Inmemory cache must be empty after test - test left leakage");
}
```

2. Add imports if needed:
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
```

### For InmemoryPipelineIT.java

1. Add after line 177 (after @BeforeAll method):
```java
@BeforeEach
public void beforeEach() throws Exception {
    // Ensure clean state before each test
    inmemory.stat();
    assertEquals(true, inmemory.isEmpty(),
        "Test must start with clean inmemory state");
}

@AfterEach
public void afterEach() throws Exception {
    // Clean cache and pipeline
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();

    // Verify complete cleanup
    assertEquals(true, inmemory.isEmpty(),
        "Inmemory cache must be empty after test - test left leakage");
}
```

2. Simplify testImproveSim() - Remove lines 497-501
3. Simplify testImproveSimTwice() - Remove lines 524-527
4. Simplify testImproveAutoSim() - Remove lines 545-548

5. Add imports if needed:
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
```

---

## Testing the Fix

After implementing these changes:

1. Run tests in different orders to verify independence:
   ```bash
   # Run in forward order
   mvn test -Dtest=InmemoryPipelineBatchIT#testBatchedPipelineSmallBatchSize,InmemoryPipelineBatchIT#testBatchedPipelineLargeBatchSize

   # Run in reverse order
   mvn test -Dtest=InmemoryPipelineBatchIT#testBatchedPipelineLargeBatchSize,InmemoryPipelineBatchIT#testBatchedPipelineSmallBatchSize
   ```

2. Run individual tests to verify they pass in isolation
3. Run full suite to verify no conflicts

---

## Verification Checklist

- [ ] No test configuration changes persist after test completion
- [ ] Inmemory cache starts empty at beginning of each test
- [ ] Inmemory cache ends empty at completion of each test
- [ ] Mock state is reset between tests
- [ ] IO state (DbDao, etc.) is restored
- [ ] ActionThread queues are cleared
- [ ] Tests pass independently and in any order
- [ ] Tests pass when run multiple times in succession

---

*Implementation Priority: HIGH - These are critical test quality issues*

