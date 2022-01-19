package roart.iclij.verifyprofit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.util.TimeUtil;
import roart.common.util.ValidateUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.Trend;

public class VerifyProfit {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Deprecated
    public void doVerify(List<IncDecItem> list, int days, Map<String, List<List<Double>>> categoryValueMap, LocalDate date, int startoffset, Double threshold) {
        if (days <= 0) {
            return;
        }
        for (IncDecItem item : list) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1 - startoffset);
                Double valNow = mainList.get(mainList.size() - 1 - startoffset - days);
                if (valFuture != null && valNow != null) {
                    boolean verified = (item.isIncrease() && (valFuture / valNow > threshold)) ||
                            (!item.isIncrease() && (valFuture / valNow < threshold));
                    //item.setDate(date);
                    item.setVerified(verified);
                    item.setVerificationComment("Change: " + Trend.roundme(valFuture / valNow) + " Old: " + valNow + " New: " + valFuture);
                }
            }
        }
    }

    public void doVerify(Collection<IncDecItem> list, int days, Map<String, List<List<Double>>> categoryValueMap, LocalDate date, int startoffset, Double threshold, List<String> stockDates) {
        if (days <= 0) {
            return;
        }
        int indexoffsetLast = stockDates.indexOf(TimeUtil.convertDate2(date));
        // not found
        if (indexoffsetLast < 0) {
            return;
        }
        for (IncDecItem item : list) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            String aDate = TimeUtil.convertDate2(item.getDate());
            int itemdateoffset = stockDates.indexOf(aDate);
            // not found
            if (itemdateoffset < 0) {
                continue;
            }
            int indexoffset = indexoffsetLast - itemdateoffset;
            startoffset = 0;
            List<Double> mainList = resultList.get(0);
            log.debug("Sizes {} {}", stockDates.size(), mainList.size());
            if (mainList != null) {
                ValidateUtil.validateSizes(mainList, stockDates);
                if (indexoffset + startoffset < days) {
                    log.error("Recent date {}", aDate);
                    continue;
                }
                Double valFuture = mainList.get(mainList.size() - 1 - indexoffset - startoffset + days);
                Double valNow = mainList.get(mainList.size() - 1 - indexoffset - startoffset);
                if (valFuture != null && valNow != null) {
                    boolean verified = (item.isIncrease() && (valFuture / valNow > threshold)) ||
                            (!item.isIncrease() && (valFuture / valNow < threshold));
                    //item.setDate(date);
                    item.setVerified(verified);
                    item.setVerificationComment("Change: " + Trend.roundme(valFuture / valNow) + " Old: " + valNow + " New: " + valFuture);
                }
            }
        }
    }

    public Trend getTrend(int days, Map<String, List<List<Double>>> categoryValueMap, int startoffset, List<String> stockDates) {
        Trend trend = new Trend();
        if (days <= 0) {
            return trend;
        }
        int nocount2 = 0;
        int nocount = 0;
        int count = 0;
        List<Double> incs = new ArrayList<>();
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            List<List<Double>> resultList = entry.getValue();
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                ValidateUtil.validateSizes(mainList, stockDates);
                if (mainList.size() - 1 - startoffset - days < 0) {
                    continue;
                }
                Double valFuture = mainList.get(mainList.size() - 1 - startoffset);
                Double valNow = mainList.get(mainList.size() - 1 - startoffset - days);
                if (valFuture != null && valNow != null) {
                    if (valFuture > valNow) {
                        trend.up++;
                    }
                    if (valFuture.equals(valNow)) {
                        trend.neutral++;
                    }
                    if (valFuture < valNow) {
                        trend.down++;
                    }
                    incs.add(valFuture / valNow);
                    count++;
                } else {
                    nocount++;
                }
            } else {
                nocount2++;
            }
        }
        if (count == 0) {
            return trend;
        }
        trend.incProp = ((double) trend.up) / count;
        OptionalDouble average = incs
                .stream()
                .mapToDouble(a -> a)
                .average();
        trend.incAverage = average.getAsDouble();
        trend.min = Collections.min(incs);
        trend.max = Collections.max(incs);
        trend.stats = incs.stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).summaryStatistics().toString();
        return trend;
    }
    
    public Trend getTrend(int days, Map<String, List<List<Double>>> categoryValueMap, int startoffset, int loopoffset, List<String> stockDates) {
        Trend trend = new Trend();
        if (days <= 0) {
            return trend;
        }
        int nocount2 = 0;
        int nocount = 0;
        int count = 0;
        List<Double> incs = new ArrayList<>();
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            List<List<Double>> resultList = entry.getValue();
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                if (stockDates != null &&mainList.size() != stockDates.size()) {
                    log.error("Size mismatch {} vs {}", mainList.size(), stockDates.size());
                }
                Double valFuture = mainList.get(mainList.size() - 1 - startoffset);
                Double valNow = mainList.get(mainList.size() - 1 - startoffset - days);
                if (valFuture != null && valNow != null) {
                    if (valFuture > valNow) {
                        trend.up++;
                    }
                    if (valFuture.equals(valNow)) {
                        trend.neutral++;
                    }
                    if (valFuture < valNow) {
                        trend.down++;
                    }
                    incs.add(valFuture / valNow);
                    count++;
                } else {
                    nocount++;
                }
            } else {
                nocount2++;
            }
        }
        if (count == 0) {
            return trend;
        }
        trend.incProp = ((double) trend.up) / count;
        OptionalDouble average = incs
                .stream()
                .mapToDouble(a -> a)
                .average();
        trend.incAverage = average.getAsDouble();
        trend.min = Collections.min(incs);
        trend.max = Collections.max(incs);
        trend.stats = incs.stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).summaryStatistics().toString();
        return trend;
    }

    public Map<Integer, Trend> getTrend(int days, Map<String, List<List<Double>>> categoryValueMap, List<String> stockDates, int firstidx, int lastidx) {
        Map<Integer, Trend> trendMap = new HashMap<>();
        int size = stockDates.size();
        int start = size - 1 - firstidx;
        int end = size - 1 - lastidx;
        //List<Pair<String, Double>> valueList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            int indexOffset = size - 1 - i;
            Trend trend = new Trend();
            trendMap.put(i, trend);
            if (days <= 0) {
                continue;
            }
            int nocount2 = 0;
            int nocount = 0;
            int count = 0;
            List<Double> incs = new ArrayList<>();
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                List<List<Double>> resultList = entry.getValue();
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                if (mainList != null) {
                    ValidateUtil.validateSizes(mainList, stockDates);
                    if (mainList.size() - 1 - indexOffset - days < 0) {
                        continue;
                    }
                    Double valFuture = mainList.get(mainList.size() - 1 - indexOffset);
                    Double valNow = mainList.get(mainList.size() - 1 - indexOffset - days);
                    if (valFuture != null && valNow != null) {
                        if (valFuture > valNow) {
                            trend.up++;
                        }
                        if (valFuture.equals(valNow)) {
                            trend.neutral++;
                        }
                        if (valFuture < valNow) {
                            trend.down++;
                        }
                        double v = valFuture / valNow;
                        if (v < 0) {
                            int jj = 0;
                        }
                        incs.add(valFuture / valNow);
                        count++;
                    } else {
                        nocount++;
                    }
                } else {
                    nocount2++;
                }
            }
            if (count == 0) {
                continue;
            }
            trend.incProp = ((double) trend.up) / count;
            OptionalDouble average = incs
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            trend.incAverage = average.getAsDouble();
            trend.min = Collections.min(incs);
            trend.max = Collections.max(incs);
            trend.stats = incs.stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).summaryStatistics().toString();
            if (trend != null && trend.incAverage < 0) {
                int jj = 0;
            }
        }
        return trendMap;
    }

    public Set<String> getTrend(Map<String, List<List<Double>>> categoryValueMap, List<String> stockDates, int firstidx, int lastidx, Double margin) {
        Set<String> excludes = new HashSet<>();
        if (margin == null) {
            return excludes;
        }
        int size = stockDates.size();
        int start = size - 1 - firstidx;
        int end = size - 1 - lastidx;
        for (int i = start; i <= end; i++) {
            int indexOffset = size - 1 - i;
            for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                if (excludes.contains(entry.getKey())) {
                    continue;
                }
                List<List<Double>> resultList = entry.getValue();
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                if (mainList != null) {
                    ValidateUtil.validateSizes(mainList, stockDates);
                    if (mainList.size() - 1 - indexOffset - 1 < 0) {
                        continue;
                    }
                    Double valFuture = mainList.get(mainList.size() - 1 - indexOffset);
                    Double valNow = mainList.get(mainList.size() - 1 - indexOffset - 1);
                    if (valFuture != null && valNow != null) {
                        if (valNow / valFuture > margin || valFuture / valNow > margin) {
                            excludes.add(entry.getKey());
                        }
                    }
                }
            }
        }
        return excludes;
    }
}
