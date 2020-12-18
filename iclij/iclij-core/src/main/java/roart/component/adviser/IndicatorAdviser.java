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

import roart.common.constants.Constants;
import roart.common.cache.MyCache;
import roart.common.config.ConfigConstants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.MetaUtil;
import roart.common.util.ValidateUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.IncDecItem;

public abstract class IndicatorAdviser extends Adviser {

    protected Map<String, List<Object>> objectMap;
    
    protected boolean indicatorreverse;
    
    protected boolean interpolate;
    
    protected List<MetaItem> allMetas;
    
    private boolean indicatordirection;
    
    private boolean indicatordirectionup;
    
    public IndicatorAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
        SimulateInvestData simulateParam;
        if (param instanceof SimulateInvestData) {
            simulateParam = (SimulateInvestData) param;
        } else {
            simulateParam = new SimulateInvestData(param);
        }
        if (simulateParam.getAllMetas() != null) {
            allMetas = simulateParam.getAllMetas();
        } else {
            allMetas = getAllMetas(param);
        }

        indicatorreverse = simulateConfig.getIndicatorReverse();
        
        interpolate = simulateConfig.getInterpolate();
        
        indicatordirection = simulateConfig.getIndicatorDirection();
        
        indicatordirectionup = simulateConfig.getIndicatorDirectionUp();
        
        if (param instanceof SimulateInvestData) {            
            MetaItem meta = new MetaUtil().findMeta(allMetas, market.getConfig().getMarket());
            Map<String, Map<String, Object>> resultMaps;
            if (simulateConfig.getIndicatorRebase()) {
                resultMaps = simulateParam.getResultRebaseMaps();
            } else {
                resultMaps = simulateParam.getResultMaps();
            }
            if (resultMaps == null || resultMaps.isEmpty()) {
                int jj = 0;
            }
            Integer cat = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
            String catName = new MetaUtil().getCategory(meta, cat);
            Map<String, Object> objectMaps = resultMaps.get(catName);
            if (objectMaps != null) {
                Map<String, Object> indicatorMaps = (Map<String, Object>) objectMaps.get(getPipeline());
                //System.out.println("macd"+ macdMaps.keySet());
                objectMap = (Map<String, List<Object>>) indicatorMaps.get(PipelineConstants.OBJECT);
            }
            return;
        }
        
        Map<String, Object> aMap = new HashMap<>();
        // for improve evolver
        List<MetaItem> metas = allMetas; //param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        //List<String> categories = new MetaUtil().getCategories(meta);
        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        // don't need these both here and in getevolveml?
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, true);
        aMap.put(ConfigConstants.INDICATORSMACD, true);
        aMap.put(ConfigConstants.INDICATORSRSI, true);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, simulateConfig.getIndicatorRebase());
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        param.getResultMap(null, aMap);
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        /*
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            String key = entry.getKey();
            System.out.println("key " + key);
            System.out.println("keys " + entry.getValue().keySet());
        }
        */
        Integer cat = (Integer) maps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        String catName = new MetaUtil().getCategory(meta, cat);
        Map<String, Object> resultMaps = maps.get(catName);
        if (resultMaps != null) {
            Map<String, Object> indicatorMaps = (Map<String, Object>) resultMaps.get(getPipeline());
            //System.out.println("macd"+ macdMaps.keySet());
            objectMap = (Map<String, List<Object>>) indicatorMaps.get(PipelineConstants.OBJECT);
        }
    }

    //@Override
    public List<IncDecItem> getIncs2(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        Map<String, List<List<Double>>> categoryValueMap;
        if (interpolate) {
            categoryValueMap = param.getFillCategoryValueMap();
        } else {
            categoryValueMap = param.getCategoryValueMap();
        }
        List<Pair<String, Double>> valueList = getValuePairs(categoryValueMap, indexOffset, stockDates, excludes);
        List<IncDecItem> list = new ArrayList<>();
        for (Pair<String, Double> value : valueList) {
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

    //@Override
    public List<String> getIncs(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        int idx = stockDates.size() - 1 - indexOffset;
        if (idx < 0) {
            return new ArrayList<>();
        }
        List<Pair<String, Double>> valueList = valueMap.get(idx);
        if (valueList == null) {
            return new ArrayList<>();
        }
        return valueList.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    private List<Pair<String, Double>> getValuePairs(Map<String, List<List<Double>>> categoryValueMap, int indexOffset, List<String> stockDates, List<String> excludes) {
        List<Pair<String, Double>> valueList = new ArrayList<>();
        IclijConfig config = param.getInput().getConfig();
        for(Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            if (excludes.contains(entry.getKey())) {
                continue;
            }
            if ("1302814".equals(entry.getKey())) {
                int jj = 0;
            }
            List<List<Double>> resultList = entry.getValue();
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            //Double[] macdList = calculatedMap.get(entry.getKey());
            List<Object> indicatorList2 = objectMap.get(entry.getKey());
            if (indicatorList2 == null) {
                continue;
            }
            List<Double> indicatorList = (List<Double>) indicatorList2.get(getOffset());
            int end = (Integer) indicatorList2.get(getOffset2());
            int indicatorIndex = indicatorList.size() - 1 - indexOffset - end;
            if (indicatorIndex < 0) {
                continue;
            }
            Double indicatorValue = indicatorList.get(indicatorIndex);
            if (indicatordirection) {
                if (indicatorIndex < 1) {
                    continue;
                }
                Double indicatorPrevValue = indicatorList.get(indicatorIndex - 1);
                if (indicatorValue == null || indicatorPrevValue == null) {
                    continue;
                }
                if (!indicatordirectionup == indicatorValue > indicatorPrevValue) {
                    continue;
                }
            }
            if (mainList != null) {
                //ValidateUtil.validateSizes(mainList, stockDates);
                Double valNow;
                if (simulateConfig.getIndicatorPure()) {
                    valNow = 1.0;
                } else {
                    valNow = mainList.get(mainList.size() - 1 - indexOffset);
                    // mainList.size() - 1 - (stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring))
                }
                if (valNow != null && valNow != 0 && indicatorValue != null) {
                    Pair<String, Double> value = new ImmutablePair<String, Double>(entry.getKey(), indicatorValue / valNow);
                    valueList.add(value);
                }
            }
        }
        sort(valueList);
        return valueList;
    }

    private Map<Integer, List<Pair<String, Double>>> getValuePairs(Map<String, List<List<Double>>> categoryValueMap, List<String> stockDates, List<String> excludes, int firstidx, int lastidx) {
        Map<Integer, List<Pair<String, Double>>> valueMap = new HashMap<>();
        IclijConfig config = param.getInput().getConfig();
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
                //Double[] macdList = calculatedMap.get(entry.getKey());
                List<Object> indicatorList2 = objectMap.get(entry.getKey());
                if (indicatorList2 == null) {
                    continue;
                }
                List<Double> indicatorList = (List<Double>) indicatorList2.get(getOffset());
                int endbuf = (Integer) indicatorList2.get(getOffset2());
                int indicatorIndex = indicatorList.size() - 1 - indexOffset - endbuf;
                if (indicatorIndex < 0) {
                    continue;
                }
                Double indicatorValue = indicatorList.get(indicatorIndex);
                if (indicatordirection) {
                    if (indicatorIndex < 1) {
                        continue;
                    }
                    Double indicatorPrevValue = indicatorList.get(indicatorIndex - 1);
                    if (indicatorValue == null || indicatorPrevValue == null) {
                        continue;
                    }
                    if (!indicatordirectionup == indicatorValue > indicatorPrevValue) {
                        continue;
                    }
                }
                if (mainList != null) {
                    //ValidateUtil.validateSizes(mainList, stockDates);
                    Double valNow;
                    if (simulateConfig.getIndicatorPure()) {
                        valNow = 1.0;
                    } else {
                        valNow = mainList.get(mainList.size() - 1 - indexOffset);
                        // mainList.size() - 1 - (stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring))
                    }
                    if (valNow != null && valNow != 0 && indicatorValue != null) {
                        Pair<String, Double> value = new ImmutablePair<String, Double>(entry.getKey(), indicatorValue / valNow);
                        valueList.add(value);
                    }
                }
            }
            valueMap.put(i, valueList);
        }
        for (Entry<Integer, List<Pair<String, Double>>> entry : valueMap.entrySet()) {
            List<Pair<String, Double>> valueList = entry.getValue();
        }
        return valueMap;
    }

    protected void sort(List<Pair<String, Double>> valueList) {
        valueList.sort(Comparator.comparing(Pair::getValue));
        if (indicatorreverse) {
            Collections.reverse(valueList);
        }
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
    
    protected abstract int getOffset();

    protected abstract int getOffset2();
    
    protected abstract String getPipeline();

    private List<MetaItem> getAllMetas(ComponentData param) {
        return param.getService().getMetas();
    }

    @Override
    public void getValueMap(List<String> stockDates, int firstidx, int lastidx,
            Map<String, List<List<Double>>> categoryValueMap) {
        String key = "ADVISERVALUEMAP" + market.getConfig().getMarket() + this.getClass().getName() + firstidx + "_" + lastidx;
        valueMap = (Map<Integer, List<Pair<String, Double>>>) MyCache.getInstance().get(key);
        if (valueMap != null) {
            return;
        }
        valueMap = getValuePairs(categoryValueMap, stockDates, new ArrayList<>(), firstidx, lastidx);                
        MyCache.getInstance().put(key, valueMap);
    }
}

