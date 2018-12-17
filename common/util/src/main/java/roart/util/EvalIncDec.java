package roart.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EvalIncDec extends EvalUtil {
    public double calculateResult(Map<String, List<Double>> resultMap) {
        double result = 0;
        double[] array = calculate(resultMap);
        result += array[0] / array[1];
        return result;
    }

    public double[] calculate(Map<String, List<Double>> resultMap) {
        int div = 4;
        double goodBuy = 0;
        long totalBuy = 0;
        int ups = 0;
        int downs = 0;
        List<BuySellList> buys = new ArrayList<>();
        List<BuySellList> sells = new ArrayList<>();
        for (Entry<String, List<Double>> entry : resultMap.entrySet()) {
            List<Double> resultList = entry.getValue();
            Double score = resultList.get(0);
            Double change = resultList.get(1);
            BuySellList aBuy = new BuySellList();
            aBuy.id = entry.getKey();
            aBuy.change = change;
            aBuy.score = score;
            buys.add(aBuy);
            if (change > 1) {
                ups++;
            }
            if (change < 1) {
                downs++;
            }
        }
        //buys.sort(BuySellList.class);
        Collections.sort(buys, (d1, d2) -> Double.compare(d2.score, d1.score));
        Collections.sort(sells, (d1, d2) -> Double.compare(d2.score, d1.score));
        div = 1;
        int myups = ups / div;
        int mydowns = downs / div;
        //myups = Math.min(myups, 40);
        //mydowns = Math.min(mydowns, 40);
        int buyTop = 0;
        int buyBottom = 0;
        int sellTop = 0;
        int sellBottom = 0;
        for (int i = 0; i < myups; i++) {
            if (buys.get(i).change > 1) {
                buyTop++;
            }
            BuySellList aBuy = buys.get(i);
            //log.info("Eval Up {} {} {}", aBuy.id, aBuy.score, aBuy.change);            
            /*
            if (sells.get(i).change > 1) {
                sellTop++;
            }
            */
        }
        int size = buys.size();
        for (int i = 0; i < mydowns; i++) {
            if (buys.get(size - 1 - i).change < 1) {
                buyBottom++;
            }
            BuySellList aBuy = buys.get(size - 1 - i);
            //log.info("Eval Down {} {} {}", aBuy.id, aBuy.score, aBuy.change);            
            /*
            if (sells.get(size - 1 - i).change < 1) {
                sellBottom++;
            }
            */
        }
        goodBuy = buyTop + buyBottom;
        log.info("buyselltopbottom {} {} {} {} {} {}", buyTop, buyBottom, sellTop, sellBottom, myups, mydowns);
        totalBuy = myups + mydowns;
        double[] array = new double[2];
        array[0] = goodBuy;
        array[1] = totalBuy;
        return array;
    }
    class BuySellList implements Comparable<Double> {
        String id;
        Double change;
        Double score;
        
        @Override
        public int compareTo(Double score0) {
            return Double.compare(score0, score);
        }
    }
    
    @Override
    public String name() {
        return "incdec";
    }

}

