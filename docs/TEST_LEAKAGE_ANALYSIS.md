# Test Leakage Analysis Report

## Summary
Both `InmemoryPipelineBatchIT` and `InmemoryPipelineIT` test classes contain significant test leakage issues due to shared state not being properly cleaned between tests. The classes use `@TestInstance(Lifecycle.PER_CLASS)` which causes all tests to run with the same instance, making state persistence inevitable without proper cleanup.

---

## Critical Issues Found

### 1. **Configuration State Not Reset Between Tests**

#### Location: `InmemoryPipelineBatchIT`

**Problem**: Multiple tests modify `MISCINMEMORYPIPELINEBATCHSIZE` configuration but never reset it to the original value.

**Affected Tests**:
- `testBatchedPipelineSmallBatchSize()` - sets to 2
- `testBatchedPipelineLargeBatchSize()` - sets to 100
- `testMachineLearningBatched()` - sets to 5
- `testFindProfitBatched()` - sets to 5
- `testSimBatched()` - sets to 5
- `testEvolveBatched()` - sets to 5
- `testImproveProfitBatched()` - sets to 5
- `testIndicatorAggregatorGetMapMapBatchedNonBatchedConsistency()` - sets to 0, then 5
- `testIndicatorAggregatorGetMapMapDirectComparisonReal()` - sets to 0, then 5

**Impact**: Tests run in different order could produce different results depending on what batch size was set by a previous test.

**Example**:
```java
// testBatchedPipelineSmallBatchSize() sets batch size to 2
iconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 2);
// Never resets it! So next test runs with batch size 2 instead of expected value
```

---

### 2. **Inmemory Cache State Not Consistently Cleaned**

#### Location: Both `InmemoryPipelineBatchIT` and `InmemoryPipelineIT`

**Problem**: Most tests don't clean up inmemory cache after execution. Only a few tests explicitly call cleanup:
- `testIndicatorAggregatorGetMapMapBatchedNonBatchedConsistency()`
- `testIndicatorAggregatorGetMapMapDirectComparisonReal()`

**Tests Without Cleanup (`InmemoryPipelineBatchIT`):**
- `testBatchedPipelineSmallBatchSize()` - No cleanup
- `testBatchedPipelineLargeBatchSize()` - No cleanup
- `testMachineLearningBatched()` - No cleanup
- `testFindProfitBatched()` - No cleanup
- `testSimBatched()` - No cleanup
- `testEvolveBatched()` - No cleanup
- `testImproveProfitBatched()` - No cleanup only calls `inmemory.stat()`

**Tests Without Proper Cleanup (`InmemoryPipelineIT`):**
- `testMachineLearning()` - No cleanup
- `testFindProfit()` - No cleanup
- `testFindProfitARI()` - No cleanup
- `testFindProfitPredictor()` - No cleanup
- `testEvolve()` - No cleanup
- `testEvolveARI()` - No cleanup
- `testImproveProfit()` - No cleanup
- `testImproveProfitMLI()` - No cleanup
- `testCrosstest()` - No cleanup
- `testFilter()` - No cleanup
- `testAboveBelow()` - No cleanup
- `testSim()` - No cleanup
- `testSimEvent()` - Partial cleanup (only restores DbDao)
- `testSimWithDbid()` - No cleanup
- `testSimWithDbidAndMod()` - No cleanup
- `testSimWithDbidAndModMethod()` - No cleanup
- `testSimRun()` - No cleanup
- `testAutoSim()` - No cleanup

**Expected Pattern** (used in only 3 tests):
```java
testutils.cacheinvalidate();
testutils.deletepipeline(ControlService.id);
inmemory.stat();
assertEquals(true, inmemory.isEmpty());
```

---

### 3. **Inconsistent Assertions for State Verification**

#### Location: Both test classes

**Problem**: Not all tests verify that the inmemory state is empty after completion.

**Tests Missing Verification (`InmemoryPipelineIT`)**:
- `testFindProfit()` - calls `inmemory.stat()` but no assertion
- `testEvolveARI()` - calls `inmemory.stat()` but no assertion
- `testImproveSimEvent()` - No final state check
- And many others

**Current Pattern** (Incomplete):
```java
inmemory.stat();
// Missing: assertEquals(true, inmemory.isEmpty());
```

---

### 4. **Static Shared State (ActionThread)**

#### Location: Both tests reference `ActionThread.queue` and `ActionThread.queued`

**Problem**: ActionThread maintains static queue and queued collections that are shared across all tests.

**Lines with Static State Access**:
- `InmemoryPipelineIT.java:335` - `ActionThread.queue.size()` and `ActionThread.queued.size()`
- `InmemoryPipelineIT.java:363` - Same
- `InmemoryPipelineIT.java:394` - Same
- `InmemoryPipelineIT.java:417` - Same
- Multiple references in `testSimBatched()`, `testSim()` and other tests

**Risk**: Queue state from one test could affect the behavior or timing of subsequent tests.

---

### 5. **IO and Mock State Issues**

#### Location: `InmemoryPipelineIT.testSimEvent()`

**Problem** (Partial):
```java
DbDao origDbDao = io.getDbDao();
DbDao dbDao = new DbDao(iconf, periodDataSources[2][1]);
io.setDbDao(dbDao);  // Modifies shared IO instance

// Tests run...

io.setDbDao(origDbDao);  // Restores only if no exception
```

**Risk**: If an exception occurs before restoration, subsequent tests will use the wrong DbDao.

---

### 6. **Mock Configuration Not Reset**

#### Location: Both `@BeforeAll` methods

**Problem**: Mock objects are set up once and reused for all tests. Mock expectations or return values modified during tests are not reset.

**Example from `testSimWithDbid()`**:
```java
SimDataDTO simData = iclijDbDao.getAllSimData(market, null, null).get(0);
String dbid = "" + simData.getDbid();
doReturn(simData).when(iclijDbDao).getSimData(dbid);  // Sets up mock
// This mock configuration persists for all subsequent tests
```

---

### 7. **No AfterEach or AfterAll Cleanup**

#### Location: Both test classes

**Missing**: Neither class has any `@AfterEach` or `@AfterAll` methods to ensure cleanup.

**Should Have**:
```java
@AfterEach
public void afterEach() throws Exception {
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();
    // Reset configuration to default
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);
}
```

---

## Specific Test Execution Order Dependency Issues

### Scenario 1: Configuration State Leakage
1. `testBatchedPipelineSmallBatchSize()` sets batch size to 2
2. `testBatchedPipelineLargeBatchSize()` runs next expecting default batch size but gets 2
3. Test may pass/fail depending on execution order

### Scenario 2: Cache Pollution
1. `testMachineLearning()` populates inmemory cache with ML data
2. `testFindProfit()` runs next and tries to populate cache
3. Old data from previous test may interfere with results

### Scenario 3: Mock State Pollution
1. `testSimWithDbid()` sets up a mock return value for `getSimData(dbid)`
2. `testSimWithDbidAndMod()` runs next and calls same method
3. Previous mock configuration is still active, affecting results

---

## Recommendations

### Immediate Actions:

1. **Add `@AfterEach` cleanup method** to both test classes
2. **Reset configuration values** after each test
3. **Add consistent assertions** to verify clean state
4. **Rename tests** to run in alphabetical order to avoid dependency on execution order (tests should be independent)

### Code Changes Needed:

```java
@AfterEach
public void afterEach() throws Exception {
    // Reset configuration
    iconf.getConfigData().getConfigValueMap().put(
        IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, 0);

    // Clean cache and pipeline
    testutils.cacheinvalidate();
    testutils.deletepipeline(ControlService.id);
    inmemory.stat();

    // Verify cleanup
    assertTrue(inmemory.isEmpty(), "Inmemory must be empty after test");
}
```

### Additional Safeguards:

4. **Use `@BeforeEach`** to reset state explicitly
5. **Reset ActionThread queues** if they're static
6. **Reset all mock configurations** between tests
7. **Consider using `@TestInstance(Lifecycle.PER_METHOD)`** instead of `PER_CLASS`
8. **Separate test concerns** - unit tests vs integration tests

---

## Summary of Affected Files

- `/home/roart/src/stockstatspark/iclij/iclij-core/src/test/java/roart/controller/InmemoryPipelineBatchIT.java`
- `/home/roart/src/stockstatspark/iclij/iclij-core/src/test/java/roart/controller/InmemoryPipelineIT.java`

## Total Test Leakage Instances: 40+

---

*Generated: April 28, 2026*

