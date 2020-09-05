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
        if (true) {
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
            categoryValueMap = param.getCategoryValueMap();
        }
    }

    @Override
    public List<IncDecItem> getIncs(String aParameter, int buytop, LocalDate date, int indexOffset, List<String> stockDates, List<String> excludes) {
        //Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        List<Pair<String, Double>> valueList = getValuePairs(categoryValueMap, indexOffset, excludes);
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

}
