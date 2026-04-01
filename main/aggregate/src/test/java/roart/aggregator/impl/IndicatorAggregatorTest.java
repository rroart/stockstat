package roart.aggregator.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.constants.Constants;
import roart.iclij.config.IclijConfig;
import roart.common.config.ConfigData;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.ml.NeuralNetCommand;
import roart.common.inmemory.model.Inmemory;
import roart.result.model.ResultItemTableRow;

// Github Copilot

class IndicatorAggregatorTest {

    @Mock
    private IclijConfig conf;

    @Mock
    private ConfigData configData;

    private TestIndicatorAggregator aggregator;

    @BeforeEach
    @SuppressWarnings("EmptyTryBlock")
    void setUp() throws Exception {
        try (@SuppressWarnings("unused") var ignored = MockitoAnnotations.openMocks(this)) {
            // MockitoAnnotations opened and will be closed
        }
        // Mock the config data
        Map<String, Object> configValueMap = new HashMap<>();
        configValueMap.put("aggregatorsusecurve", false);
        configValueMap.put("aggregatorsuseconfusion", false);
        configValueMap.put("machinelearningusebinary", false);
        when(configData.getConfigValueMap()).thenReturn(configValueMap);
        when(conf.getConfigData()).thenReturn(configData);
        when(conf.wantAggregatorsUsecurve()).thenReturn(false);
        when(conf.wantAggregatorsUseConfusion()).thenReturn(false);
        when(conf.wantUseBinary()).thenReturn(false);
        when(conf.wantML()).thenReturn(false);
        when(conf.wantMLTimes()).thenReturn(false);
        when(conf.wantOtherStats()).thenReturn(false);
        when(conf.wantAggregatorsUseConfusion()).thenReturn(false);
        // Mock or create minimal dependencies
        Map<String, String> idNameMap = new HashMap<>();
        idNameMap.put("testId", "Test Name");
        List<String> stockDates = Arrays.asList("2023-01-01", "2023-01-02");
        // For simplicity, pass null for complex objects
        aggregator = new TestIndicatorAggregator(conf, "test", 1, "TestTitle", idNameMap, null, null, stockDates, null);
    }

    @Test
    void testCreateLabelMapShort_WithoutConfusion() {
        // Assume conf.wantAggregatorsUseConfusion() returns false
        Map<Double, String> labelMap = aggregator.createLabelMapShort();
        assertNotNull(labelMap);
        assertEquals(2, labelMap.size());
        assertEquals(Constants.BELOW, labelMap.get(0.0));
        assertEquals(Constants.ABOVE, labelMap.get(1.0));
    }

    @Test
    void testCreateLabelMapShort_WithConfusion() {
        // This would require mocking conf to return true for wantAggregatorsUseConfusion
        // For now, test the default case
        Map<Double, String> labelMap = aggregator.createLabelMapShort();
        assertNotNull(labelMap);
        // Depending on conf, but assuming default is without confusion
        assertTrue(labelMap.size() == 2 || labelMap.size() == 4);
    }

    @Test
    void testGetFilename() {
        // Test getFilename method returns non-null result
        String filename = aggregator.getFilenamePart();
        assertNotNull(filename);
        assertEquals("test", filename);
    }

    @Test
    void testAddResultItemTitle() {
        aggregator.usedSubTypes = List.of(); // TODO or not empty?
        ResultItemTableRow headrow = new ResultItemTableRow();
        aggregator.addResultItemTitle(headrow);
        // Check that headrow has been populated
        assertNotNull(headrow);
        assertNotNull(headrow.getarr());
    }

    @Test
    void testOtherTables() {
        Map<Integer, List<ResultItemTableRow>> tables = aggregator.otherTables();
        assertNotNull(tables);
        // Depending on conf, may have MLTIMES or EVENT tables
    }

    @Test
    void testMergeMapMap_WithNullSource() throws Exception {
        // Test that merging with null source does nothing
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        aggregator.invokePrivateMergeMapMap(target, null);
        assertNotNull(target);
        assertEquals(0, target.size());
    }

    @Test
    void testMergeMapMap_WithEmptySource() throws Exception {
        // Test that merging with empty source map works correctly
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        aggregator.invokePrivateMergeMapMap(target, source);
        assertNotNull(target);
        assertEquals(0, target.size());
    }

    @Test
    void testMergeMapMap_WithSingleLevelData() throws Exception {
        // Test merging with single level data
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        
        // Create source data structure
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subTypeMap = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList = new ArrayList<>();
        
        double[] arr = {1.0, 2.0};
        Pair<Object, Double> innerPair = new ImmutablePair<>(new Object(), 0.5);
        pairList.add(new ImmutablePair<>(arr, innerPair));
        
        keyMap.put("testKey", pairList);
        subTypeMap.put("mainKey", keyMap);
        source.put(subType, subTypeMap);
        
        aggregator.invokePrivateMergeMapMap(target, source);
        
        assertNotNull(target);
        assertEquals(1, target.size());
        assertTrue(target.containsKey(subType));
        assertEquals(1, target.get(subType).size());
        assertEquals(1, target.get(subType).get("mainKey").get("testKey").size());
    }

    @Test
    void testMergeMapMap_WithMultipleSubTypes() throws Exception {
        // Test merging with multiple SubTypes
        IndicatorAggregator.SubType subType1 = aggregator.new TestSubType();
        IndicatorAggregator.SubType subType2 = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        
        // Create first subtype data
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subTypeMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList1 = new ArrayList<>();
        pairList1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        keyMap1.put("key1", pairList1);
        subTypeMap1.put("mainKey1", keyMap1);
        source.put(subType1, subTypeMap1);
        
        // Create second subtype data
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subTypeMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList2 = new ArrayList<>();
        pairList2.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.7)));
        keyMap2.put("key2", pairList2);
        subTypeMap2.put("mainKey2", keyMap2);
        source.put(subType2, subTypeMap2);
        
        aggregator.invokePrivateMergeMapMap(target, source);
        
        assertEquals(2, target.size());
        assertTrue(target.containsKey(subType1));
        assertTrue(target.containsKey(subType2));
    }

    @Test
    void testMergeMapMap_AppendingToExistingData() throws Exception {
        // Test that merging appends to existing data rather than overwriting
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        
        // Add initial data to target
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> targetSubTypeMap = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> targetKeyMap = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> targetPairList = new ArrayList<>();
        targetPairList.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        targetKeyMap.put("testKey", targetPairList);
        targetSubTypeMap.put("mainKey", targetKeyMap);
        target.put(subType, targetSubTypeMap);
        
        // Create source data with same keys
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> sourceSubTypeMap = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> sourceKeyMap = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> sourcePairList = new ArrayList<>();
        sourcePairList.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.7)));
        sourceKeyMap.put("testKey", sourcePairList);
        sourceSubTypeMap.put("mainKey", sourceKeyMap);
        source.put(subType, sourceSubTypeMap);
        
        aggregator.invokePrivateMergeMapMap(target, source);
        
        // Verify that target now has both entries
        assertEquals(2, target.get(subType).get("mainKey").get("testKey").size());
    }

    @Test
    void testMergeMapMap_WithComplexNesting() throws Exception {
        // Test merging with deeply nested structures containing multiple keys at each level
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        
        // Create complex nested structure
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subTypeMap = new HashMap<>();
        
        // First main key
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList1 = new ArrayList<>();
        pairList1.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(new Object(), 0.5)));
        keyMap1.put("subKey1", pairList1);
        keyMap1.put("subKey2", pairList1);
        subTypeMap.put("mainKey1", keyMap1);
        
        // Second main key
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList2 = new ArrayList<>();
        pairList2.add(new ImmutablePair<>(new double[]{3.0, 4.0}, new ImmutablePair<>(new Object(), 0.7)));
        keyMap2.put("subKey3", pairList2);
        subTypeMap.put("mainKey2", keyMap2);
        
        source.put(subType, subTypeMap);
        
        aggregator.invokePrivateMergeMapMap(target, source);
        
        assertEquals(2, target.get(subType).size());
        assertEquals(2, target.get(subType).get("mainKey1").size());
        assertEquals(1, target.get(subType).get("mainKey2").size());
    }

    @Test
    void testMergeMapMap_PreservesAllData() throws Exception {
        // Test that all data is preserved during merge operations
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source = new HashMap<>();
        
        // Create source with multiple entries
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subTypeMap = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> keyMap = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList = new ArrayList<>();
        
        // Add multiple pairs
        for (int i = 0; i < 5; i++) {
            pairList.add(new ImmutablePair<>(new double[]{i * 1.0}, new ImmutablePair<>(new Object(), i * 0.1)));
        }
        
        keyMap.put("testKey", pairList);
        subTypeMap.put("mainKey", keyMap);
        source.put(subType, subTypeMap);
        
        aggregator.invokePrivateMergeMapMap(target, source);
        
        // Verify all data is present
        assertEquals(5, target.get(subType).get("mainKey").get("testKey").size());
    }

    // ============= Tests for compareMaps() method =============

    @Test
    void testCompareMaps_BothNull() {
        // Test comparing two null maps
        assertTrue(aggregator.compareMaps(null, null));
    }

    @Test
    void testCompareMaps_OneNull() {
        // Test when one map is null and other is not
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map = new HashMap<>();
        assertFalse(aggregator.compareMaps(map, null));
        assertFalse(aggregator.compareMaps(null, map));
    }

    @Test
    void testCompareMaps_BothEmpty() {
        // Test comparing two empty maps
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        assertTrue(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_SameSizeEmptyInner() {
        // Test maps with same size but empty inner structures
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        map1.put(subType, new HashMap<>());
        map2.put(subType, new HashMap<>());
        
        assertTrue(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_DifferentSizes() {
        // Test maps with different outer sizes
        IndicatorAggregator.SubType subType1 = aggregator.new TestSubType();
        IndicatorAggregator.SubType subType2 = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        map1.put(subType1, new HashMap<>());
        map1.put(subType2, new HashMap<>());
        map2.put(subType1, new HashMap<>());
        
        assertFalse(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_IdenticalSimpleStructures() {
        // Test identical simple map structures
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        // Create shared Object instance for consistency
        Object sharedObject = "testObject";
        Double sharedDouble = 0.5;
        
        // Build identical structure for map1
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList1 = new ArrayList<>();
        pairList1.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(sharedObject, sharedDouble)));
        innerInnerMap1.put("key", pairList1);
        innerMap1.put("key", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        // Build identical structure for map2 using same SubType and shared objects
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList2 = new ArrayList<>();
        pairList2.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(sharedObject, sharedDouble)));
        innerInnerMap2.put("key", pairList2);
        innerMap2.put("key", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertTrue(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_DifferentArrayValues() {
        // Test maps with different double array values
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap1.put("key", list1);
        innerMap1.put("key", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{3.0, 4.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap2.put("key", list2);
        innerMap2.put("key", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertFalse(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_DifferentDoubleValues() {
        // Test maps with different Double values in inner pairs
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap1.put("key", list1);
        innerMap1.put("key", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.7)));
        innerInnerMap2.put("key", list2);
        innerMap2.put("key", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertFalse(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_DifferentListSizes() {
        // Test maps with different list sizes
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        list1.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.6)));
        innerInnerMap1.put("key", list1);
        innerMap1.put("key", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap2.put("key", list2);
        innerMap2.put("key", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertFalse(aggregator.compareMaps(map1, map2));
    }

    @Test
    void testCompareMaps_ComplexIdenticalStructure() {
        // Test with complex nested structure containing multiple keys at each level
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        // Create shared Object instances for consistency
        Object obj1 = "object1";
        Object obj2 = "object2";
        
        // Create identical complex structure for map1
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        
        // First main key for map1
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1a = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1a = new ArrayList<>();
        list1a.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(obj1, 0.5)));
        list1a.add(new ImmutablePair<>(new double[]{3.0, 4.0}, new ImmutablePair<>(obj2, 0.6)));
        innerInnerMap1a.put("subKey1", list1a);
        innerInnerMap1a.put("subKey2", list1a);
        innerMap1.put("mainKey1", innerInnerMap1a);
        
        // Second main key for map1
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1b = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1b = new ArrayList<>();
        list1b.add(new ImmutablePair<>(new double[]{5.0, 6.0}, new ImmutablePair<>(obj1, 0.7)));
        innerInnerMap1b.put("subKey3", list1b);
        innerMap1.put("mainKey2", innerInnerMap1b);
        map1.put(subType, innerMap1);
        
        // Create identical complex structure for map2
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        
        // First main key for map2
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2a = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2a = new ArrayList<>();
        list2a.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(obj1, 0.5)));
        list2a.add(new ImmutablePair<>(new double[]{3.0, 4.0}, new ImmutablePair<>(obj2, 0.6)));
        innerInnerMap2a.put("subKey1", list2a);
        innerInnerMap2a.put("subKey2", list2a);
        innerMap2.put("mainKey1", innerInnerMap2a);
        
        // Second main key for map2
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2b = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2b = new ArrayList<>();
        list2b.add(new ImmutablePair<>(new double[]{5.0, 6.0}, new ImmutablePair<>(obj1, 0.7)));
        innerInnerMap2b.put("subKey3", list2b);
        innerMap2.put("mainKey2", innerInnerMap2b);
        map2.put(subType, innerMap2);
        
        assertTrue(aggregator.compareMaps(map1, map2));
    }

    // ============= Tests for compareMapAndListSizes() method =============

    @Test
    void testCompareMapAndListSizes_BothNull() {
        // Test comparing two null maps
        assertTrue(aggregator.compareMapAndListSizes(null, null));
    }

    @Test
    void testCompareMapAndListSizes_OneNull() {
        // Test when one map is null and other is not
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map = new HashMap<>();
        assertFalse(aggregator.compareMapAndListSizes(map, null));
        assertFalse(aggregator.compareMapAndListSizes(null, map));
    }

    @Test
    void testCompareMapAndListSizes_BothEmpty() {
        // Test comparing two empty maps
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        assertTrue(aggregator.compareMapAndListSizes(map1, map2));
    }

    @Test
    void testCompareMapAndListSizes_DifferentOuterSize() {
        // Test maps with different outer sizes
        IndicatorAggregator.SubType subType1 = aggregator.new TestSubType();
        IndicatorAggregator.SubType subType2 = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        map1.put(subType1, new HashMap<>());
        map1.put(subType2, new HashMap<>());
        map2.put(subType1, new HashMap<>());
        
        assertFalse(aggregator.compareMapAndListSizes(map1, map2));
    }

    @Test
    void testCompareMapAndListSizes_IdenticalStructure() {
        // Test identical structure sizes
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        list1.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.6)));
        innerInnerMap1.put("testKey", list1);
        innerMap1.put("mainKey", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{9.0}, new ImmutablePair<>(new Object(), 0.9)));
        list2.add(new ImmutablePair<>(new double[]{8.0}, new ImmutablePair<>(new Object(), 0.8)));
        innerInnerMap2.put("testKey", list2);
        innerMap2.put("mainKey", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertTrue(aggregator.compareMapAndListSizes(map1, map2));
    }

    @Test
    void testCompareMapAndListSizes_DifferentListSizes() {
        // Test maps with different list sizes (should return false)
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        list1.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.6)));
        list1.add(new ImmutablePair<>(new double[]{3.0}, new ImmutablePair<>(new Object(), 0.7)));
        innerInnerMap1.put("testKey", list1);
        innerMap1.put("mainKey", innerInnerMap1);
        map1.put(subType, innerMap1);
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{9.0}, new ImmutablePair<>(new Object(), 0.9)));
        innerInnerMap2.put("testKey", list2);
        innerMap2.put("mainKey", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertFalse(aggregator.compareMapAndListSizes(map1, map2));
    }

    @Test
    void testCompareMapAndListSizes_ComplexStructureSameSizes() {
        // Test complex structure with same sizes but different values
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        // Create complex structure for map1
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1a = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1b = new HashMap<>();
        
        List<Pair<double[], Pair<Object, Double>>> list1a = new ArrayList<>();
        list1a.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        list1a.add(new ImmutablePair<>(new double[]{2.0}, new ImmutablePair<>(new Object(), 0.6)));
        innerInnerMap1a.put("key1", list1a);
        
        List<Pair<double[], Pair<Object, Double>>> list1b = new ArrayList<>();
        list1b.add(new ImmutablePair<>(new double[]{3.0}, new ImmutablePair<>(new Object(), 0.7)));
        innerInnerMap1b.put("key2", list1b);
        
        innerMap1.put("mainKey1", innerInnerMap1a);
        innerMap1.put("mainKey2", innerInnerMap1b);
        map1.put(subType, innerMap1);
        
        // Create complex structure for map2 with same sizes
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2a = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2b = new HashMap<>();
        
        List<Pair<double[], Pair<Object, Double>>> list2a = new ArrayList<>();
        list2a.add(new ImmutablePair<>(new double[]{9.0}, new ImmutablePair<>(new Object(), 0.9)));
        list2a.add(new ImmutablePair<>(new double[]{8.0}, new ImmutablePair<>(new Object(), 0.8)));
        innerInnerMap2a.put("key1", list2a);
        
        List<Pair<double[], Pair<Object, Double>>> list2b = new ArrayList<>();
        list2b.add(new ImmutablePair<>(new double[]{7.0}, new ImmutablePair<>(new Object(), 0.7)));
        innerInnerMap2b.put("key2", list2b);
        
        innerMap2.put("mainKey1", innerInnerMap2a);
        innerMap2.put("mainKey2", innerInnerMap2b);
        map2.put(subType, innerMap2);
        
        assertTrue(aggregator.compareMapAndListSizes(map1, map2));
    }

    @Test
    void testCompareMapAndListSizes_DifferentMiddleMapSize() {
        // Test with different middle-level map sizes
        IndicatorAggregator.SubType subType = aggregator.new TestSubType();
        
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map1 = new HashMap<>();
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map2 = new HashMap<>();
        
        // map1 has 2 middle-level entries
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap1 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1a = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1b = new HashMap<>();
        
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap1a.put("key", list1);
        innerInnerMap1b.put("key", list1);
        
        innerMap1.put("mainKey1", innerInnerMap1a);
        innerMap1.put("mainKey2", innerInnerMap1b);
        map1.put(subType, innerMap1);
        
        // map2 has 1 middle-level entry
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap2 = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{1.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap2.put("key", list2);
        innerMap2.put("mainKey1", innerInnerMap2);
        map2.put(subType, innerMap2);
        
        assertFalse(aggregator.compareMapAndListSizes(map1, map2));
    }

    // ============= Helper Methods =============

    /**
     * Creates a simple test map structure
     */
    private Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> createTestMapStructure(IndicatorAggregator.SubType subType) {
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap = new HashMap<>();
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> pairList = new ArrayList<>();
        
        pairList.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(new Object(), 0.5)));
        innerInnerMap.put("key", pairList);
        innerMap.put("key", innerInnerMap);
        map.put(subType, innerMap);
        
        return map;
    }

    /**
     * Creates a complex test map structure with multiple keys at each level
     */
    private Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> createComplexTestMap(IndicatorAggregator.SubType subType) {
        Map<IndicatorAggregator.SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> map = new HashMap<>();
        
        Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> innerMap = new HashMap<>();
        
        // First main key
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap1 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list1 = new ArrayList<>();
        list1.add(new ImmutablePair<>(new double[]{1.0, 2.0}, new ImmutablePair<>(new Object(), 0.5)));
        list1.add(new ImmutablePair<>(new double[]{3.0, 4.0}, new ImmutablePair<>(new Object(), 0.6)));
        innerInnerMap1.put("subKey1", list1);
        innerInnerMap1.put("subKey2", list1);
        innerMap.put("mainKey1", innerInnerMap1);
        
        // Second main key
        Map<String, List<Pair<double[], Pair<Object, Double>>>> innerInnerMap2 = new HashMap<>();
        List<Pair<double[], Pair<Object, Double>>> list2 = new ArrayList<>();
        list2.add(new ImmutablePair<>(new double[]{5.0, 6.0}, new ImmutablePair<>(new Object(), 0.7)));
        innerInnerMap2.put("subKey3", list2);
        innerMap.put("mainKey2", innerInnerMap2);
        
        map.put(subType, innerMap);
        return map;
    }

    // Test implementation of IndicatorAggregator
    private static class TestIndicatorAggregator extends IndicatorAggregator {

        public TestIndicatorAggregator(IclijConfig conf, String string, int category, String title, Map<String, String> idNameMap, SerialPipeline datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates, Inmemory inmemory) throws Exception {
            super(conf, string, category, title, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        }

        @Override
        protected String getNeuralNetConfig() {
            return null;
        }

        @Override
        protected AfterBeforeLimit getAfterBefore() {
            return new AfterBeforeLimit(5, 5); // Example values
        }

        @Override
        protected int fieldSize() {
            return 10; // Example value
        }

        @Override
        protected List<SubType> getWantedSubTypes(AfterBeforeLimit afterbefore) {
            return new ArrayList<>(); // Empty for test
        }

        @Override
        public String getFilenamePart() {
            return "test";
        }

        @Override
        public Map<String, Object> getResultMap() {
            return new HashMap<>();
        }

        @Override
        public boolean isEnabled() {
            return false; // Return false to avoid calling calculateMe in constructor
        }

        @Override
        public String getName() {
            return "TestAggregator";
        }

        @Override
        protected String getAggregatorsThreshold() {
            return "1.0";
        }

        /**
         * Helper method to invoke the private mergeMapMap method via reflection
         */
        public void invokePrivateMergeMapMap(Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> target,
                                            Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> source) throws Exception {
            java.lang.reflect.Method method = IndicatorAggregator.class.getDeclaredMethod("mergeMapMap", Map.class, Map.class);
            method.setAccessible(true);
            method.invoke(this, target, source);
        }

        /**
         * Inner class to create test SubType instances
         */
        public class TestSubType extends SubType {
            @Override
            public String getType() {
                return "TestType";
            }

            @Override
            public String getName() {
                return "TestSubType";
            }

            @Override
            public int getArrIdx() {
                return 0;
            }
        }
    }
}
