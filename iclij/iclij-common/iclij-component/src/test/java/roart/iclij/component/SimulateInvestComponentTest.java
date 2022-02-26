package roart.iclij.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.testdata.TestData;

public class SimulateInvestComponentTest {
    @Test
    public void test() {
        TestData testData = new TestData();
        Map<String, List<List<Double>>> categoryValueMap = testData.getVolumeCatValMap();
        Map<String, List<List<Object>>> volumeMap = testData.getVolumeMap();
        List<String> stockDates = testData.getStockDates(LocalDate.now(), 14, false);
        SimulateInvestComponent comp = new SimulateInvestComponent();
        int firstidx = 13; 
        int lastidx = 0;
        int interval = 1;
        SimulateInvestConfig config = new SimulateInvestConfig();
        Map<String, Double> limitMap = new HashMap<>();
        limitMap.put("DKK", 100000.0);
        config.setVolumelimits(limitMap);
        Map<Integer, List<String>> map = comp.getVolumeExcludesFull(config, interval, categoryValueMap, volumeMap, firstidx, lastidx);
        System.out.println(map);
        assertFalse(map.isEmpty());
    }

}
