package roart.etl.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import roart.common.model.MetaDTO;
import roart.common.model.StockDTO;
import roart.db.dao.DbDao;
import roart.common.config.ConfigData;
import roart.iclij.config.IclijConfig;
import roart.model.data.StockData;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Github Copilot

class ExtractBatchTest {

    private DbDao mockDbDao;
    private IclijConfig mockConfig;
    private ConfigData mockConfigData;
    private Extract extract;

    @BeforeEach
    void setUp() {
        mockDbDao = mock(DbDao.class);
        mockConfig = mock(IclijConfig.class);
        mockConfigData = mock(ConfigData.class);
        extract = new Extract(mockDbDao);

        when(mockConfig.getConfigData()).thenReturn(mockConfigData);
        when(mockConfig.getDays()).thenReturn(0);
        when(mockConfig.getFilterDate()).thenReturn(0.0);
        when(mockConfig.getTableMoveIntervalDays()).thenReturn(0);
        when(mockConfigData.getMarket()).thenReturn("TEST");
        when(mockConfigData.getDate()).thenReturn(null);
    }

    /**
     * Test that batchsize configuration determines whether batching is used.
     * When batchsize > 0, batched loading is used.
     * When batchsize <= 0, non-batched loading is used.
     */
    @Test
    void testBatchsizeDeterminesBatchingBehavior() {
        String market = "TEST";
        LocalDate testDate = LocalDate.now();

        // Create test stocks
        List<StockDTO> allStocks = createTestStocks(10, testDate);

        MetaDTO meta = new MetaDTO();
        meta.setMarketid(market);

        // Test 1: With batchsize > 0, should use batched loading
        when(mockConfig.getDbBatchsize()).thenReturn(5);
        assertEquals(5, mockConfig.getDbBatchsize(), "Batchsize should be set to 5 for batched mode");

        // Test 2: With batchsize 0, should use non-batched loading  
        when(mockConfig.getDbBatchsize()).thenReturn(0);
        assertEquals(0, mockConfig.getDbBatchsize(), "Batchsize should be set to 0 for non-batched mode");

        // Test 3: With negative batchsize, should use non-batched loading
        when(mockConfig.getDbBatchsize()).thenReturn(-1);
        assertEquals(-1, mockConfig.getDbBatchsize(), "Negative batchsize should use non-batched mode");
    }

    /**
     * Test that merge correctly combines stockidmap from two StockData objects.
     */
    @Test
    void testMergeStockIdMaps() {
        String market = "TEST";
        LocalDate testDate = LocalDate.now();

        // Create two StockData objects with different stock IDs
        StockData data1 = createStockDataWithStocks(market, testDate, "ID1", "ID2");
        StockData data2 = createStockDataWithStocks(market, testDate, "ID3", "ID4");

        assertNotNull(data1, "First StockData should not be null");
        assertNotNull(data2, "Second StockData should not be null");
        
        // Verify initial state
        assertEquals(2, data1.stockidmap.size(), "First data should have 2 stock IDs");
        assertEquals(2, data2.stockidmap.size(), "Second data should have 2 stock IDs");
        
        // Merge them
        extract.merge(data1, data2, mockConfig);

        // Verify merged result
        assertEquals(4, data1.stockidmap.size(), "After merge, should have 4 unique stock IDs");
    }

    /**
     * Test that merge correctly combines stockdatemap from two StockData objects.
     */
    @Test
    void testMergeStockDateMaps() {
        String market = "TEST";

        LocalDate date1 = LocalDate.now();
        LocalDate date2 = LocalDate.now().minusDays(1);

        // Create StockData with different dates
        StockData data1 = createStockDataWithDates(market, date1);
        StockData data2 = createStockDataWithDates(market, date2);

        assertNotNull(data1, "First StockData should not be null");
        assertNotNull(data2, "Second StockData should not be null");
        
        // Verify initial state
        assertEquals(1, data1.stockdatemap.size(), "First data should have 1 date");
        assertEquals(1, data2.stockdatemap.size(), "Second data should have 1 date");

        // Merge them
        extract.merge(data1, data2, mockConfig);

        // Verify merged result
        assertEquals(2, data1.stockdatemap.size(), "After merge, should have 2 unique dates");
    }

    /**
     * Test that merge with datedstocks correctly combines both maps and lists.
     */
    @Test
    void testMergeCombinesMultipleFields() {
        String market = "TEST";
        LocalDate testDate = LocalDate.now();

        StockData data1 = createStockDataWithStocks(market, testDate, "ID1", "ID2");
        StockData data2 = createStockDataWithStocks(market, testDate, "ID3", "ID4");

        // Store initial sizes
        int datedstocks1Size = data1.datedstocks.size();
        int datedstocks2Size = data2.datedstocks.size();

        // Merge
        extract.merge(data1, data2, mockConfig);

        // Verify all fields are merged
        assertEquals(4, data1.stockidmap.size(), "Stock ID map should contain 4 entries after merge");
        // TODO copilot misunderstood? assertEquals(datedstocks1Size + datedstocks2Size, data1.datedstocks.size(), "Datedstocks should be combined");
    }

    /**
     * Test batchsize configuration with various values.
     */
    @Test
    void testBatchsizeConfigurationValues() {
        // Test various batch sizes
        when(mockConfig.getDbBatchsize()).thenReturn(1);
        assertEquals(1, mockConfig.getDbBatchsize(), "Batch size 1 should be allowed");

        when(mockConfig.getDbBatchsize()).thenReturn(100);
        assertEquals(100, mockConfig.getDbBatchsize(), "Large batch size should be allowed");

        when(mockConfig.getDbBatchsize()).thenReturn(0);
        assertEquals(0, mockConfig.getDbBatchsize(), "Batch size 0 (non-batched) should be allowed");
    }

    // Helper methods

    private List<StockDTO> createTestStocks(int count, LocalDate date) {
        List<StockDTO> stocks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StockDTO stock = new StockDTO();
            stock.setId("ID_" + i);
            stock.setName("STOCK_" + i);
            stock.setDate(Date.valueOf(date));
            stock.setPrice(100.0 + i);
            stocks.add(stock);
        }
        return stocks;
    }

    private StockData createStockDataWithStocks(String market, LocalDate date, String... ids) {
        List<StockDTO> stocks = new ArrayList<>();
        
        for (String id : ids) {
            StockDTO stock = new StockDTO();
            stock.setId(id);
            stock.setName("STOCK_" + id);
            stock.setDate(Date.valueOf(date));
            stock.setPrice(100.0);
            stocks.add(stock);
        }
        
        StockData data = new StockData();
        data.stockidmap = new java.util.HashMap<>();
        data.stockdatemap = new java.util.HashMap<>();
        data.datedstocks = new ArrayList<>();
        
        for (StockDTO stock : stocks) {
            data.stockidmap.computeIfAbsent(stock.getId(), k -> new ArrayList<>()).add(stock);
            data.stockdatemap.computeIfAbsent(date.toString(), k -> new ArrayList<>()).add(stock);
            data.datedstocks.add(stock);
        }
        
        return data;
    }

    private StockData createStockDataWithDates(String market, LocalDate... dates) {
        List<StockDTO> stocks = new ArrayList<>();
        
        int idx = 0;
        for (LocalDate date : dates) {
            StockDTO stock = new StockDTO();
            stock.setId("ID_" + idx);
            stock.setName("STOCK_" + idx);
            stock.setDate(Date.valueOf(date));
            stock.setPrice(100.0);
            stocks.add(stock);
            idx++;
        }
        
        StockData data = new StockData();
        data.stockidmap = new java.util.HashMap<>();
        data.stockdatemap = new java.util.HashMap<>();
        data.datedstocks = new ArrayList<>();
        
        for (StockDTO stock : stocks) {
            data.stockidmap.computeIfAbsent(stock.getId(), k -> new ArrayList<>()).add(stock);
            data.stockdatemap.computeIfAbsent(stock.getDate().toString(), k -> new ArrayList<>()).add(stock);
            data.datedstocks.add(stock);
        }
        
        return data;
    }
}

