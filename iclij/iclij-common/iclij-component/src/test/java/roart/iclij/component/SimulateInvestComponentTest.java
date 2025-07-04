package roart.iclij.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        aResult.history = new ArrayList(List.of(new StockHistory("2022.02.02", capital, capital, 0.2, "0.3", List.of("EQ"), "trend", 0)));
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

}
