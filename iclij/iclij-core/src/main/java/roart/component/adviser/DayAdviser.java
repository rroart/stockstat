package roart.component.adviser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.cache.MyCache;
import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.IncDecItem;

public class DayAdviser extends Adviser {

    private boolean indicatorreverse;
    
    private int day;
    
    private Map<String, List<List<Double>>> categoryValueMap;
    
    public DayAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
        indicatorreverse = simulateConfig.getIndicatorReverse();
        day = simulateConfig.getDay();
        if (false) {
            int period = simulateConfig.getPeriod();
            //List<MetaItem> metas = param.getService().getMetas();
            //MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
            //List<String> categories = new MetaUtil().getCategories(meta);
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNING, false);
            aMap.put(ConfigConstants.AGGREGATORS, false);
            aMap.put(ConfigConstants.INDICATORS, false);
            aMap.put(ConfigConstants.INDICATORSMACD, true);
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);
            Integer cat = period; //new MetaUtil().getCategory(meta, period);
            //Integer cat = (Integer) result.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
            //Map<String, Object> resultMaps = param.getResultMap(""+ cat, aMap);
            Map<String, Map<String, Object>> resultMaps = param.getResultMaps();
            Map<String, Object> objectMaps = resultMaps.get("" + cat);
            Map<String, List<List<Double>>> aCategoryValueMap;
            if (simulateConfig.getInterpolate()) {
                aCategoryValueMap = (Map<String, List<List<Double>>>) objectMaps.get(PipelineConstants.FILLLIST);
            } else {
                aCategoryValueMap = (Map<String, List<List<Double>>>) objectMaps.get(PipelineConstants.LIST);
            }
            categoryValueMap = aCategoryValueMap;
        } else {
            if (simulateConfig.getInterpolate()) {
                categoryValueMap = param.getFillCategoryValueMap();
            } else {
                categoryValueMap = param.getCategoryValueMap();
            }
        }
    }

    @Override
    public List<String> getIncs(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        //Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        List<Pair<String, Double>> valueList = getValuePairs(categoryValueMap, indexOffset, excludes);
        if (valueList == null) {
            return new ArrayList<>();
        }
        return valueList.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    //@Override
    public List<IncDecItem> getIncs2(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        int idx = stockDates.size() - 1 - indexOffset;
        if (idx < 0) {
            return new ArrayList<>();
        }
        List<Pair<String, Double>> valueList = valueMap.get(idx);
        if (valueList == null) {
            return new ArrayList<>();
        }
        List<IncDecItem> list = new ArrayList<>();
        for (Pair<String, Double> value : valueList) {
            if (excludes.contains(value.getKey())) {
                continue;
            }
            Double myvalue = value.getValue();
            if (myvalue == null) {
                continue;
            }
            IncDecItem item = new IncDecItem();
            item.setId(value.getKey());
            if (myvalue <= 0) {
                myvalue = -myvalue;
            }
            item.setScore(myvalue);
            item.setIncrease(myvalue > 0);
            list.add(item);
        }
        list = list.stream().filter(m1 -> m1.isIncrease()).collect(Collectors.toList());
        //int subListSize = Math.min(buytop, list.size());
        //list = list.subList(0, subListSize);
        return list;
    }

    private List<Pair<String, Double>> getValuePairs(Map<String, List<List<Double>>> categoryValueMap, int indexOffset, List<String> excludes) {
        List<Pair<String, Double>> valueList = new ArrayList<>();
        for(Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            if (excludes.contains(entry.getKey())) {
                continue;
            }
            List<List<Double>> resultList = entry.getValue();
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                if (mainList.size() - 1 - indexOffset - day < 0) {
                    continue;
                }
                Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                Double valWas = mainList.get(mainList.size() - 1 - indexOffset - day);
                if (valNow != null && valWas != null && valWas != 0) {
                    Pair<String, Double> value = new ImmutablePair<String, Double>(entry.getKey(), valNow / valWas);
                    valueList.add(value);
                }
            }
        }
        valueList.sort(Comparator.comparing(Pair::getValue));
        if (indicatorreverse) {
            Collections.reverse(valueList);
        }
        return valueList;
    }

    private Map<Integer, List<Pair<String, Double>>> getValuePairs(Map<String, List<List<Double>>> categoryValueMap, List<String> stockDates, List<String> excludes, int firstidx, int lastidx) {
        Map<Integer, List<Pair<String, Double>>> valueMap = new HashMap<>();
        int size = stockDates.size();
        int start = size - 1 - firstidx;
        int end = size - 1 - lastidx;
        //List<Pair<String, Double>> valueList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            List<Pair<String, Double>> valueList = new ArrayList<>();
            valueMap.put(i, valueList);
            int indexOffset = size - 1 - i;
            for(Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
                if (excludes.contains(entry.getKey())) {
                    continue;
                }
                List<List<Double>> resultList = entry.getValue();
                if (resultList == null || resultList.isEmpty()) {
                    continue;
                }
                List<Double> mainList = resultList.get(0);
                if (mainList != null) {
                    if (mainList.size() - 1 - indexOffset - day < 0) {
                        continue;
                    }
                    Double valNow = mainList.get(mainList.size() - 1 - indexOffset);
                    Double valWas = mainList.get(mainList.size() - 1 - indexOffset - day);
                    if (valNow != null && valWas != null && valWas != 0) {
                        Pair<String, Double> value = new ImmutablePair<String, Double>(entry.getKey(), valNow / valWas);
                        valueList.add(value);
                    }
                }
            }
            valueList.sort(Comparator.comparing(Pair::getValue));
            if (indicatorreverse) {
                Collections.reverse(valueList);
            }
            valueMap.put(i, valueList);
        }
        return valueMap;
    }

    @Override
    public List<String> getParameters() {
        List<String> list = new ArrayList<>();
        list.add(null);
        return list;
    }

    @Override
    public double getReliability(LocalDate date, Boolean above) {
        return 1;
    }

    @Override
    public void getValueMap(List<String> stockDates, int firstidx, int lastidx,
            Map<String, List<List<Double>>> categoryValueMap2) {
        String key = "ADVISERVALUEMAP" + market.getConfig().getMarket() + this.getClass().getName() + firstidx + "_" + lastidx;
        valueMap = (Map<Integer, List<Pair<String, Double>>>) MyCache.getInstance().get(key);
        if (valueMap != null) {
            return;
        }
        valueMap = getValuePairs(categoryValueMap, stockDates, new ArrayList<>(), firstidx, lastidx);                
        MyCache.getInstance().put(key, valueMap);
    }
}
