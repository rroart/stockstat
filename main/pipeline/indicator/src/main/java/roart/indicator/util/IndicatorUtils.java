package roart.indicator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorATR;
import roart.indicator.impl.IndicatorCCI;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCH;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.data.ExtraData;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.util.TaUtil;
import java.util.Objects;

public class IndicatorUtils {

    protected static Logger log = LoggerFactory.getLogger(IndicatorUtils.class);

    /**
     * 
     * Get an array with two arrays
     * First a map from day to related macd map from id to values
     * Second a map from day to all macd values
     * 
     * @param conf Main config, not evolution
     * @param objectMap map from stock id to macd map
     * @param listMap map from stock id to values
     * @param tu
     * @return
     * @throws Exception
     */

    @Deprecated
    public static Object[] getDayMomMap(MyMyConfig conf, Map<String, Object[]> objectMap, Map<String, Double[]> listMap,
            TaUtil tu) throws Exception {
        Object[] retobj = new Object[2];
        Map<Integer, Map<String, Double[]>> dayMomMap = new HashMap<>();
        Map<Integer, List<Double>[]> dayMacdsMap = new HashMap<>();
        List<Double> macdLists[] = new ArrayList[4];
        List<Double> macdMinMax[] = new ArrayList[4];
        for (int tmpj = 0; tmpj < 4; tmpj ++) {
            macdLists[tmpj] = new ArrayList<>();
            macdMinMax[tmpj] = new ArrayList<>();
        }
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < conf.getTableDays(); j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            Map<String, Double[]> momMap = new HashMap<>();
            for (String id : listMap.keySet()) {
                Object[] objs = objectMap.get(id);
                Double[] momentum = tu.getWithThreeAndDelta(conf.getMACDHistogramDeltaDays(), conf.getMACDDeltaDays(), conf.getMACDSignalDeltaDays(), objs, j);
                if (momentum != null) {
                    momMap.put(id, momentum);
                    IndicatorUtils.addToLists(macdLists, momentum);
                }
            }
            dayMomMap.put(j, momMap);
            dayMacdsMap.put(j, macdLists);
        }
        macdMinMax[0].add(Collections.min(macdLists[0]));
        macdMinMax[0].add(Collections.max(macdLists[0]));
        macdMinMax[1].add(Collections.min(macdLists[1]));
        macdMinMax[1].add(Collections.max(macdLists[1]));
        retobj[0] = dayMomMap;
        retobj[1] = macdMinMax;
        return retobj;
    }

    @Deprecated
    public static Object[] getDayRsiMap(MyMyConfig conf, Map<String, Object[]> objectMap, Map<String, Double[]> listMap,
            TaUtil tu) throws Exception {
        Object[] retobj = new Object[2];
        Map<Integer, Map<String, Double[]>> dayRsiMap = new HashMap<>();
        List<Double> rsiLists[] = new ArrayList[2];
        List<Double> rsiMinMax[] = new ArrayList[2];
        for (int tmpj = 0; tmpj < 2; tmpj ++) {
            rsiLists[tmpj] = new ArrayList<>();
            rsiMinMax[tmpj] = new ArrayList<>();
        }
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < conf.getTableDays(); j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            Map<String, Double[]> rsiMap = new HashMap<>();
            for (String id : listMap.keySet()) {
                Object[] objs = objectMap.get(id);
                Double[] rsi = tu.getWithOneAndDelta(conf.getRSIDeltaDays(), objs, j);
                if (rsi != null) {
                    rsiMap.put(id, rsi);
                    IndicatorUtils.addToLists(rsiLists, rsi);
                }
            }
            dayRsiMap.put(j, rsiMap);
        }
        rsiMinMax[0].add(Collections.min(rsiLists[0]));
        rsiMinMax[0].add(Collections.max(rsiLists[0]));
        rsiMinMax[1].add(Collections.min(rsiLists[1]));
        rsiMinMax[1].add(Collections.max(rsiLists[1]));
        retobj[0] = dayRsiMap;
        retobj[1] = rsiMinMax;
        return retobj;
    }

    @Deprecated
    public static Object[] getDayMomRsiMap(MyMyConfig conf, Map<String, Object[]> objectMacdMap, Map<String, Double[]> listMacdMap, Map<String, Object[]> objectRsiMap, Map<String, Double[]> listRsiMap, 
            TaUtil tu) throws Exception {
        Object[] retobj = new Object[2];
        Map<Integer, Map<String, Double[]>> dayMomRsiMap = new HashMap<>();
        List<Double>[] macdrsiLists = new ArrayList[6];
        List<Double>[] macdrsiMinMax = new ArrayList[6];
        for (int tmpj = 0; tmpj < 4 + 2; tmpj ++) {
            macdrsiLists[tmpj] = new ArrayList<>();
            macdrsiMinMax[tmpj] = new ArrayList<>();
        }
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < conf.getTableDays(); j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            Map<String, Double[]> momrsiMap = new HashMap<>();
            for (String id : listMacdMap.keySet()) {
                Object[] objsMacd = objectMacdMap.get(id);
                Object[] objsRSI = objectRsiMap.get(id);
                Double[] momentum = tu.getWithThreeAndDelta(conf.getMACDHistogramDeltaDays(), conf.getMACDDeltaDays(), conf.getMACDSignalDeltaDays(), objsMacd, j);
                Double[] rsi = tu.getWithOneAndDelta(conf.getRSIDeltaDays(), objsRSI, j);
                if (momentum != null) {
                    Double[] momrsi = ArrayUtils.addAll(momentum, rsi);
                    momrsiMap.put(id, momrsi);
                    IndicatorUtils.addToLists(macdrsiLists, momrsi);
                }
            }
            dayMomRsiMap.put(j, momrsiMap);
        }
        for (int i = 0; i < 4 + 2; i++) {
            macdrsiMinMax[i].add(Collections.min(macdrsiLists[i]));
            macdrsiMinMax[i].add(Collections.max(macdrsiLists[i]));
        }
        retobj[0] = dayMomRsiMap;
        retobj[1] = macdrsiMinMax;
        return retobj;
    }

    @Deprecated
    public static Object[] getDayIndicatorMap(MyMyConfig conf, TaUtil tu, List<AbstractIndicator> indicators, int futureDays, int tableDays, int intervalDays) throws Exception {
        List<Map<String, Object[]>> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = 0;
        for (AbstractIndicator indicator : indicators) {
            Map<String, Object> resultMap = indicator.getLocalResultMap();
            Map<String, Object[]> objMap = (Map<String, Object[]>) resultMap.get(PipelineConstants.OBJECT);
            if (objMap != null) {
                objectMapsList.add(objMap);
                listList.add((Map<String, Double[][]>) resultMap.get(PipelineConstants.LIST));
                arraySize += indicator.getResultSize();
                System.out.println("sizes " + listList.get(listList.size() - 1).size());
            }
        }
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        List<Double>[] indicatorLists = new ArrayList[arraySize];
        List<Double>[] indicatorMinMax = new ArrayList[arraySize];
        for (int tmpj = 0; tmpj < arraySize; tmpj ++) {
            indicatorLists[tmpj] = new ArrayList<>();
            indicatorMinMax[tmpj] = new ArrayList<>();
        }
        if (listList.isEmpty()) {
            return retobj;
        }
        // copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        ids.addAll(listList.get(0).keySet());



























        // change
        for (int j = futureDays; j < tableDays; j += intervalDays) {
            Map<String, Double[]> indicatorMap = new HashMap<>();
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                result = getCommonResult(indicators, objectMapsList, j, id, result);










                if (result.length == arraySize) {
                    indicatorMap.put(id, result);
                    IndicatorUtils.addToLists(indicatorLists, result);
                    log.info("outid {} {}", id, Arrays.asList(result));
                } else {
                    continue;
                }
                dayIndicatorMap.put(j, indicatorMap);
            }
        }
        findMinMax(arraySize, dayIndicatorMap, indicatorLists, indicatorMinMax);
        retobj[0] = dayIndicatorMap;
        retobj[1] = indicatorMinMax;
        retobj[2] = listList;
        return retobj;
    }

    public static Object[] getDayIndicatorMap(MyMyConfig conf, TaUtil tu, List<AbstractIndicator> indicators, int futureDays, int tableDays, int intervalDays, ExtraData extraData, Pipeline[] datareaders) throws Exception {
        List<Map<String, Object[]>> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = getCommonArraySizeAndObjectMap(indicators, objectMapsList, listList, datareaders);
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        if (listList.isEmpty()) {
            return retobj;
        }
        // copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        ids.addAll(listList.get(0).keySet());

        List<AbstractIndicator> allIndicators = new ArrayList<>();
        // for extrareader data
        if (extraData != null) {
            arraySize = getExtraDataSize(conf, extraData, arraySize, allIndicators);
        }
        List<Double>[] indicatorLists = new ArrayList[arraySize];
        List<Double>[] indicatorMinMax = new ArrayList[arraySize];
        for (int tmpj = 0; tmpj < arraySize; tmpj ++) {
            indicatorLists[tmpj] = new ArrayList<>();
            indicatorMinMax[tmpj] = new ArrayList<>();
        }

        // for more extra end
        // change
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        if (tableDays < deltas) {
            deltas = 0;
        }
        for (int j = futureDays; j < tableDays - deltas; j += intervalDays) {
            Map<String, Double[]> indicatorMap = new HashMap<>();
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                result = getCommonResult(indicators, objectMapsList, j, id, result);
                if (extraData != null) {
                    // for extrareader data
                    result = ExtraReader.getExtraData(conf, extraData.dateList, extraData.pairDateMap, extraData.pairCatMap, j, id, result);
                    // for extrareader data end
                    // for more extrareader data
                    result = getExtraIndicatorsResult(extraData.categories, j, result, extraData.pairDateMap, extraData.pairCatMap, extraData.datareaders, allIndicators);
                    // for more extrareader data end
                }
                if (result.length == arraySize && Arrays.stream(result).allMatch(i -> !Double.isNaN(i))) {
                    indicatorMap.put(id, result);
                    IndicatorUtils.addToLists(indicatorLists, result);
                    log.debug("outid {} {}", id, Arrays.asList(result));
                } else {
                    if (result.length == arraySize) {
                        int jj = 0;
                    }
                    int jj = 0;
                    continue;
                }
            }
            dayIndicatorMap.put(j, indicatorMap);
        }
        findMinMax(arraySize, dayIndicatorMap, indicatorLists, indicatorMinMax);
        retobj[0] = dayIndicatorMap;
        retobj[1] = indicatorMinMax;
        retobj[2] = listList;
        if (indicatorMinMax == null || indicatorMinMax.length == 1) {
            int jj = 0;
        }
        if (dayIndicatorMap == null || dayIndicatorMap.isEmpty()) {
            int jj = 0;
        }
        return retobj;
    }

    private static void findMinMax(int arraySize, Map<Integer, Map<String, Double[]>> dayIndicatorMap,
            List<Double>[] indicatorLists, List<Double>[] indicatorMinMax) {
        try {
        if (!dayIndicatorMap.isEmpty()) {
            for (int i = 0; i < arraySize; i++) {
                indicatorMinMax[i].add(Collections.min(indicatorLists[i]));
                indicatorMinMax[i].add(Collections.max(indicatorLists[i]));
            }
        }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    private static Double[] getCommonResult(List<AbstractIndicator> indicators, List<Map<String, Object[]>> objectMapsList,
            int j, String id, Double[] result) {
        int idx = 0;
        for (Map<String, Object[]> objectMap : objectMapsList) {
            AbstractIndicator ind = indicators.get(idx++);
            Object[] objsIndicator = objectMap.get(id);
            result = appendDayResult(j, result, ind, objsIndicator);
        }
        return result;
    }

    private static int getCommonArraySizeAndObjectMap(List<AbstractIndicator> indicators, List<Map<String, Object[]>> objectMapsList,
            List<Map<String, Double[][]>> listList, Pipeline[] datareaders) {
        int arraySize = 0;
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        for (AbstractIndicator indicator : indicators) {
            DataReader datareader = (DataReader) pipelineMap.get("" + indicator.getCategory());
            Map<String, Object> resultMap = indicator.getLocalResultMap();
            Map<String, Object[]> objMap = (Map<String, Object[]>) resultMap.get(PipelineConstants.OBJECT);
            if (objMap != null) { 
                objectMapsList.add(objMap);
                Map<String, Double[][]> list0 = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
                listList.add(list0);
                arraySize += indicator.getResultSize();
                log.info("sizes {}", listList.get(listList.size() - 1).size());
            }
        }
        return arraySize;
    }

    private static int getExtraDataSize(MyMyConfig conf, ExtraData extraData, int arraySize,
            List<AbstractIndicator> allIndicators) throws Exception {
        if (extraData.pairDateMap != null) {
            if (!extraData.pairDateMap.isEmpty()) {
                int jj = 0;
            }
            arraySize += 2 * extraData.pairDateMap.keySet().size();
            System.out.println("sizes " + arraySize);
        }
        // for extrareader data end
        // for more extra
        getAllIndicators(allIndicators, conf, extraData.categories, extraData.pairCatMap, extraData.datareaders);
        for (AbstractIndicator indicator : allIndicators) {
            int aSize = indicator.getResultSize();
            arraySize += extraData.pairCatMap.size() * aSize;
        }
        log.info("listsize {}", extraData.dateList.size());
        return arraySize;
    }

    public static int getCategoryFromString(Map<Pair<String, String>, String> pairCatMap, Pair<String, String> pairKey) {
        String categoryString = pairCatMap.get(pairKey);
        int category = 0;
        switch (categoryString) {
        case Constants.PRICE:
            category = Constants.PRICECOLUMN;
            break;
        case Constants.INDEX:
            category = Constants.INDEXVALUECOLUMN;
            break;
        }
        return category;
    }

    private static Double[] getExtraIndicatorsResult(AbstractCategory[] categories, int j, Double[] result, Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap, Map<Pair<String, String>, String> pairCatMap, Pipeline[] datareaders, List<AbstractIndicator> allIndicators) throws Exception {
        for (AbstractIndicator indicator : allIndicators) {
            if (indicator.wantForExtras()) {
                Map<String, Object> localIndicatorResults =  indicator.getLocalResultMap();
                Map<String, Map<String, Object[]>> marketObjectMap = (Map<String, Map<String, Object[]>>) localIndicatorResults.get(PipelineConstants.MARKETOBJECT);
                for (Entry<String, Map<String, Object[]>> marketEntry : marketObjectMap.entrySet()) {
                    Map<String, Object[]> objectMap = marketEntry.getValue();
                    for (Entry<String, Object[]> entry : objectMap.entrySet()) {
                        Object[] objsIndicator = entry.getValue();
                        result = appendDayResult(j, result, indicator, objsIndicator);
                    }
                }
            }
        }
        return result;
    }

    private static Double[] appendDayResult(int j, Double[] result, AbstractIndicator indicator, Object[] objsIndicator) {
        Object[] arr = indicator.getDayResult(objsIndicator, j);
        if (arr != null && arr.length > 0) {
            result = (Double[]) ArrayUtils.addAll(result, arr);
        }
        return result;
    }

    private static void getAllIndicators(List<AbstractIndicator> allIndicators, MyMyConfig conf, AbstractCategory[] categories,
            Map<Pair<String, String>, String> pairCatMap, Pipeline[] datareaders) throws Exception {
        Set<String> indicatorSet = new HashSet<>();
        for (Entry<Pair<String, String>, String> pairEntry : pairCatMap.entrySet()) {
            String market = pairEntry.getKey().getLeft();
            String cat = pairEntry.getValue();
            if (market.equals(conf.getMarket())) {
                for (AbstractCategory category : categories) {
                    if (cat.equals(category.getTitle())) {
                        Map<String, AbstractIndicator> indicatorMap = category.getIndicatorMap();
                        for (Entry<String, AbstractIndicator> entry : indicatorMap.entrySet()) {
                            
                            AbstractIndicator indicator = entry.getValue();
                            if (indicator.wantForExtras()) {
                                allIndicators.add(indicator);
                                indicatorSet.add(entry.getKey());
                            }
                        }
                    }
                }
            }
        }
        // make indicator factory
        if (indicatorSet.add(PipelineConstants.INDICATORMACD) && conf.wantAggregatorsIndicatorExtrasMACD()) {
            allIndicators.add(new IndicatorMACD(conf, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORRSI) && conf.wantAggregatorsIndicatorExtrasRSI()) {
            allIndicators.add(new IndicatorRSI(conf, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORATR) && conf.wantAggregatorsIndicatorExtrasATR()) {
            allIndicators.add(new IndicatorATR(conf, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORCCI) && conf.wantAggregatorsIndicatorExtrasCCI()) {
            allIndicators.add(new IndicatorCCI(conf, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORSTOCH) && conf.wantAggregatorsIndicatorExtrasSTOCH()) {
            allIndicators.add(new IndicatorSTOCH(conf, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORSTOCHRSI) && conf.wantAggregatorsIndicatorExtrasSTOCHRSI()) {
            allIndicators.add(new IndicatorSTOCHRSI(conf, null, null, 42, datareaders, true));       
        }
    }

    public static void addToLists(List<Double> macdLists[], Double[] momentum) throws Exception {
        for (int i = 0; i < macdLists.length; i ++) {
            List<Double> macdList = macdLists[i];
            if (momentum[i] != null) {
                macdList.add(momentum[i]);
            }
        }
    }

    public static void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdLists[], String market, Double[] momentum) throws Exception {
        for (int i = 0; i < macdLists.length; i ++) {
            List<Double> macdList = macdLists[i];
            if (momentum[i] != null) {
                macdList.add(momentum[i]);
            }
        }
    }

    public static Map<String, Pipeline> getPipelineMap(Pipeline[] datareaders) {
        Map<String, Pipeline> pipelineMap = new HashMap<>();
        for (Pipeline datareader : datareaders) {
            pipelineMap.put(datareader.pipelineName(), datareader);
        }
        return pipelineMap;
    }

    public static AbstractCategory getWantedCategory(AbstractCategory[] categories, MetaItem meta) throws Exception {
        String[] defaultPriorities = { Constants.PRICE, Constants.INDEX };
        String[] priorities;
        String priority = null;
        if (meta != null) {
            priority = meta.getPriority();
        }
        if (priority != null) {
            priorities = priority.split(",");            
        } else {
            priorities = defaultPriorities;
        }
        AbstractCategory cat = null;
        for (String aPriority : priorities) {
            for (AbstractCategory category : categories) {
                if (category.getTitle().equals(aPriority) && category.hasContent()) {
                    return category;
                }
            }
        }
        return cat;
    }

    public static AbstractCategory getWantedCategory(AbstractCategory[] categories, int cat) throws Exception {
        for (AbstractCategory category : categories) {
            if (cat == category.getPeriod()) {
                return category;
            }
        }
        return null;
    }

    public static Integer getWantedCategory(List<StockItem> stocks, PeriodData periodData) throws Exception {
        if (StockUtil.hasStockValue(stocks, Constants.PRICECOLUMN)) {
            return Constants.PRICECOLUMN;
        }
        if (StockUtil.hasStockValue(stocks, Constants.INDEXVALUECOLUMN)) {
            return Constants.INDEXVALUECOLUMN;
        }
        if (periodData == null) {
            return null;
        }
        Set<Pair<String, Integer>> pairs = periodData.pairs;
        for (Pair<String, Integer> pair : pairs) {
            int cat = (int) pair.getRight();
            if (StockUtil.hasStockValue(stocks, cat)) {
                return cat;
            }
        }
        return null;
    }

    public static Integer getWantedCategory(List<StockItem> stocks, MetaItem meta) throws Exception {
        Integer[] defaultPris = { Constants.PRICECOLUMN, Constants.INDEXVALUECOLUMN };
        String[] defaultPriorities = { Constants.PRICE, Constants.INDEX };
        String[] priorities;
        String priority = null;
        if (meta != null) {
            priority = meta.getPriority();
        }
        if (priority != null) {
            priorities = priority.split(",");            
        } else {
            priorities = defaultPriorities;
        }
        for (String aPriority : priorities) {
            for (int i = 0; i < defaultPriorities.length; i++) {
                if (defaultPriorities[i].equals(aPriority)) {
                    if (StockUtil.hasStockValue(stocks, defaultPris[i])) {
                        return defaultPris[i];
                    }
                }
            }
            String[] periods = new String[0];
            if (meta != null) {
                periods = meta.getPeriod();
            }
            for (int i = 0; i < periods.length; i++) {
                if (aPriority.equals(periods[i])) {
                    if (StockUtil.hasStockValue(stocks, i)) {
                        return i;
                    }                    
                }
            }
        }
        return null;
    }

    public static void filterNonExistingClassifications(Map<Double, String> labelMapShort,
            Map<String, Double[]> classifyResult) {
        log.info("Values " + classifyResult.values());
        // due to tensorflow l classifying to 3drd (not inc dec)
        List<String> filterNonExistingClassifications = new ArrayList<>();
        for (Entry<String, Double[]> entry : classifyResult.entrySet()) {
            Double[] value = entry.getValue();
            if (labelMapShort.get(value[0]) == null) {
                filterNonExistingClassifications.add(entry.getKey());
            }
        }
        for (String key : filterNonExistingClassifications) {
            classifyResult.remove(key);
            log.error("Removing key {}", key);
        }
    }

    public static void filterNonExistingClassifications2(Map labelMapShort, Map map) {
        log.info("Values " + map.values());
        // due to tensorflow l classifying to 3rd (not inc dec)
        List<Object> filterNonExistingClassifications = new ArrayList<>();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (labelMapShort.get(value) == null) {
                filterNonExistingClassifications.add(key);
            }
        }
        for (Object key : filterNonExistingClassifications) {
            map.remove(key);
            log.error("Removing key {}", key);
        }
    }

    public static void filterNonExistingClassifications3(Map<Double, String> labelMapShort, Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap) {
        //log.info("Values " + learnMap.values());
        // due to tensorflow l classifying to 3rd (not inc dec)
        for (Entry<String, List<Pair<double[], Pair<Object, Double>>>> entry : learnMap.entrySet()) {
            String key = entry.getKey();
            List<Object> filterNonExistingClassifications = new ArrayList<>();
            List<Pair<double[], Pair<Object, Double>>> list = entry.getValue();
            for (Pair<double[], Pair<Object, Double>> elem : list) {
                Double value = elem.getRight().getRight();
                if (labelMapShort.get(value) == null) {
                    filterNonExistingClassifications.add(elem);
                }
            }
            if (!filterNonExistingClassifications.isEmpty()) {
                list.removeAll(filterNonExistingClassifications);
                log.error("Removing key {} {}", key, filterNonExistingClassifications);
            }
        }
    }

    public static void filterNonExistingClassifications4(Map<Double, String> labelMapShort, List<Triple<String, Object, Double>> list) {
        log.info("Values " + list);
        // due to tensorflow l classifying to 3rd (not inc dec)
        List<Triple<String, Object, Double>> filterNonExistingClassifications = new ArrayList<>();
        for (Triple<String, Object, Double> entry : list) {
            Double value = entry.getRight();
            if (labelMapShort.get(value) == null) {
                filterNonExistingClassifications.add(entry);
            }
        }
        for (Triple<String, Object, Double> key : filterNonExistingClassifications) {
            list.remove(key);
            log.error("Removing key {}", key);
        }
    }

}
