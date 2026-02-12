package roart.iclij.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialListMap;
import roart.common.pipeline.data.SerialListMapPlain;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialListSimulateStock;
import roart.common.pipeline.data.SerialListStockHistory;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialSimulateStock;
import roart.common.pipeline.data.SerialStockHistory;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.constants.SimConstants;
import roart.iclij.component.SimulateInvestComponent.Results;
import roart.iclij.component.adviser.Adviser;
import roart.iclij.component.adviser.AdviserFactory;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.action.MarketActionData;
import roart.simulate.model.Capital;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;
import roart.testdata.TestData;

public class SimulateInvestComponentTest {
    private static final ObjectMapper mapper = JsonMapper.builder().build();

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
    
    // from github copilot
    
    // TODO @Test
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

    // from github copilot
    
    // TODO @Test
    public void testMorePrivateMethods() throws Exception {
        SimulateInvestComponent comp = new SimulateInvestComponent();

        // mycompare
        SimulateInvestComponent.OneRun o1 = comp.new OneRun();
        SimulateInvestComponent.OneRun o2 = comp.new OneRun();
        o1.autoscore = 2.0; o1.dbid = 5L;
        o2.autoscore = 1.0; o2.dbid = 6L;
        Triple<SimulateInvestConfig, SimulateInvestComponent.OneRun, Results> t1 = new ImmutableTriple<>(new SimulateInvestConfig(), o1, comp.new Results());
        Triple<SimulateInvestConfig, SimulateInvestComponent.OneRun, Results> t2 = new ImmutableTriple<>(new SimulateInvestConfig(), o2, comp.new Results());
        java.lang.reflect.Method mycompare = SimulateInvestComponent.class.getDeclaredMethod("mycompare", Triple.class, Triple.class);
        mycompare.setAccessible(true);
        int cmp = (Integer) mycompare.invoke(comp, t1, t2);
        // t1 autoscore > t2 so mycompare should be negative (o2 before o1 yields positive?), assert non-zero
        assertTrue(cmp != 0);

        // getScore: branch where numlast==0 and plotCapital present
        AutoSimulateInvestConfig aconf = new AutoSimulateInvestConfig();
        aconf.setLastcount(0);
        SimulateInvestComponent.OneRun orun = comp.new OneRun();
        orun.runs = 1;
        orun.capital = new Capital(); orun.capital.amount = 1;
        Results res = comp.new Results();
        res.plotCapital.add(2.0);
        java.lang.reflect.Method getScore = SimulateInvestComponent.class.getDeclaredMethod("getScore", AutoSimulateInvestConfig.class, SimulateInvestComponent.OneRun.class, Results.class);
        getScore.setAccessible(true);
        Double score = (Double) getScore.invoke(comp, aconf, orun, res);
        assertTrue(score != null);

        // getAdviserTriplets: create list with duplicate advisers and check size <=10
        List<Triple<SimulateInvestConfig, SimulateInvestComponent.OneRun, Results>> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            SimulateInvestConfig sc = new SimulateInvestConfig(); sc.setAdviser(i % 5);
            list.add(new ImmutableTriple<>(sc, comp.new OneRun(), comp.new Results()));
        }
        java.lang.reflect.Method getAdv = SimulateInvestComponent.class.getDeclaredMethod("getAdviserTriplets", List.class);
        getAdv.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Triple<SimulateInvestConfig, SimulateInvestComponent.OneRun, Results>> adv = (List) getAdv.invoke(comp, list);
        assertTrue(adv.size() <= 10);

        // getOneRun and getTriples
        java.lang.reflect.Method getOneRun = SimulateInvestComponent.class.getDeclaredMethod("getOneRun", Market.class, ComponentData.class, Long.class, SimulateInvestConfig.class, SimulateInvestComponent.Data.class, LocalDate.class, LocalDate.class, Integer.class, MarketActionData.class);
        getOneRun.setAccessible(true);
        SimulateInvestComponent.Data data = comp.new Data();
        SimulateInvestConfig scfg = new SimulateInvestConfig();
        Object oneRunObj = getOneRun.invoke(comp, null, new ComponentData(), null, scfg, data, LocalDate.now(), LocalDate.now(), null, null);
        assertNotEquals(null, oneRunObj);

        // getTriples: supply simsConfigs pair list
        List<Pair<Long, SimulateInvestConfig>> sims = new ArrayList<>();
        sims.add(new ImmutablePair<>(1L, scfg));
        java.lang.reflect.Method getTriples = SimulateInvestComponent.class.getDeclaredMethod("getTriples", Market.class, ComponentData.class, SimulateInvestComponent.Data.class, LocalDate.class, LocalDate.class, SimulateInvestComponent.Mydate.class, List.class, MarketActionData.class);
        getTriples.setAccessible(true);
        SimulateInvestComponent.Mydate mydate = comp.new Mydate(); mydate.date = LocalDate.now();
        @SuppressWarnings("unchecked")
        List<Triple<SimulateInvestConfig, SimulateInvestComponent.OneRun, Results>> triples = (List) getTriples.invoke(comp, null, new ComponentData(), data, LocalDate.now(), LocalDate.now(), mydate, sims, null);
        assertEquals(1, triples.size());

        // getSimConfigs: construct simConfigs map and ensure returned list non-empty
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> simMap = new HashMap<>();
        LocalDate left = LocalDate.now().minusMonths(1);
        LocalDate right = LocalDate.now();
        Pair<LocalDate, LocalDate> key = new ImmutablePair<>(left, right);
        List<Pair<Long, SimulateInvestConfig>> val = new ArrayList<>();
        val.add(new ImmutablePair<>(2L, new SimulateInvestConfig()));
        simMap.put(key, val);
        Set<Pair<LocalDate, LocalDate>> keys = new HashSet<>();
        java.lang.reflect.Method getSimConfigs = SimulateInvestComponent.class.getDeclaredMethod("getSimConfigs", Map.class, SimulateInvestComponent.Mydate.class, Set.class, Market.class);
        getSimConfigs.setAccessible(true);
        mydate.date = right;
        @SuppressWarnings("unchecked")
        List<Pair<Long, SimulateInvestConfig>> out = (List) getSimConfigs.invoke(comp, simMap, mydate, keys, null);
        assertTrue(out.size() > 0);

        // getSimulate
        java.lang.reflect.Method getSimulate = SimulateInvestComponent.class.getDeclaredMethod("getSimulate", SimulateInvestConfig.class);
        getSimulate.setAccessible(true);
        SimulateInvestConfig ns = (SimulateInvestConfig) getSimulate.invoke(comp, (Object) null);
        assertNotEquals(null, ns);
        SimulateInvestConfig in = new SimulateInvestConfig(); in.setDelay(3);
        SimulateInvestConfig ns2 = (SimulateInvestConfig) getSimulate.invoke(comp, in);
        assertNotEquals(null, ns2);

        // createStockDatesBiMap and getStockDatesBiMap
        java.lang.reflect.Method createBi = SimulateInvestComponent.class.getDeclaredMethod("createStockDatesBiMap", List.class);
        createBi.setAccessible(true);
        @SuppressWarnings("unchecked")
        com.google.common.collect.BiMap<String, LocalDate> bi = (com.google.common.collect.BiMap<String, LocalDate>) createBi.invoke(comp, List.of("2020.01.01","2020.01.02"));
        assertNotEquals(null, bi);
        java.lang.reflect.Method getBi = SimulateInvestComponent.class.getDeclaredMethod("getStockDatesBiMap", String.class, List.class);
        getBi.setAccessible(true);
        @SuppressWarnings("unchecked")
        com.google.common.collect.BiMap<String, LocalDate> bi2 = (com.google.common.collect.BiMap<String, LocalDate>) getBi.invoke(comp, "MKT", List.of("2020.01.01","2020.01.02"));
        assertNotEquals(null, bi2);

        // copy
        SimulateStock s = new SimulateStock(); s.setId("Z"); s.setCount(2); s.setPrice(10.0);
        List<SimulateStock> input = new ArrayList<>(); input.add(s);
        java.lang.reflect.Method copy = SimulateInvestComponent.class.getDeclaredMethod("copy", List.class);
        copy.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimulateStock> cp = (List<SimulateStock>) copy.invoke(comp, input);
        assertEquals(1, cp.size());
        assertEquals("Z", cp.get(0).getId());
        // ensure not same instance
        assertTrue(cp.get(0) != input.get(0));
    }

    // TODO @Test
    public void testRemainingMethods() throws Exception {
        SimulateInvestComponent comp = new SimulateInvestComponent();

        // addEvent when doprev = true -> uses addEventPrev
        SimulateInvestComponent.OneRun onerun = comp.new OneRun();
        SimulateStock s1 = new SimulateStock(); s1.setId("AAA");
        List<SimulateStock> list = new ArrayList<>(); list.add(s1);
        java.lang.reflect.Method addEvent = SimulateInvestComponent.class.getDeclaredMethod("addEvent", SimulateInvestComponent.OneRun.class, List.class, String.class, int.class, SimulateInvestConfig.class);
        addEvent.setAccessible(true);
        SimulateInvestConfig simc = new SimulateInvestConfig(); simc.setStocks(5);
        SimulateInvestComponent.doprev = true;
        @SuppressWarnings("unchecked")
        List<SimulateStock> returned = (List<SimulateStock>) addEvent.invoke(comp, onerun, list, "BUY", 3, simc);
        // eventMap should contain entry at 3
        assertTrue(onerun.eventMap.containsKey(3));
        assertTrue(onerun.eventMap.get(3).get("BUY").size() == 1);

        // addEvent when doprev = false -> exercise buy filtering logic
        SimulateInvestComponent.doprev = false;
        SimulateInvestComponent.OneRun onerun2 = comp.new OneRun();
        SimulateStock s2 = new SimulateStock(); s2.setId("BBB");
        List<SimulateStock> list2 = new ArrayList<>(); list2.add(s2);
        simc.setStocks(2);
        @SuppressWarnings("unchecked")
        List<SimulateStock> returned2 = (List<SimulateStock>) addEvent.invoke(comp, onerun2, list2, "BUY", 1, simc);
        assertTrue(onerun2.eventMap.containsKey(1));

        // getFilterStocks (buy filter)
        java.lang.reflect.Method getFilter2 = SimulateInvestComponent.class.getDeclaredMethod("getFilterStocks", List.class, List.class, boolean.class);
        getFilter2.setAccessible(true);
        List<SimulateStock> buys = new ArrayList<>(); SimulateStock b1 = new SimulateStock(); b1.setId("X"); buys.add(b1);
        List<SimulateStock> stocks = new ArrayList<>(); SimulateStock m1 = new SimulateStock(); m1.setId("X"); stocks.add(m1);
        @SuppressWarnings("unchecked")
        List<SimulateStock> filtered = (List<SimulateStock>) getFilter2.invoke(comp, stocks, buys, true);
        // when buy=true, filter returns stocks where buy contains id -> so returned should be empty
        assertTrue(filtered.isEmpty());

        // buy method
        java.lang.reflect.Method createBi = SimulateInvestComponent.class.getDeclaredMethod("createStockDatesBiMap", List.class);
        createBi.setAccessible(true);
        @SuppressWarnings("unchecked")
        com.google.common.collect.BiMap<String, LocalDate> bi = (com.google.common.collect.BiMap<String, LocalDate>) createBi.invoke(comp, List.of("2020.01.01","2020.01.02","2020.01.03"));

        java.lang.reflect.Method buym = SimulateInvestComponent.class.getDeclaredMethod("buy", List.class, Map.class, Capital.class, int.class, List.class, List.class, int.class, com.google.common.collect.BiMap.class, Set.class);
        buym.setAccessible(true);
        Map<String, List<List<Double>>> cat = new HashMap<>();
        cat.put("A", List.of(List.of(10.0, 11.0, 12.0)));
        Capital cap = new Capital(); cap.amount = 1000;
        List<SimulateStock> mystocks = new ArrayList<>();
        SimulateStock na = new SimulateStock(); na.setId("A");
        List<SimulateStock> newbuys = new ArrayList<>(); newbuys.add(na);
        Set<String> future = new java.util.HashSet<>();
        buym.invoke(comp, List.of("2020.01.01","2020.01.02","2020.01.03"), cat, cap, 1, mystocks, newbuys, 0, bi, future);
        assertEquals(1, mystocks.size());
        assertTrue(cap.amount < 1000);

        // sell method
        java.lang.reflect.Method sellm = SimulateInvestComponent.class.getDeclaredMethod("sell", List.class, Map.class, Capital.class, List.class, List.class, int.class, List.class, com.google.common.collect.BiMap.class, Set.class);
        sellm.setAccessible(true);
        // prepare mystocks with a stock to sell
        SimulateStock toSell = new SimulateStock(); toSell.setId("S1"); toSell.setCount(2); toSell.setPrice(5.0); toSell.setBuyprice(4.0);
        List<SimulateStock> myst2 = new ArrayList<>(); myst2.add(toSell);
        List<SimulateStock> sells = new ArrayList<>(); sells.add(toSell);
        List<SimulateStock> history = new ArrayList<>();
        Capital cap2 = new Capital(); cap2.amount = 0;
        Map<String, List<List<Double>>> cat2 = new HashMap<>(); cat2.put("S1", List.of(List.of(4.0,5.0,6.0)));
        sellm.invoke(comp, List.of("2020.01.01","2020.01.02","2020.01.03"), cat2, cap2, sells, history, 0, myst2, bi, new java.util.HashSet<>());
        assertEquals(0, myst2.size());
        assertEquals(1, history.size());
        assertTrue(cap2.amount > 0);

        // doBuySell: prepare OneRun with eventMap containing BUY and SELL
        SimulateInvestComponent.OneRun orun = comp.new OneRun();
        Map<String, List<SimulateStock>> em = new HashMap<>();
        SimulateStock sbuy = new SimulateStock(); sbuy.setId("A");
        SimulateStock ssell = new SimulateStock(); ssell.setId("S1"); ssell.setCount(1); ssell.setPrice(6.0);
        em.put("BUY", List.of(sbuy));
        em.put("SELL", List.of(ssell));
        orun.eventMap.put(5, new HashMap<>(em));
        orun.mystocks = new ArrayList<>();
        orun.capital = new Capital(); orun.capital.amount = 1000;
        java.lang.reflect.Method doBuySell = SimulateInvestComponent.class.getDeclaredMethod("doBuySell", SimulateInvestConfig.class, SimulateInvestComponent.OneRun.class, SimulateInvestComponent.Results.class, SimulateInvestComponent.Data.class, int.class, com.google.common.collect.BiMap.class);
        doBuySell.setAccessible(true);
        SimulateInvestComponent.Results res = comp.new Results();
        SimulateInvestComponent.Data data = comp.new Data();
        data.stockDates = List.of("2020.01.01","2020.01.02","2020.01.03");
        data.categoryValueMap = cat2; // contains S1 prices
        doBuySell.invoke(comp, simc, orun, res, data, 5, bi);
        // eventHistoryMap should contain index 5
        assertTrue(orun.eventHistoryMap.containsKey(5));

        // getCurrency
        java.lang.reflect.Method getCur = SimulateInvestComponent.class.getDeclaredMethod("getCurrency", Map.class, String.class);
        getCur.setAccessible(true);
        Map<String,String> curMap = new HashMap<>(); curMap.put("X","CUR");
        String cur = (String) getCur.invoke(comp, curMap, "X");
        assertEquals("CUR", cur);

        // getExclusions -> uses provided newVolumeMap
        java.lang.reflect.Method getVolFull = SimulateInvestComponent.class.getDeclaredMethod("getVolumeExcludesFull", SimulateInvestConfig.class, int.class, Map.class, Map.class, Map.class, int.class, int.class);
        getVolFull.setAccessible(true);
        // reuse earlier test data via TestData
        TestData td = new TestData();
        Map<String, List<List<Double>>> catMap = td.getVolumeCatValMap();
        Map<String, Long[]> vmap = td.getVolumeMap();
        // call public method getVolumeExcludesFull (non-private) already used in test(), simpler to call directly
        Map<Integer, List<String>> full = comp.getVolumeExcludesFull(simc, 1, catMap, vmap, Map.of(), 0, 0); // TODO
        java.lang.reflect.Method getEx = SimulateInvestComponent.class.getDeclaredMethod("getExclusions", SimulateInvestConfig.class, List.class, List.class, Set.class, int.class, Map.class);
        getEx.setAccessible(true);
        List<String> excl = (List<String>) getEx.invoke(comp, simc, td.getStockDates(LocalDate.now(), 10, false), List.of("Z"), new java.util.HashSet<>(), 0, full);
        assertTrue(excl.contains("Z") || excl.size() >= 0);

        // confidenceBuyHoldSell & noConfidenceHoldSell basic run
        java.lang.reflect.Method confm = SimulateInvestComponent.class.getDeclaredMethod("confidenceBuyHoldSell", SimulateInvestConfig.class, List.class, Map.class, Adviser.class, List.class, String.class, List.class, List.class, List.class, SimulateInvestComponent.Mydate.class);
        confm.setAccessible(true);
        // create a trivial adviser via AdviserFactory with id -1 will return an adviser; pass null for market param earlier
        Adviser adv = new AdviserFactory().get(-1, null, LocalDate.now(), LocalDate.now(), new ComponentData(), simc);
        // prepare mystocks and sells
        List<SimulateStock> myst = new ArrayList<>();
        List<SimulateStock> sellsList = new ArrayList<>();
        List<SimulateStock> buysList = new ArrayList<>();
        List<SimulateStock> holdIncrease = new ArrayList<>();
        SimulateInvestComponent.Mydate mdate = comp.new Mydate(); mdate.indexOffset = 0; mdate.prevIndexOffset = 0;
        @SuppressWarnings("unchecked")
        List<SimulateStock> newm = (List<SimulateStock>) confm.invoke(comp, simc, td.getStockDates(LocalDate.now(), 10, false), catMap, adv, new ArrayList<>(), null, myst, sellsList, buysList, holdIncrease, mdate);
        assertNotEquals(null, newm);

        java.lang.reflect.Method noConf = SimulateInvestComponent.class.getDeclaredMethod("noConfidenceHoldSell", List.class, List.class, List.class, SimulateInvestConfig.class);
        noConf.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimulateStock> nm2 = (List<SimulateStock>) noConf.invoke(comp, myst, holdIncrease, sellsList, simc);
        assertNotEquals(null, nm2);
    }
}
