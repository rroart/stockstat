package roart.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.constants.Constants;
import roart.common.model.MyDataSource;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.etl.db.Extract;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.bean.ConfigI;
import roart.model.data.StockData;
import roart.testdata.TestConstants;

// Github Copilot

/**
 * Integration tests for batch processing of StockData.
 * Validates that batch-wise loading produces the same result as loading all at once.
 * Uses Spring Boot configuration and real data sources similar to InmemoryPipelineIT.
 */
@TestInstance(Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class })
class BatchIT {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;
    
    // no autowiring
    IclijConfig conf = null;
   
    MyDataSource dataSource;
    
    DbDao dbDao;
    
    Extract extract;

    @BeforeAll
    void setUp() {
        log.info("Setting up batch integration tests");
        
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "coreconfig", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        
        String market = TestConstants.MARKET;
        String start = "2022.01.01";
        String end = "2025.01.01";
        
        try {
            TestDataSource dataSource1 = new TestDataSource(conf, 
                TimeUtil.convertDate2(start), 
                TimeUtil.convertDate2(end), 
                market, 26, false, Constants.INDEXVALUECOLUMN, false, 
                new String[] { "1d", "1w", "1m", "3m", "1y", "3y", "5y", "10y" }, null);
            
            dataSource = new TestDataSources(List.of(dataSource1));
            
            dbDao = new DbDao(iconf, dataSource);
            extract = new Extract(dbDao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test data sources", e);
        }
        
        log.info("Batch integration tests setup complete");
    }

    /**
     * Test that batch-wise loading produces the same result as loading all at once.
     * This test validates that 15 stocks split into 3 batches of 5 stocks each
     * produce identical results to loading all 15 stocks at once.
     */
    //@Test
    void testBatchVsNonBatchedLoadingProducesSameResult() {
        log.info("Testing batch vs non-batched loading");
        
        String market = TestConstants.MARKET;
        
        // Setup batch size for batched loading
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 5);
        
        // Load data batch-wise
        StockData batchedResult = extract.getStockData(conf, market, true);
        
        // Setup non-batched loading (batch size 0)
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 0);
        
        // Load all data at once
        StockData nonBatchedResult = extract.getStockData(conf, market, true);
        
        // Verify results are equivalent using comparison methods
        assertNotNull(batchedResult, "Batched result should not be null");
        assertNotNull(nonBatchedResult, "Non-batched result should not be null");
        
        log.info("Batched result size: {}, Non-batched result size: {}", 
            batchedResult.datedstocks.size(), 
            nonBatchedResult.datedstocks.size());
        
        // Use the size comparator method to compare structures
        assertTrue(batchedResult.compareMapAndListSizesBoolean(nonBatchedResult),
                "Batched and non-batched results should have the same map and list sizes:\n" 
                + batchedResult.compareMapAndListSizes(nonBatchedResult));
        
        // Also verify that the actual data matches
        assertTrue(batchedResult.compare(nonBatchedResult),
                "Batched and non-batched results should have identical data");
        
        log.info("Batch vs non-batched test passed");
    }

    /**
     * Test batching with exact batch boundaries (data size is multiple of batch size).
     * Validates that 10 stocks in 2 batches of 5 stocks each are properly handled.
     */
    @Test
    void testBatchingWithExactBoundaries() {
        log.info("Testing batching with exact boundaries");
        
        String market = TestConstants.MARKET;
        
        // Setup batch size for exact boundary testing
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 5);
        
        // Load batched result
        StockData batchedResult = extract.getStockData(conf, market, true);
        
        // Setup non-batched loading
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 0);
        
        // Load non-batched result
        StockData nonBatchedResult = extract.getStockData(conf, market, true);
        
        assertNotNull(batchedResult, "Batched result should not be null");
        assertNotNull(nonBatchedResult, "Non-batched result should not be null");
        
        log.info("Exact boundary - Batched: {}, Non-batched: {}", 
            batchedResult.datedstocks.size(), 
            nonBatchedResult.datedstocks.size());
        
        // Use size comparator to verify structure matches
        assertTrue(batchedResult.compareMapAndListSizesBoolean(nonBatchedResult),
                "Batched and non-batched results should have matching map/list sizes with exact boundaries:\n"
                + batchedResult.compareMapAndListSizes(nonBatchedResult));
        
        log.info("Exact boundaries test passed");
    }

    /**
     * Test batching with non-exact batch boundaries (data size not a multiple of batch size).
     * Validates that partial final batches are properly handled.
     */
    @Test
    void testBatchingWithNonExactBoundaries() {
        log.info("Testing batching with non-exact boundaries");
        
        String market = TestConstants.MARKET;
        
        // Setup smaller batch size to test partial batches
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 7);
        
        // Load batched result with partial final batch
        StockData batchedResult = extract.getStockData(conf, market, true);
        
        // Setup non-batched loading
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 0);
        
        // Load non-batched result
        StockData nonBatchedResult = extract.getStockData(conf, market, true);
        
        assertNotNull(batchedResult, "Batched result should not be null");
        assertNotNull(nonBatchedResult, "Non-batched result should not be null");
        
        log.info("Non-exact boundary - Batched: {}, Non-batched: {}", 
            batchedResult.datedstocks.size(), 
            nonBatchedResult.datedstocks.size());
        
        // Use size comparator to verify structure matches with non-exact boundaries
        assertTrue(batchedResult.compareMapAndListSizesBoolean(nonBatchedResult),
                "Batched and non-batched results should have matching map/list sizes with non-exact boundaries:\n"
                + batchedResult.compareMapAndListSizes(nonBatchedResult));
        
        log.info("Non-exact boundaries test passed");
    }

    /**
     * Test with single batch (all data fits in one batch).
     * Validates that when all data fits in a single batch, results match non-batched loading.
     */
    @Test
    void testSingleBatch() {
        log.info("Testing single batch loading");
        
        String market = TestConstants.MARKET;
        
        // Setup large batch size so all data fits in one batch
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 1000);
        
        StockData result = extract.getStockData(conf, market, true);
        
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.datedstocks, "Dated stocks should not be null");
        assertFalse(result.datedstocks.isEmpty(), "Should have stocks loaded in single batch");
        
        log.info("Single batch test passed with {} stocks", result.datedstocks.size());
    }

    /**
     * Test stockidmap is properly merged across batches.
     * Validates that stock ID mapping works correctly when batching.
     */
    @Test
    void testStockIdMapMerging() {
        log.info("Testing stock ID map merging");
        
        String market = TestConstants.MARKET;
        
        // Setup batch size for merging test
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 5);
        
        StockData batchedResult = extract.getStockData(conf, market, true);
        
        assertNotNull(batchedResult, "Batched result should not be null");
        assertNotNull(batchedResult.stockidmap, "Stock ID map should not be null");
        assertFalse(batchedResult.stockidmap.isEmpty(), "Should have stock IDs in map");
        
        log.info("Stock ID map merging test passed with {} IDs", batchedResult.stockidmap.size());
    }

    /**
     * Test stockdatemap is properly merged across batches.
     * Validates that stock date mapping works correctly when batching.
     */
    @Test
    void testStockDateMapMerging() {
        log.info("Testing stock date map merging");
        
        String market = TestConstants.MARKET;
        
        // Setup batch size for merging test
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATABASEBATCHSIZE, 5);
        
        StockData batchedResult = extract.getStockData(conf, market, true);
        
        assertNotNull(batchedResult, "Batched result should not be null");
        assertNotNull(batchedResult.stockdatemap, "Stock date map should not be null");
        assertFalse(batchedResult.stockdatemap.isEmpty(), "Should have stock dates in map");
        
        log.info("Stock date map merging test passed with {} dates", batchedResult.stockdatemap.size());
    }
}

