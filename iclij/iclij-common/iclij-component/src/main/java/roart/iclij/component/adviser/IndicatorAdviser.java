package roart.iclij.component.adviser;

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

import roart.common.inmemory.model.Inmemory;
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.model.IncDecDTO;
import roart.common.model.MetaDTO;
import roart.common.model.util.MetaUtil;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.config.SimulateInvestConfig;

public abstract class IndicatorAdviser extends Adviser {

    protected Map<String, SerialTA> objectMap;
    
    protected boolean indicatorreverse;
    
    protected boolean interpolate;
    
    protected List<MetaDTO> allMetas;
    
    private boolean indicatordirection;
    
    private boolean indicatordirectionup;
    
    public IndicatorAdviser(Market market, LocalDate investStart, LocalDate investEnd, ComponentData param, SimulateInvestConfig simulateConfig) {
        super(market, investStart, investEnd, param, simulateConfig);
        Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getService().getIclijConfig());
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
            MetaDTO meta = new MetaUtil().findMeta(allMetas, market.getConfig().getMarket());
            PipelineData[] resultMaps;
            if (simulateConfig.getIndicatorRebase()) {
                resultMaps = simulateParam.getResultRebaseMaps();
            } else {
                resultMaps = simulateParam.getResultMaps();
            }
            if (resultMaps == null || resultMaps.length == 0) {
                int  jj = 0;
            }
            Integer cat = PipelineUtils.getWantedcat(PipelineUtils.getPipeline(resultMaps, PipelineConstants.META, inmemory));
            String catName = new MetaUtil().getCategory(meta, cat);
            PipelineData objectMaps = PipelineUtils.getPipeline(resultMaps, getPipeline(), inmemory);
            if (objectMaps != null) {
                Map<String, Object> indicatorMaps = (Map<String, Object>) objectMaps.get(getPipeline());
                //System.out.println("macd"+ macdMaps.keySet());
                objectMap = PipelineUtils.getObjectMap(objectMaps);
            }
            return;
        }
        
        Map<String, Object> aMap = new HashMap<>();
        // for improve evolver
        List<MetaDTO> metas = allMetas; //param.getService().getMetas();
        MetaDTO meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        //List<String> categories = new MetaUtil().getCategories(meta);
        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        // don't need these both here and in getevolveml?
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        // TODO  move
	// TODO only two indicators used
        aMap.put(ConfigConstants.INDICATORS, true);
        aMap.put(ConfigConstants.INDICATORSMACD, true);
        aMap.put(ConfigConstants.INDICATORSRSI, true);

	aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        // TODO
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, simulateConfig.getIndicatorRebase());

	// common
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);

	// TODO why
	aMap.put(ConfigConstants.MISCMERGECY, false);
        // TODO todo
	
	// TODO not calling when cached
        //param.getResultMap(null, aMap, false);
        PipelineData[] maps = param.getResultMaps();
        /*
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            String key = entry.getKey();
            System.out.println("key " + key);
            System.out.println("keys " + entry.getValue().keySet());
        }
        */
        Integer cat = PipelineUtils.getWantedcat(PipelineUtils.getPipeline(maps, PipelineConstants.META, inmemory));
        String catName = new MetaUtil().getCategory(meta, cat);
        PipelineData resultMaps = PipelineUtils.getPipeline(maps, getPipeline(), inmemory);
        if (resultMaps != null) {
            //Map<String, Object> indicatorMaps = (Map<String, Object>) resultMaps.get(getPipeline());
            //System.out.println("macd"+ macdMaps.keySet());
            objectMap = PipelineUtils.getObjectMap(resultMaps);
        }
    }

    //@Override
    public List<IncDecDTO> getIncs2(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        Map<String, List<List<Double>>> categoryValueMap;
        if (interpolate) {
            categoryValueMap = param.getFillCategoryValueMap();
        } else {
            categoryValueMap = param.getCategoryValueMap();
        }
        List<Pair<String, Double>> valueList = getValuePairs(categoryValueMap, indexOffset, stockDates, excludes);
        List<IncDecDTO> list = new ArrayList<>();
        for (Pair<String, Double> value : valueList) {
            Double myvalue = value.getValue();
            if (myvalue == null) {
                continue;
            }
            IncDecDTO item = new IncDecDTO();
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

    @Override
    public List<String> getIncs(String aParameter, int buytop, int indexOffset, List<String> stockDates, List<String> excludes) {
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
        IclijConfig config = param.getConfig();
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
            SerialTA indicatorList2 = objectMap.get(entry.getKey());
            if (indicatorList2 == null) {
                continue;
            }
            double[] indicatorList = indicatorList2.getarray(getOffset());
            int end = (Integer) indicatorList2.get(getOffset2());
            int indicatorIndex = indicatorList.length - 1 - indexOffset - end;
            if (indicatorIndex < 0) {
                continue;
            }
            Double indicatorValue = indicatorList[indicatorIndex];
            if (indicatordirection) {
                if (indicatorIndex < 1) {
                    continue;
                }
                Double indicatorPrevValue = indicatorList[indicatorIndex - 1];
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
        IclijConfig config = param.getConfig();
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
                SerialTA indicatorList2 = objectMap.get(entry.getKey());
                if (indicatorList2 == null) {
                    continue;
                }
                double[] indicatorList = indicatorList2.getarray(getOffset());
                int endbuf = (Integer) indicatorList2.get(getOffset2());
                int indicatorIndex = indicatorList.length - 1 - indexOffset - endbuf;
                if (indicatorIndex < 0) {
                    continue;
                }
                Double indicatorValue = indicatorList[indicatorIndex];
                if (indicatordirection) {
                    if (indicatorIndex < 1) {
                        continue;
                    }
                    Double indicatorPrevValue = indicatorList[indicatorIndex - 1];
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

    protected abstract int getOffset();

    protected abstract int getOffset2();
    
    protected abstract String getPipeline();

    private List<MetaDTO> getAllMetas(ComponentData param) {
        return param.getService().getMetas();
    }

    @Override
    public void getValueMap(List<String> stockDates, int firstidx, int lastidx,
            Map<String, List<List<Double>>> categoryValueMap) {
        int start = stockDates.size() - 1 - firstidx;
        int end = stockDates.size() - 1 - lastidx;
        String investStart = stockDates.get(start);
        String investEnd = stockDates.get(end);
        String key = CacheConstants.SIMULATEINVESTADVISER + market.getConfig().getMarket() + this.getClass().getName() + investStart + investEnd + simulateConfig.getIndicatorPure() + indicatordirection + indicatordirectionup + indicatorreverse + interpolate + " " + simulateConfig.getIndicatorRebase();
        valueMap = (Map<Integer, List<Pair<String, Double>>>) MyCache.getInstance().get(key);
        Map<Integer, List<Pair<String, Double>>> newValueMap = null;
        if (valueMap == null || VERIFYCACHE) {
            long time0 = System.currentTimeMillis();
            newValueMap = getValuePairs(categoryValueMap, stockDates, new ArrayList<>(), firstidx, lastidx);
            log.debug("time millis {}", System.currentTimeMillis() - time0);
        }
        if (VERIFYCACHE && valueMap != null) {
            for (Entry<Integer, List<Pair<String, Double>>> entry : newValueMap.entrySet()) {
                int key2 = entry.getKey();
                List<Pair<String, Double>> v2 = entry.getValue();
                List<Pair<String, Double>> v = valueMap.get(key2);
                if (v2 != null && !v2.equals(v)) {
                    log.error("Difference with cache");
                }
            }
        }
        if (valueMap != null) {
            return;
        }
        valueMap = newValueMap;
        MyCache.getInstance().put(key, valueMap);
    }
}

