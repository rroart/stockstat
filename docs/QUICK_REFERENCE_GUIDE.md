# Quick Reference Guide - Running Tests After Leakage Fixes

## What Was Fixed?

✅ **InmemoryPipelineBatchIT.java**
- Added @BeforeEach to ensure clean state before each test
- Added @AfterEach to ensure cleanup and verify empty inmemory cache
- Removed redundant cleanup code

✅ **InmemoryPipelineIT.java**
- Added @BeforeEach to ensure clean state before each test
- Added @AfterEach to ensure cleanup and verify empty inmemory cache
- Simplified 3 test methods by removing redundant cleanup
- Fixed testSimEvent() with try-finally for guaranteed DbDao restoration

---

## How to Run Tests

### Run Everything
```bash
cd /home/roart/src/stockstatspark
mvn test -Dtest=InmemoryPipelineBatchIT,InmemoryPipelineIT
```

### Run Batch Tests Only
```bash
mvn test -Dtest=InmemoryPipelineBatchIT
```

### Run Pipeline Tests Only
```bash
mvn test -Dtest=InmemoryPipelineIT
```

### Run Single Test
```bash
mvn test -Dtest=InmemoryPipelineBatchIT#testBatchedPipelineSmallBatchSize
```

### Run Multiple Specific Tests
```bash
mvn test -Dtest=InmemoryPipelineBatchIT#testBatchedPipelineSmallBatchSize,InmemoryPipelineBatchIT#testBatchedPipelineLargeBatchSize
```

### Run Tests Multiple Times (Verify No Flakiness)
```bash
for i in {1..3}; do
  echo "Run $i"
  mvn test -Dtest=InmemoryPipelineIT
  if [ $? -ne 0 ]; then
    echo "FAILED on run $i"
    break
  fi
done
```

### Run with Detailed Output
```bash
mvn test -Dtest=InmemoryPipelineBatchIT -X
```

### Skip Tests (if needed)
```bash
mvn clean package -DskipTests
```

---

## Key Changes Summary

### Before Fixes
```
Test A sets batch size to 2
Test B runs expecting default, gets 2 ❌ TEST LEAKAGE
Test C sets cache value
Test D runs and finds leftover cache ❌ TEST DEPENDENCY
```

### After Fixes
```
@BeforeEach: Clear state
Test A sets batch size to 2
@AfterEach: Reset batch size, clear cache
@BeforeEach: Clear state
Test B runs expecting default, gets default ✅ INDEPENDENT
@AfterEach: Reset batch size, clear cache
... (continues cleanly)
```

---

## Expected Output

### Successful Test Run
```
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
```

### What to Look For
✅ All tests pass
✅ No "test left leakage" assertions fail
✅ No "test must start with clean inmemory state" assertions fail
✅ Tests pass in any order
✅ Tests pass when run individually
✅ Tests pass when run multiple times

### If Tests Fail
Common issues and solutions:

**1. "Inmemory cache must be empty after test" fails**
- Cause: A test didn't finish cleanup
- Fix: Check if test threw uncaught exception
- Location: Look at stack trace for which test failed

**2. "Test must start with clean inmemory state" fails**
- Cause: Previous test didn't clean up properly
- Fix: Run the failing test individually to isolate
- Check: Look at @AfterEach for the previous test

**3. Tests pass individually but fail together**
- Cause: Test order dependency
- Fix: This indicates test leakage - report details
- Debug: Add logging in @BeforeEach/@AfterEach

---

## Verification Script

Run this script to verify all fixes work correctly:

```bash
#!/bin/bash

echo "=== Test Leakage Fix Verification Script ==="
echo ""

# Get the project root
PROJECT_ROOT="/home/roart/src/stockstatspark"
cd "$PROJECT_ROOT"

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASS_COUNT=0
FAIL_COUNT=0

# Test 1: Run both test classes
echo -e "${YELLOW}Test 1: Running both test classes...${NC}"
if mvn test -Dtest=InmemoryPipelineBatchIT,InmemoryPipelineIT -q; then
    echo -e "${GREEN}✓ PASS: Both test classes ran successfully${NC}"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗ FAIL: Tests failed${NC}"
    ((FAIL_COUNT++))
fi
echo ""

# Test 2: Run in different order
echo -e "${YELLOW}Test 2: Running tests in different order...${NC}"
if mvn test -Dtest=InmemoryPipelineIT,InmemoryPipelineBatchIT -q; then
    echo -e "${GREEN}✓ PASS: Tests passed in reverse order${NC}"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗ FAIL: Tests failed in reverse order${NC}"
    ((FAIL_COUNT++))
fi
echo ""

# Test 3: Run individual batch test
echo -e "${YELLOW}Test 3: Running individual batch test...${NC}"
if mvn test -Dtest=InmemoryPipelineBatchIT#testBatchedPipelineSmallBatchSize -q; then
    echo -e "${GREEN}✓ PASS: Individual batch test passed${NC}"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗ FAIL: Individual batch test failed${NC}"
    ((FAIL_COUNT++))
fi
echo ""

# Test 4: Run individual pipeline test
echo -e "${YELLOW}Test 4: Running individual pipeline test...${NC}"
if mvn test -Dtest=InmemoryPipelineIT#testSim -q; then
    echo -e "${GREEN}✓ PASS: Individual pipeline test passed${NC}"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗ FAIL: Individual pipeline test failed${NC}"
    ((FAIL_COUNT++))
fi
echo ""

# Summary
echo -e "${YELLOW}=== Summary ===${NC}"
echo -e "Passed: ${GREEN}$PASS_COUNT${NC}"
echo -e "Failed: ${RED}$FAIL_COUNT${NC}"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ All verification tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some verification tests failed!${NC}"
    exit 1
fi
```

Save as `verify_test_fixes.sh` and run:
```bash
chmod +x verify_test_fixes.sh
./verify_test_fixes.sh
```

---

## Documentation Files Generated

1. **TEST_LEAKAGE_ANALYSIS.md**
   Detailed analysis of all test leakage issues found

2. **TEST_LEAKAGE_FIX_GUIDE.md**
   Step-by-step implementation guide with code examples

3. **TEST_LEAKAGE_FIXES_SUMMARY.md**
   Complete summary of all changes made

4. **QUICK_REFERENCE_GUIDE.md** (this file)
   Quick commands and verification steps

---

## Next Steps

### Immediate
1. ✅ Review the fixes (already done)
2. ⏳ Run tests locally
3. ⏳ Verify all tests pass

### Short Term
4. ⏳ Run tests multiple times to confirm stability
5. ⏳ Check CI/CD pipeline integration
6. ⏳ Update documentation if needed

### Future
7. Consider using `@TestInstance(Lifecycle.PER_METHOD)` for even better isolation
8. Add test categorization (unit vs integration)
9. Implement automated test order randomization

---

## Common Issues & Solutions

### Issue: Tests timeout after changes
**Solution**: Cleanup methods add minimal overhead - verify hardware resources

### Issue: One specific test always fails
**Solution**: Run it individually - if passes alone, it has dependency on another test

### Issue: Different results in IDE vs command line
**Solution**: Likely class loader or artifact caching issue - do: `mvn clean`

### Issue: "Cannot find symbol: testutils"
**Solution**: Rebuild project - do: `mvn clean compile`

---

## Support

If tests still fail after applying these fixes:

1. **Check test names**: Verify names match exactly
2. **Check working directory**: Must be `/home/roart/src/stockstatspark`
3. **Check Maven version**: Use `mvn -v` to verify Maven is installed
4. **Check compilation**: Run `mvn clean compile` first
5. **Review logs**: Check `target/surefire-reports/` for details

---

**Last Updated**: April 28, 2026
**Status**: Ready for Testing ✅

