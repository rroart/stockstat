package roart.simulate.util;

import java.time.LocalDate;
import java.time.Period;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.constants.Constants;
import roart.common.model.SimDataDTO;
import roart.common.pipeline.data.SerialListSimulateStock;
import roart.common.pipeline.data.SerialListStockHistory;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.MapUtil;
import roart.common.util.MathUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.constants.SimConstants;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.SimulateFilter;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.SimulateInvestUtils;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.simulate.model.SimulateStock;
import roart.simulate.model.StockHistory;

public class SimUtil {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private static final boolean VERIFYCACHE = false;

    public static List<Pair<String, Double>> getTradeStocks(SerialListSimulateStock stockhistory) {
        return getTradeStocks(stockhistory.getList());
    }
    
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

    public static boolean isStable(SimulateFilter filter, SerialListStockHistory history, List<Double> list) {
        return isStable(filter, history.getList(), list);
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


    public Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> getSimConfigs(String market, AutoSimulateInvestConfig autoSimConf, List<SimulateFilter> filter, List<SimulateFilter[]> filters, IclijConfig config, MarketActionData actionData, ComponentData param) {
        List<SimDataDTO> all = new ArrayList<>();
        try {
            String simkey = CacheConstants.SIMDATA + market + autoSimConf.getStartdate() + autoSimConf.getEnddate();
            all =  (List<SimDataDTO>) MyCache.getInstance().get(simkey);
            if (all == null) {
                LocalDate startDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getStartdate()));
                LocalDate endDate = null;
                if (autoSimConf.getEnddate() != null) {
                    endDate = TimeUtil.convertDate(TimeUtil.replace(autoSimConf.getEnddate()));
                }
                all = param.getService().getIo().getIdbDao().getAllSimData(market, null, null); // fix later: , startDate, endDate);
                MyCache.getInstance().put(simkey, all);
            }
            /*
            if (VERIFYCACHE && trendMap != null) {
                for (Entry<Integer, Trend> entry : newTrendMap.entrySet()) {
                    int key2 = entry.getKey();
                    Trend v2 = entry.getValue();
                    Trend v = trendMap.get(key2);
                    if (v2 != null && !v2.toString().equals(v.toString())) {
                        log.error("Difference with cache");
                    }
                }
            }
             */
        } catch (Exception e) {
            log.error(Constants.ERROR, e);
        }
        List<SimulateFilter[]> list = null;
        if (autoSimConf != null) {
            List<SimulateFilter> listoverride = filter; //autoSimConf.getFilters();
            list = getDefaultList(actionData);
            if (list != null) {
                filters.addAll(list);
            }
            if (listoverride != null) {
                mergeFilterList(list, listoverride);
            }
        }
        String listString = JsonUtil.convert(list);
        String key = CacheConstants.AUTOSIMCONFIG + market + autoSimConf.getStartdate() + autoSimConf.getEnddate() + "_" + autoSimConf.getInterval() + "_" + autoSimConf.getPeriod() + "_" + autoSimConf.getScorelimit().doubleValue() + " " + listString;
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> retMap = (Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>>) MyCache.getInstance().get(key);
        Map<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> newRetMap = new HashMap<>();
        if (retMap == null || VERIFYCACHE) {
            for (SimDataDTO data : all) {
                if (autoSimConf.getScorelimit().doubleValue() > data.getScore().doubleValue()) {
                    continue;
                }
                Integer period = null;
                int months = Period.between(data.getStartdate(), data.getEnddate()).getMonths();
                switch (months) {
                case 1:
                    period = 0;
                    break;
                case 3:
                    period = 1;
                    break;
                    // TODO
                case 12:
                    period = 2;
                    break;
                }
                if (period == null) {
                    continue;
                }
                if (period.intValue() == autoSimConf.getPeriod().intValue()) {
                    String amarket = data.getMarket();
                    if (market.equals(amarket)) {
                        Long dbid = data.getDbid();
                        SimulateInvestConfig simConf = getSimulateInvestConfig(config, data);
                        if (simConf.getInterval().intValue() != autoSimConf.getInterval().intValue()) {
                            continue;
                        }
                        simConf.setVolumelimits(autoSimConf.getVolumelimits());
                        int adviser = simConf.getAdviser();
                        String filterStr = data.getFilter();
                        SimulateFilter myFilter = JsonUtil.convert((String)filterStr, SimulateFilter.class);
                        SimulateFilter[] autoSimConfFilters = list.get(0);
                        if (autoSimConfFilters != null) {
                            SimulateFilter autoSimConfFilter = autoSimConfFilters[adviser];
                            if (myFilter != null && autoSimConfFilter != null) {
                                if (myFilter.getCorrelation() != null && autoSimConfFilter.getCorrelation() > 0 && autoSimConfFilter.getCorrelation() > myFilter.getCorrelation()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getLucky() > 0 && autoSimConfFilter.getLucky() < myFilter.getLucky()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getStable() > 0 && autoSimConfFilter.getStable() < myFilter.getStable()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getShortrun() > 0 && autoSimConfFilter.getShortrun() > myFilter.getShortrun()) {
                                    continue;
                                }
                                if (autoSimConfFilter.getPopulationabove() > myFilter.getPopulationabove()) {
                                    continue;
                                }
                            }
                        }
                        Pair<LocalDate, LocalDate> aKey = new ImmutablePair(data.getStartdate(), data.getEnddate());
                        MapUtil.mapAddMe(newRetMap, aKey, new ImmutablePair(dbid, simConf));
                    }
                }
            }
        }
        if (VERIFYCACHE && retMap != null) {
            for (Entry<Pair<LocalDate, LocalDate>, List<Pair<Long, SimulateInvestConfig>>> entry : newRetMap.entrySet()) {
                Pair<LocalDate, LocalDate> key2 = entry.getKey();
                List<Pair<Long, SimulateInvestConfig>> v2 = entry.getValue();
                List<Pair<Long, SimulateInvestConfig>> v = retMap.get(key2);
                if (v == null || v2 == null || v.size() != v2.size()) {
                    log.error("Difference with cache");
                    continue;
                }
                for (int i = 0; i < v.size(); i++) {
                    if (!v.get(i).equals(v2.get(i))) {
                        log.error("Difference with cache");
                    }
                }
            }
        }
        if (retMap != null) {
            return retMap;
        }
        retMap = newRetMap;
        MyCache.getInstance().put(key, retMap);
        log.info("retmap" + retMap.size() + " " + retMap.keySet());
        return retMap;
    }

    public SimulateInvestConfig getSimulateInvestConfig(IclijConfig config, SimDataDTO data) {
        String configStr = data.getConfig();
        //SimulateInvestConfig s = JsonUtil.convert(configStr, SimulateInvestConfig.class);
        Map defaultMap = config.getConfigData().getConfigMaps().deflt;
        Map map = JsonUtil.convert(configStr, Map.class);
        Map newMap = new HashMap<>();
        newMap.putAll(defaultMap);
        newMap.putAll(map);
        IclijConfig dummy = new IclijConfig(config);
        dummy.getConfigData().setConfigValueMap(newMap);
        SimulateInvestConfig simConf = SimulateInvestUtils.getSimConfig(dummy);
        return simConf;
    }

    public List<SimulateFilter[]> getDefaultList(MarketActionData action) {
        List<SimulateFilter[]> list = null;
        try {
            list = IclijXMLConfig.getSimulate(action.getIclijConfig());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return list;
    }

    public void mergeFilterList(List<SimulateFilter[]> list, List<SimulateFilter> listoverride) {
        for (int i = 0; i < listoverride.size(); i++) {
            SimulateFilter afilter = list.get(0)[i];
            SimulateFilter otherfilter = listoverride.get(i);
            afilter.merge(otherfilter);
        }
    }

}
