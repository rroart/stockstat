package roart.iclij.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.util.Log;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialListMap;
import roart.common.pipeline.data.SerialListMapPlain;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialListSimulateStock;
import roart.common.pipeline.data.SerialListStockHistory;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialSimulateStock;
import roart.common.pipeline.data.SerialStockHistory;
import roart.common.pipeline.data.SerialVolume;
import roart.common.util.JsonUtil;
import roart.constants.SimConstants;
import roart.iclij.component.SimulateInvestComponent.Results;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.simulate.model.Capital;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.testdata.TestData;

public class SimulateInvestComponentTest {
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    public void test() {
        TestData testData = new TestData();
        Map<String, List<List<Double>>> categoryValueMap = testData.getVolumeCatValMap();
        Map<String, Long[]> volumeMap = testData.getVolumeMap();
        List<String> stockDates = testData.getStockDates(LocalDate.now(), 14, false);
        SimulateInvestComponent comp = new SimulateInvestComponent();
        int firstidx = 13; 
        int lastidx = 0;
        int interval = 1;
        SimulateInvestConfig config = new SimulateInvestConfig();
        Map<String, Double> limitMap = new HashMap<>();
        limitMap.put("DKK", 100000.0);
        config.setVolumelimits(limitMap);
        Map<String, String> currencyMap = Map.of("1", "DKK");
        Map<Integer, List<String>> map = comp.getVolumeExcludesFull(config, interval, categoryValueMap, volumeMap, currencyMap, firstidx, lastidx);
        System.out.println(map);
        assertFalse(map.isEmpty());
    }

    @Test
    public void testSerialization() {
        Double score = 0.1;
        Capital capital = new Capital();
        capital.amount = 0.5;
        Results aResult = new SimulateInvestComponent().new Results();
        aResult.plotCapital = new ArrayList(List.of(1.0));
        aResult.stockhistory = new ArrayList(List.of(new SimulateStock("1", 100.1, 3, 99.1, 101.1, LocalDate.now(), LocalDate.now(), 0, "ok")));
        aResult.history = new ArrayList(List.of(new StockHistory("2022.02.02", capital, capital, 0.2, "0.3", List.of("EQ"), null, null, "trend", 0)));
        //Map<String, Object> map = new HashMap<>();
        //map.put(SimConstants.STOCKHISTORY, aResult.stockhistory);
        //map.put(SimConstants.HISTORY, new SerialListStockHistory(aResult.history));
        //map.put(SimConstants.PLOTCAPITAL, aResult.plotCapital);
        //map.put(SimConstants.SCORE, score);
        int offset = 0;
        
        // old, cur
        
        Map<String, Object> mapx = new HashMap<>();
        mapx.put(SimConstants.HISTORY, aResult.history);

        PipelineData resultMapx = new PipelineData();
        resultMapx.put("" + offset, new SerialMapPlain(mapx));
        
        PipelineData resultMapy = testsub(resultMapx);
        
        System.out.println("Class" + ((SerialMapPlain)resultMapx.get("0")).get(SimConstants.HISTORY).getClass().getName());
        System.out.println("Class" + ((SerialMapPlain)resultMapy.get("0")).get(SimConstants.HISTORY).getClass().getName());
        // equals, but arraylist, and no serialize
        assertEquals(((SerialMapPlain)resultMapx.get("0")).get(SimConstants.HISTORY).getClass().getName(), ((SerialMapPlain)resultMapy.get("0")).get(SimConstants.HISTORY).getClass().getName());

        // SerialListMapPlain SerialListStockHistory
        
        Map<String, Object> map = new HashMap<>();
        map.put(SimConstants.HISTORY, new SerialListStockHistory(aResult.history));

        PipelineData resultMap = new PipelineData();
        resultMap.put("" + offset, new SerialListMapPlain(map));
        
        PipelineData resultMap2 = testsub(resultMap);
        
        System.out.println("Class" + ((SerialListMapPlain)resultMap.get("0")).get(SimConstants.HISTORY).getClass().getName());
        System.out.println("Class" + ((SerialListMapPlain)resultMap2.get("0")).get(SimConstants.HISTORY).getClass().getName());
        assertNotEquals(((SerialListMapPlain)resultMap.get("0")).get(SimConstants.HISTORY).getClass().getName(), ((SerialListMapPlain)resultMap2.get("0")).get(SimConstants.HISTORY).getClass().getName());

        // SerialListMap SerialListStockHistory        
        
        Map<String, Object> map3 = new HashMap<>();
        map3.put(SimConstants.HISTORY, new SerialListStockHistory(aResult.history));
        map3.put(SimConstants.STOCKHISTORY, new SerialListSimulateStock(aResult.stockhistory));
        map3.put(SimConstants.PLOTCAPITAL, new SerialListPlain(aResult.plotCapital));
        map3.put(SimConstants.SCORE, score);

        PipelineData resultMap3 = new PipelineData();
        resultMap3.put("" + offset, new SerialListMap(map3));

        PipelineData resultMap4 = testsub(resultMap3);

        System.out.println("Class" + ((SerialListStockHistory)(((SerialListMap)resultMap3.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
        System.out.println("Class" + ((SerialListStockHistory)(((SerialListMap)resultMap4.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
        // TODO ok
        assertEquals(((SerialListStockHistory)(((SerialListMap)resultMap3.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName(), ((SerialListStockHistory)(((SerialListMap)resultMap4.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());

        // plain SerialListStockHistory
        
        Map<String, Object> map5 = new HashMap<>();
        map5.put(SimConstants.HISTORY, new SerialListStockHistory(aResult.history));

        PipelineData resultMap5 = new PipelineData();
        resultMap5.put("" + offset, map5);

        PipelineData resultMap6 = testsub(resultMap5);

        System.out.println("Class" + ((SerialListStockHistory)(((Map)resultMap5.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
        System.out.println("Class" + ((LinkedHashMap)(((Map)resultMap6.get("0")).get(SimConstants.HISTORY))).get("list").getClass().getName());
        assertNotEquals(((SerialListStockHistory)(((Map)resultMap5.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName(), ((LinkedHashMap)(((Map)resultMap6.get("0")).get(SimConstants.HISTORY))).get("list").getClass().getName());

        // SerialList SerialStockHistory
        
        List<SerialStockHistory> list8 = new ArrayList<>();
        for (StockHistory entry : aResult.history) {
            list8.add(new SerialStockHistory(entry));
        }

        List<SerialSimulateStock> list82 = new ArrayList<>();
        for (SimulateStock entry : aResult.stockhistory) {
            list82.add(new SerialSimulateStock(entry));
        }

        Map<String, Object> map7 = new HashMap<>();
        map7.put(SimConstants.HISTORY, new SerialList(list8));
        map7.put(SimConstants.STOCKHISTORY, new SerialList(list82));
        map7.put(SimConstants.PLOTCAPITAL, new SerialListPlain(aResult.plotCapital));
        map7.put(SimConstants.SCORE, score);

        PipelineData resultMap7 = new PipelineData();
        resultMap7.put("" + offset, new SerialListMap(map7));

        PipelineData resultMap8= testsub(resultMap7);

        System.out.println("Class" + ((SerialList)(((SerialListMap)resultMap7.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
        System.out.println("Class" + ((SerialList)(((SerialListMap)resultMap8.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
        // TODO ok?
        assertEquals(((SerialList)(((SerialListMap)resultMap7.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName(), ((SerialList)(((SerialListMap)resultMap8.get("0")).get(SimConstants.HISTORY))).get(0).getClass().getName());
}
    
    // TODO duplicated
    public <C> C testsub(C object) {
        System.out.println("Checking for " + object.getClass().getCanonicalName());
        String json = JsonUtil.convert(object, mapper);
        
        System.out.println("json" + json);
                
        C data2 = (C) JsonUtil.convertnostrip(json, object.getClass(), mapper);

        String newjson = JsonUtil.convert(data2, mapper);
        
        System.out.println("json2"+ newjson);
        
        assertEquals(json, newjson);

        return data2;
    }

    @Test
    public void test2() {
        
    }
    
    @Test
    public void testHelperMethodsReflection() throws Exception {
        SimulateInvestComponent comp = new SimulateInvestComponent();

        // test myAddAll and getText via reflection
        java.lang.reflect.Method myAddAll = SimulateInvestComponent.class.getDeclaredMethod("myAddAll", List.class, List.class);
        myAddAll.setAccessible(true);
        SimulateStock s1 = new SimulateStock(); s1.setId("A");
        SimulateStock s2 = new SimulateStock(); s2.setId("B");
        List<SimulateStock> main = new ArrayList<>(); main.add(s1);
        List<SimulateStock> toAdd = new ArrayList<>(); toAdd.add(s1); toAdd.add(s2);
        myAddAll.invoke(comp, main, toAdd);
        // now size should be 2
        assertEquals(2, main.size());

        java.lang.reflect.Method getText = SimulateInvestComponent.class.getDeclaredMethod("getText", Map.class, List.class);
        getText.setAccessible(true);
        Map<String,String> stocks = new HashMap<>(); stocks.put("A","Alpha"); stocks.put("B","Beta");
        String txt = (String) getText.invoke(comp, stocks, List.of("A","B"));
        assertTrue(txt.contains("Alpha") && txt.contains("Beta"));

        // test index offset helpers
        SimulateInvestComponent.Mydate mydate = comp.new Mydate(); mydate.indexOffset = 10; mydate.prevIndexOffset = 5;
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setExtradelay(2);
        simConfig.setDelay(3);
        java.lang.reflect.Method getValueIndexOffset = SimulateInvestComponent.class.getDeclaredMethod("getValueIndexOffset", SimulateInvestComponent.Mydate.class, SimulateInvestConfig.class);
        getValueIndexOffset.setAccessible(true);
        int val = (Integer) getValueIndexOffset.invoke(comp, mydate, simConfig);
        assertEquals(12, val);
        java.lang.reflect.Method getBSIndexOffset = SimulateInvestComponent.class.getDeclaredMethod("getBSIndexOffset", SimulateInvestComponent.Mydate.class, SimulateInvestConfig.class);
        getBSIndexOffset.setAccessible(true);
        int bs = (Integer) getBSIndexOffset.invoke(comp, mydate, simConfig);
        assertEquals(5, bs);
        java.lang.reflect.Method getBSValueIndexOffset = SimulateInvestComponent.class.getDeclaredMethod("getBSValueIndexOffset", SimulateInvestComponent.Mydate.class, SimulateInvestConfig.class);
        getBSValueIndexOffset.setAccessible(true);
        int bsv = (Integer) getBSValueIndexOffset.invoke(comp, mydate, simConfig);
        assertEquals(8, bsv);

        // test getBuyList/getSellList/getSum
        Map<String, List<List<Double>>> category = new HashMap<>();
        List<Double> prices = List.of(1.0,2.0,3.0,4.0);
        category.put("A", List.of(prices));
        category.put("B", List.of(prices));
        category.put("C", List.of(prices));
        java.lang.reflect.Method getBuyList = SimulateInvestComponent.class.getDeclaredMethod("getBuyList", Map.class, Set.class, Integer.class);
        getBuyList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimulateStock> newbuys = (List<SimulateStock>) getBuyList.invoke(comp, category, new LinkedHashMap<String,Integer>(){{put("A",1);put("B",1);}}.keySet(), 2);
        assertEquals(2, newbuys.size());

        // prepare mystocks for sell and sum
        SimulateStock owned1 = new SimulateStock(); owned1.setId("A"); owned1.setPrice(5.0); owned1.setCount(2);
        SimulateStock owned2 = new SimulateStock(); owned2.setId("B"); owned2.setPrice(3.0); owned2.setCount(3);
        List<SimulateStock> mystocks = new ArrayList<>(); mystocks.add(owned1); mystocks.add(owned2);
        java.lang.reflect.Method getSellList = SimulateInvestComponent.class.getDeclaredMethod("getSellList", List.class, List.class);
        getSellList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimulateStock> sells = (List<SimulateStock>) getSellList.invoke(comp, mystocks, newbuys);
        // newbuys likely contains A,B so sells should be empty
        assertEquals(0, sells.size());

        java.lang.reflect.Method getSum = SimulateInvestComponent.class.getDeclaredMethod("getSum", List.class);
        getSum.setAccessible(true);
        double sum = (Double) getSum.invoke(comp, mystocks);
        assertEquals(5.0*2 + 3.0*3, sum, 1e-9);

        // test increase/update/stoploss run without exception
        Map<String, List<List<Double>>> cat2 = new HashMap<>();
        List<Double> p2 = List.of(10.0,11.0,12.0,13.0,14.0);
        cat2.put("X", List.of(p2));
        SimulateStock s = new SimulateStock(); s.setId("X"); List<SimulateStock> owned = new ArrayList<>(); owned.add(s);
        java.lang.reflect.Method increase = SimulateInvestComponent.class.getDeclaredMethod("increase", List.class, int.class, Map.class, int.class, int.class);
        increase.setAccessible(true);
        double inc = (Double) increase.invoke(comp, owned, 1, cat2, 2, 0);
        assertEquals(14.0/13.0, inc, 1e-9);
        java.lang.reflect.Method update = SimulateInvestComponent.class.getDeclaredMethod("update", Map.class, List.class, int.class, List.class, int.class, int.class);
        update.setAccessible(true);
        List<SimulateStock> noc = new ArrayList<>();
        int up = (Integer) update.invoke(comp, cat2, owned, 1, noc, 2, 0);
        assertEquals(1, up);

        java.lang.reflect.Method createMap = SimulateInvestComponent.class.getDeclaredMethod("createStockDatesBiMap", List.class);
        createMap.setAccessible(true);
        @SuppressWarnings("unchecked")
        com.google.common.collect.BiMap<String, LocalDate> bi = (com.google.common.collect.BiMap<String, LocalDate>) createMap.invoke(comp, List.of("2020.01.01","2020.01.02","2020.01.03","2020.01.04","2020.01.05"));
        java.lang.reflect.Method stoploss = SimulateInvestComponent.class.getDeclaredMethod("stoploss", List.class, List.class, int.class, Map.class, int.class, List.class, double.class, String.class, com.google.common.collect.BiMap.class, int.class);
        stoploss.setAccessible(true);
        List<SimulateStock> sells2 = new ArrayList<>();
        stoploss.invoke(comp, owned, List.of("2020.01.01","2020.01.02","2020.01.03","2020.01.04","2020.01.05"), 1, cat2, 1, sells2, 0.5, "STOP", bi, 0);
        // ensure sells2 is a list (may be empty), no exception
        assertNotEquals(null, sells2);
    }
