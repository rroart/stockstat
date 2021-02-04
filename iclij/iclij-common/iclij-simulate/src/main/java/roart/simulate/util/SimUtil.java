package roart.simulate.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.constants.SimConstants;
import roart.iclij.util.MiscUtil;
import roart.simulate.model.SimulateStock;

public class SimUtil {
    public static List<Pair<String, Double>> getTradeStocks(Map<String, Object> aMap) {
        List<Pair<String, Double>> list = new ArrayList<>();
        List<SimulateStock> stockhistory = (List<SimulateStock>) aMap.get(SimConstants.STOCKHISTORY);
        Map<String, List<SimulateStock>> stockMap = new HashMap<>();
        for (SimulateStock aStock : stockhistory) {
            new MiscUtil().listGetterAdder(stockMap, aStock.getId(), aStock);
        }
        Map<String, Double> priceMap = new HashMap<>();
        for (Entry<String, List<SimulateStock>> entry2 : stockMap.entrySet()) {
            String id2 = entry2.getKey();
            List<SimulateStock> alist = entry2.getValue();
            double sum = alist.stream().map(e -> e.getCount()*(e.getSellprice() - e.getBuyprice())).reduce(0.0, Double::sum);
            priceMap.put(id2, sum);
        }
        for (Entry<String, Double> anEntry : priceMap.entrySet()) {
            list.add(new ImmutablePair<>(anEntry.getKey(), anEntry.getValue()));
        }
        Comparator<Pair> comparator = new Comparator<>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return Double.valueOf((Double)o1.getRight()).compareTo(Double.valueOf((Double)o2.getRight()));
            }
        };
        Collections.sort(list, comparator);
        Collections.reverse(list);
        return list;
    }


}
