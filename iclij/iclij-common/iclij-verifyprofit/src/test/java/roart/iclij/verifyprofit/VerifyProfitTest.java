package roart.iclij.verifyprofit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import roart.testdata.TestData;

public class VerifyProfitTest {
    @Test
    public void test() {
        TestData testData = new TestData();
        Map<String, List<List<Double>>> categoryValueMap = testData.getAbnormCatValMap();
        List<String> stockDates = testData.getStockDates(LocalDate.now(), 3, false);
        int firstidx = 2; 
        int lastidx = 0; 
        Double margin = 9.0;
        Set<String> excluded = new VerifyProfit().getTrend(categoryValueMap, stockDates, firstidx, lastidx, margin);
        assertEquals(1, excluded.size());
        margin = 0.0;
        excluded = new VerifyProfit().getTrend(categoryValueMap, stockDates, firstidx, lastidx, margin);
        assertEquals(1, excluded.size());
    }
}
