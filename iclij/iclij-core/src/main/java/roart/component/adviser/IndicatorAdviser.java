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
        
        if (param instanceof SimulateInvestData) {            
            MetaItem meta = new MetaUtil().findMeta(allMetas, market.getConfig().getMarket());
            Map<String, Map<String, Object>> resultMaps;
            if (simulateConfig.getIndicatorRebase()) {
                resultMaps = simulateParam.getResultRebaseMaps();
            } else {
                resultMaps = simulateParam.getResultMaps();
            }
            Integer cat = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
            String catName = new MetaUtil().getCategory(meta, cat);
            Map<String, Object> objectMaps = resultMaps.get(catName);
            if (objectMaps != null) {
                Map<String, Object> macdMaps = (Map<String, Object>) objectMaps.get(getPipeline());
                //System.out.println("macd"+ macdMaps.keySet());
                objectMap = (Map<String, List<Object>>) macdMaps.get(PipelineConstants.OBJECT);
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
            Map<String, Object> macdMaps = (Map<String, Object>) resultMaps.get(getPipeline());
            //System.out.println("macd"+ macdMaps.keySet());
            objectMap = (Map<String, List<Object>>) macdMaps.get(PipelineConstants.OBJECT);
        }
    }

    @Override
    public List<IncDecItem> getIncs(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
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
        int subListSize = Math.min(buytop, list.size());
        list = list.subList(0, subListSize);
        return list;
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
            List<Object> macdList2 = objectMap.get(entry.getKey());
            List<Double> macdList = (List<Double>) macdList2.get(getOffset());
            int end = (Integer) macdList2.get(getOffset2());
            int macdIndex = macdList.size() - 1 - indexOffset - end;
            if (macdIndex < 0) {
                continue;
            }
            Double macd = macdList.get(macdIndex);
            if (mainList != null) {
                ValidateUtil.validateSizes(mainList, stockDates);
                Double valNow;
                if (simulateConfig.getIndicatorPure()) {
                    valNow = 1.0;
                } else {
                    valNow = mainList.get(mainList.size() - 1 - indexOffset);
                    // mainList.size() - 1 - (stockDates.size() - 1 - TimeUtil.getIndexEqualAfter(stockDates, datestring))
                }
                if (valNow != null && valNow != 0 && macd != null) {
                    Pair<String, Double> value = new ImmutablePair<String, Double>(entry.getKey(), macd / valNow);
                    valueList.add(value);
                }
            }
        }
        sort(valueList);
        return valueList;
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

}

