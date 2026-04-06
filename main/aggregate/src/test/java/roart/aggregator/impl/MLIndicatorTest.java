package roart.aggregator.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import roart.iclij.config.IclijConfig;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.ml.NeuralNetCommand;
import roart.common.inmemory.model.Inmemory;
import roart.common.config.ConfigData;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.dao.MLClassifyDao;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.Mockito.*;

// gemini

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class MLIndicatorTest {

    @Autowired
    IclijConfig conf;

    @MockitoBean
    SerialPipeline mockSerialPipeline;
    @MockitoBean
    NeuralNetCommand mockNeuralNetCommand;
    @MockitoBean
    Inmemory mockInmemory;

    // Helper method to create an MLIndicator instance with mocked dependencies
    private MLIndicator createMLIndicator(IclijConfig config, String key, String title, int category) throws Exception {
        List<String> stockDates = new ArrayList<>(); // Empty list for now
        return new MLIndicator(config, key, title, category, mockSerialPipeline, mockNeuralNetCommand, stockDates, mockInmemory);
    }

    @Test
    public void testCreateLabelMapShort_binary() throws Exception {
        // Configure mock IclijConfig for binary mode
        when(conf.wantUseBinary()).thenReturn(true);

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<Double, String> labelMap = mlIndicator.createLabelMapShort();

        assertNotNull(labelMap);
        assertEquals(2, labelMap.size());
        assertEquals("ABOVE", labelMap.get(1.0));
        assertEquals("BELOW", labelMap.get(0.0));
    }

    @Test
    public void testCreateLabelMapShort_nonBinary() throws Exception {
        // Configure mock IclijConfig for non-binary mode
        when(conf.wantUseBinary()).thenReturn(false);

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<Double, String> labelMap = mlIndicator.createLabelMapShort();

        assertNotNull(labelMap);
        assertEquals(2, labelMap.size());
        assertEquals("ABOVE", labelMap.get(1.0));
        assertEquals("BELOW", labelMap.get(2.0)); // Assuming cats is 2, as it's a private static final int
    }

    @Test
    public void testGetCat_binary() throws Exception {
        when(conf.wantUseBinary()).thenReturn(true);
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);

        // Test cases for binary mode
        assertEquals(1.0, mlIndicator.getCat(0.02, 0.01), 0.001); // change > threshold
        assertEquals(0.0, mlIndicator.getCat(0.005, 0.01), 0.001); // change <= threshold
        assertEquals(0.0, mlIndicator.getCat(0.01, 0.01), 0.001); // change == threshold
    }

    @Test
    public void testGetCat_nonBinary_cats2() throws Exception {
        when(conf.wantUseBinary()).thenReturn(false);
        // Assuming 'cats' is 2 (private static final int) and 'interval' is 0.01 (private static final double)
        // Logic: if (change > threshold + interval * (cat - 1 - halfcat))
        // For cats = 2, halfcat = 1.
        // So, if (change > threshold + 0.01 * (cat - 2))
        // If cat = 2: if (change > threshold + 0.01 * 0) => if (change > threshold) return 2 + 1 - 2 = 1.0
        // If cat = 1: (loop condition cat > 1 is false)
        // If no condition met, return cats (2.0)

        // This means if change > threshold, it returns 1.0. Otherwise, it returns 2.0.
        // This effectively behaves like a binary classification (1.0 for above, 2.0 for below) when cats = 2.

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);

        assertEquals(1.0, mlIndicator.getCat(0.02, 0.01), 0.001); // change > threshold
        assertEquals(2.0, mlIndicator.getCat(0.005, 0.01), 0.001); // change <= threshold
        assertEquals(2.0, mlIndicator.getCat(0.01, 0.01), 0.001); // change == threshold
    }

    @Test
    public void testAnythingHereA_withData() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, Double[][]> listMap = new HashMap<>();
        listMap.put("id1", new Double[][]{{1.0, 2.0, null}, {4.0, 5.0, 6.0}});
        listMap.put("id2", new Double[][]{{null, null, 3.0}}); // last element not null
        listMap.put("id3", new Double[][]{{7.0, null, null}}); // first element not null

        // Use reflection to call the private method anythingHereA
        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHereA", Map.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testAnythingHereA_emptyMap() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, Double[][]> listMap = new HashMap<>();

        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHereA", Map.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testAnythingHereA_allNulls() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, Double[][]> listMap = new HashMap<>();
        listMap.put("id1", new Double[][]{{null, null, null}});
        listMap.put("id2", new Double[][]{{null, null, null}, {null, null, null}});

        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHereA", Map.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testAnythingHere_withData() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, List<List<Double>>> listMap = new HashMap<>();
        listMap.put("id1", Arrays.asList(Arrays.asList(1.0, 2.0, null)));
        listMap.put("id2", Arrays.asList(Arrays.asList(null, null, 3.0)));

        // Use reflection to call the protected method anythingHere
        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHere", Map.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testAnythingHere_emptyMap() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, List<List<Double>>> listMap = new HashMap<>();

        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHere", Map.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testAnythingHere_allNulls() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Map<String, List<List<Double>>> listMap = new HashMap<>();
        listMap.put("id1", Arrays.asList(Arrays.asList(null, null, null)));

        java.lang.reflect.Method method = MLIndicator.class.getDeclaredMethod("anythingHere", Map.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(mlIndicator, listMap));
    }

    @Test
    public void testMapAdder() throws Exception {
        Map<MLClassifyModel, Long> map = new HashMap<>();
        MLClassifyModel model1 = mock(MLClassifyModel.class);
        MLClassifyModel model2 = mock(MLClassifyModel.class);

        // Add to empty map
        MLIndicator.mapAdder(map, model1, 10L);
        assertEquals(10L, map.get(model1));

        // Add to existing entry
        MLIndicator.mapAdder(map, model1, 5L);
        assertEquals(15L, map.get(model1));

        // Add new entry
        MLIndicator.mapAdder(map, model2, 20L);
        assertEquals(20L, map.get(model2));
        assertEquals(15L, map.get(model1)); // Ensure other entries are untouched
    }

    @Test
    public void testGetThresholds_singleThreshold() throws Exception {
        // Mock ConfigData and its map
        ConfigData mockConfigData = mock(ConfigData.class);
        Map<String, Object> configValueMap = new HashMap<>();
        when(mockConfigData.getConfigValueMap()).thenReturn(configValueMap);
        when(conf.getConfigData()).thenReturn(mockConfigData);

        // Case 1: MISCTHRESHOLD is null, getAggregatorsIndicatorThreshold returns a single value
        when(conf.getAggregatorsIndicatorThreshold()).thenReturn("0.05");

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Double[] thresholds = mlIndicator.getThresholds(conf);

        assertNotNull(thresholds);
        assertEquals(1, thresholds.length);
        assertEquals(0.05, thresholds[0], 0.001);
    }

    @Test
    public void testGetThresholds_multipleThresholds() throws Exception {
        ConfigData mockConfigData = mock(ConfigData.class);
        Map<String, Object> configValueMap = new HashMap<>();
        when(mockConfigData.getConfigValueMap()).thenReturn(configValueMap);
        when(conf.getConfigData()).thenReturn(mockConfigData);

        // Case 2: getAggregatorsIndicatorThreshold returns a JSON array
        when(conf.getAggregatorsIndicatorThreshold()).thenReturn("[0.01, 0.02, 0.03]");

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Double[] thresholds = mlIndicator.getThresholds(conf);

        assertNotNull(thresholds);
        assertEquals(3, thresholds.length);
        assertEquals(0.01, thresholds[0], 0.001);
        assertEquals(0.02, thresholds[1], 0.001);
        assertEquals(0.03, thresholds[2], 0.001);
    }

    @Test
    public void testGetThresholds_guiThreshold() throws Exception {
        ConfigData mockConfigData = mock(ConfigData.class);
        Map<String, Object> configValueMap = new HashMap<>();
        configValueMap.put(ConfigConstants.MISCTHRESHOLD, "true"); // Simulate GUI threshold
        when(mockConfigData.getConfigValueMap()).thenReturn(configValueMap);
        when(conf.getConfigData()).thenReturn(mockConfigData);

        // GUI threshold should override aggregatorsIndicatorThreshold
        when(conf.getThreshold()).thenReturn("0.1");
        when(conf.getAggregatorsIndicatorThreshold()).thenReturn("[0.01, 0.02]"); // This should be ignored

        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);
        Double[] thresholds = mlIndicator.getThresholds(conf);

        assertNotNull(thresholds);
        assertEquals(1, thresholds.length);
        assertEquals(0.1, thresholds[0], 0.001);
    }

    @Test
    public void testGetFilenamePart() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);

        // Mock IndicatorUtils.dummyfactory
        try (var mockedStatic = mockStatic(IndicatorUtils.class)) {
            AbstractIndicator mockIndicator1 = mock(AbstractIndicator.class);
            when(mockIndicator1.getName()).thenReturn("MACD");
            when(mockIndicator1.wantForExtras()).thenReturn(true);

            AbstractIndicator mockIndicator2 = mock(AbstractIndicator.class);
            when(mockIndicator2.getName()).thenReturn("RSI");
            when(mockIndicator2.wantForExtras()).thenReturn(false);

            mockedStatic.when(() -> IndicatorUtils.dummyfactory(any(IclijConfig.class), eq("indicator1"))).thenReturn(mockIndicator1);
            mockedStatic.when(() -> IndicatorUtils.dummyfactory(any(IclijConfig.class), eq("indicator2"))).thenReturn(mockIndicator2);

            List<String> indicators = Arrays.asList("indicator1", "indicator2");
            String filenamePart = mlIndicator.getFilenamePart(indicators);

            assertEquals("MACD_d_RSI_", filenamePart);
        }
    }

    @Test
    public void testGetFilename() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);

        // Mock dependencies for getFilename
        MLClassifyDao mockDao = mock(MLClassifyDao.class);
        when(mockDao.getName()).thenReturn("SparkML");

        MLClassifyModel mockModel = mock(MLClassifyModel.class);
        when(mockModel.getName()).thenReturn("LogisticRegression");

        ConfigData mockConfigData = mock(ConfigData.class);
        when(mockConfigData.getMlmarket()).thenReturn(null); // No specific ML market
        when(mockConfigData.getMarket()).thenReturn("NASDAQ"); // Default market
        when(conf.getConfigData()).thenReturn(mockConfigData);
        when(conf.getAggregatorsIndicatorFuturedays()).thenReturn(5);

        List<String> indicators = Arrays.asList("indicator1", "indicator2");
        Double threshold = 0.02;

        // Mock IndicatorUtils.dummyfactory for getFilenamePart
        try (var mockedStatic = mockStatic(IndicatorUtils.class)) {
            AbstractIndicator mockIndicator1 = mock(AbstractIndicator.class);
            when(mockIndicator1.getName()).thenReturn("MACD");
            when(mockIndicator1.wantForExtras()).thenReturn(true);

            AbstractIndicator mockIndicator2 = mock(AbstractIndicator.class);
            when(mockIndicator2.getName()).thenReturn("RSI");
            when(mockIndicator2.wantForExtras()).thenReturn(false);

            mockedStatic.when(() -> IndicatorUtils.dummyfactory(any(IclijConfig.class), eq("indicator1"))).thenReturn(mockIndicator1);
            mockedStatic.when(() -> IndicatorUtils.dummyfactory(any(IclijConfig.class), eq("indicator2"))).thenReturn(mockIndicator2);

            String filename = mlIndicator.getFilename(mockDao, mockModel, "input_dim", "output_dim", "NASDAQ", indicators, threshold);

            String expectedFilenamePart = "MACD_d_RSI_";
            String expectedFilename = "NASDAQ_" + mlIndicator.getName() + "_" + mockDao.getName() + "_" + mockModel.getName() + "_" + expectedFilenamePart + "5_" + threshold + "_" + "input_dim" + "_" + "output_dim";
            assertEquals(expectedFilename, filename);
        }
    }

    @Test
    public void testGetFilename_withMlMarket() throws Exception {
        MLIndicator mlIndicator = createMLIndicator(conf, "testKey", "testTitle", 0);

        MLClassifyDao mockDao = mock(MLClassifyDao.class);
        when(mockDao.getName()).thenReturn("TensorFlow");

        MLClassifyModel mockModel = mock(MLClassifyModel.class);
        when(mockModel.getName()).thenReturn("NeuralNet");

        ConfigData mockConfigData = mock(ConfigData.class);
        when(mockConfigData.getMlmarket()).thenReturn("CRYPTO"); // Specific ML market
        when(mockConfigData.getMarket()).thenReturn("NASDAQ"); // Should be overridden
        when(conf.getConfigData()).thenReturn(mockConfigData);
        when(conf.getAggregatorsIndicatorFuturedays()).thenReturn(10);

        List<String> indicators = Arrays.asList("indicatorA");
        Double threshold = 0.05;

        try (var mockedStatic = mockStatic(IndicatorUtils.class)) {
            AbstractIndicator mockIndicatorA = mock(AbstractIndicator.class);
            when(mockIndicatorA.getName()).thenReturn("CCI");
            when(mockIndicatorA.wantForExtras()).thenReturn(false);
            mockedStatic.when(() -> IndicatorUtils.dummyfactory(any(IclijConfig.class), eq("indicatorA"))).thenReturn(mockIndicatorA);

            String filename = mlIndicator.getFilename(mockDao, mockModel, "in", "out", "NASDAQ", indicators, threshold);

            String expectedFilenamePart = "CCI_";
            String expectedFilename = "CRYPTO_" + mlIndicator.getName() + "_" + mockDao.getName() + "_" + mockModel.getName() + "_" + expectedFilenamePart + "10_" + threshold + "_" + "in" + "_" + "out";
            assertEquals(expectedFilename, filename);
        }
    }
}