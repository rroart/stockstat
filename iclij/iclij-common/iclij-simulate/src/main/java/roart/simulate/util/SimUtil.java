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
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import roart.common.util.ArraysUtil;
import roart.common.util.MathUtil;
import roart.constants.SimConstants;
import roart.iclij.config.SimulateFilter;
import roart.iclij.service.util.MiscUtil;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;

public class SimUtil {
    public static List<Pair<String, Double>> getTradeStocks(List<SimulateStock> stockhistory) {
        List<Pair<String, Double>> list = new ArrayList<>();
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

    public static boolean isCorrelating(SimulateFilter filter, List<Double> capitalList, List<Double> correlations) {
        if (capitalList.get(0).equals(capitalList.get(capitalList.size() - 1))) {
            return true;
        }
        Double[] capArray = capitalList.toArray(new Double[0]);
        double[] cap = ArraysUtil.convert(capArray);
        double[] geom = MathUtil.getGeoSeq(cap);
        SpearmansCorrelation sc = new SpearmansCorrelation();
        double sp = sc.correlation(cap, geom);
        KendallsCorrelation kc = new KendallsCorrelation();
        double ke = kc.correlation(cap, geom);
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double pe = pc.correlation(cap, geom);
        if (correlations != null) {
            correlations.add(sp);
            correlations.add(ke);
            correlations.add(pe);
        }
        
        double average = (sp + ke + pe) / 3;
        return average > filter.getCorrelation();
    }

    public static boolean isStable(SimulateFilter filter, List<StockHistory> history, List<Double> list) {
        StockHistory last = history.get(history.size() - 1);
        double lasttotal = last.getCapital().amount + last.getSum().amount;
        List<Double> values = new ArrayList<>();
        StockHistory firstHistory = history.get(0);
        double prevtotal = firstHistory.getCapital().amount + firstHistory.getSum().amount;
        for (StockHistory aHistory : history) {
            double total = aHistory.getCapital().amount + aHistory.getSum().amount;
            values.add(total - prevtotal);
            prevtotal = total;
        }
        Collections.sort(values);
        Collections.reverse(values);
        double total = 0;
        int count = 1 + values.size() / 10;
        for (int i = 0; i < count; i++) {
            total += values.get(i);
        }
        if (list != null) {
            list.add(total);
            list.add(lasttotal);
        }
        return total / lasttotal <= filter.getStable();
    }


}
