package roart.iclij.verifyprofit;

import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;
import roart.testdata.TestData;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VerifyProfitTest {
    @Test
    public void test() {
        TestData t = new TestData();
        Map<String, List<List<Double>>> categoryValueMap = t.getAbnormCatValMap();
        System.out.println(categoryValueMap);
        List<String> stockDates = new ArrayList<>();
        stockDates.add("");
        stockDates.add("");
        stockDates.add("");
        int firstidx = 2; 
        int lastidx = 0; 
        Double margin = 9.0;
        Set<String> excluded = new VerifyProfit().getTrend(categoryValueMap, stockDates, firstidx, lastidx, margin);
        System.out.println(excluded);
        assertEquals(1, excluded.size());
    }
}
