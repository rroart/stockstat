package roart.stockutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import roart.common.model.StockDTO;
import roart.common.util.TimeUtil;

public class StockUtilTest {

    @Test
    public void test() throws Exception {
        Map<String, List<StockDTO>> stockdatemap = new HashMap<>();
        String date1 = "2025.06.01";
        StockDTO stock1 = new StockDTO();
        stock1.setDate(new TimeUtil().convertDate2(date1));
        stockdatemap.put(date1, new ArrayList<>(List.of(stock1)));

        String date2 = "2025.06.02";
        StockDTO stock2 = new StockDTO();
        stock2.setDate(new TimeUtil().convertDate2(date2));
        stockdatemap.put(date2, new ArrayList<>(List.of(stock2)));

        String date3 = "2025.06.03";
        StockDTO stock3 = new StockDTO();
        stock3.setDate(new TimeUtil().convertDate2(date3));
        stockdatemap.put(date3, new ArrayList<>(List.of(stock3)));
        
        try {
            List<StockDTO>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, TimeUtil.convertDate(date2), 3, 1);
            System.out.println("len " + datedstocklists.length);
            for (int i = 0; i < datedstocklists.length; i++) {
                System.out.println("len2 " + datedstocklists[i].size());
            }
            assertEquals(3, datedstocklists.length);
            assertEquals(1, datedstocklists[0].size());
            assertEquals(1, datedstocklists[1].size());
            assertEquals(0, datedstocklists[2].size());
         } catch (Exception e) {
            e.printStackTrace();
        }

        StockUtil.swap = true;
        try {
            List<StockDTO>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, TimeUtil.convertDate(date2), 3, 1);
            System.out.println("len " + datedstocklists.length);
            for (int i = 0; i < datedstocklists.length; i++) {
                System.out.println("len2 " + datedstocklists[i].size());
            }
            assertEquals(2, datedstocklists.length);
            assertEquals(1, datedstocklists[0].size());
            assertEquals(1, datedstocklists[1].size());
        } catch (Exception e) {
            e.printStackTrace();
        }
}
}
