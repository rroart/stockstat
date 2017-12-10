package roart.indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.aggregate.ExtraReader;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.MyMyConfig;
import roart.model.StockItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.TaUtil;

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
                Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs, j);
                if (momentum != null) {
                    momMap.put(id, momentum);
                    IndicatorUtils.addToLists(macdLists, momentum);
                } else {
                    //System.out.println("No macd for id" + id);
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
                Double[] rsi = tu.getRsiAndDelta(conf.getRSIDeltaDays(), objs, j);
                if (rsi != null) {
                    rsiMap.put(id, rsi);
                    IndicatorUtils.addToLists(rsiLists, rsi);
                } else {
                    //System.out.println("No macd for id" + id);
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

    public static Object[] getDayMomRsiMap(MyMyConfig conf, Map<String, Object[]> objectMacdMap, Map<String, Double[]> listMacdMap, Map<String, Object[]> objectRsiMap, Map<String, Double[]> listRsiMap, 
            TaUtil tu) throws Exception {
        Object[] retobj = new Object[2];
        Map<Integer, Map<String, Double[]>> dayMomRsiMap = new HashMap<>();
        List<Double> macdrsiLists[] = new ArrayList[6];
        List<Double> macdrsiMinMax[] = new ArrayList[6];
        for (int tmpj = 0; tmpj < 4 + 2; tmpj ++) {
            macdrsiLists[tmpj] = new ArrayList<>();
            macdrsiMinMax[tmpj] = new ArrayList<>();
        }
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < conf.getTableDays(); j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            Map<String, Double[]> momrsiMap = new HashMap<>();
            for (String id : listMacdMap.keySet()) {
                Object[] objsMacd = objectMacdMap.get(id);
                Object[] objsRSI = objectRsiMap.get(id);
                Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objsMacd, j);
                Double[] rsi = tu.getRsiAndDelta(conf.getRSIDeltaDays(), objsRSI, j);
                if (momentum != null) {
                    Double[] momrsi = ArrayUtils.addAll(momentum, rsi);
                    momrsiMap.put(id, momrsi);
                    IndicatorUtils.addToLists(macdrsiLists, momrsi);
                } else {
                    //System.out.println("No macd for id" + id);
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

    public static Object[] getDayIndicatorMap(MyMyConfig conf, TaUtil tu, List<Indicator> indicators, int futureDays, int tableDays, int intervalDays) throws Exception {
        List<Map<String, Object[]>> objectMapsList = new ArrayList<>();
        List<Map<String, Double[]>> listList = new ArrayList<>();
        int arraySize = 0;
        for (Indicator indicator : indicators) {
            Map<String, Object> resultMap = indicator.getLocalResultMap();
            System.out.println("r " + resultMap.keySet() + " " + resultMap.get(PipelineConstants.OBJECT));
            Map<String, Object[]> objMap = (Map<String, Object[]>) resultMap.get(PipelineConstants.OBJECT);
            if (objMap != null) {
                objectMapsList.add(objMap);
                listList.add((Map<String, Double[]>) resultMap.get(PipelineConstants.LIST));
                arraySize += indicator.getResultSize();
                System.out.println("sizes " + listList.get(listList.size() - 1).size());
            }
        }
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        List<Double> indicatorLists[] = new ArrayList[arraySize];
        List<Double> indicatorMinMax[] = new ArrayList[arraySize];
        for (int tmpj = 0; tmpj < arraySize; tmpj ++) {
            indicatorLists[tmpj] = new ArrayList<>();
            indicatorMinMax[tmpj] = new ArrayList<>();
        }
        // TODO copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        if (listList.isEmpty()) {
            return new Object[3];
        }
        ids.addAll(listList.get(0).keySet());

        // TODO change
        for (int j = futureDays; j < tableDays; j += intervalDays) {
            Map<String, Double[]> indicatorMap = new HashMap<>();
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                int idx = 0;
                for (Map<String, Object[]> objectMap : objectMapsList) {
                    Indicator ind = indicators.get(idx++);
                    Object[] objsIndicator = objectMap.get(id);
                    Object[] arr = ind.getDayResult(objsIndicator, j);
                    if (arr != null && arr.length > 0) {
                        result = (Double[]) ArrayUtils.addAll(result, arr);
                    } else {
                        //System.out.println("No obj for id1 " + id);
                    }
                }
                if (result.length == arraySize) {
                    indicatorMap.put(id, result);
                    IndicatorUtils.addToLists(indicatorLists, result);
               } else {
                    //System.out.println("discarding " + id);
                    continue;
                }
                dayIndicatorMap.put(j, indicatorMap);
            }
        }
        for (int i = 0; i < arraySize; i++) {
            indicatorMinMax[i].add(Collections.min(indicatorLists[i]));
            indicatorMinMax[i].add(Collections.max(indicatorLists[i]));
        }
        retobj[0] = dayIndicatorMap;
        retobj[1] = indicatorMinMax;
        retobj[2] = listList;
        return retobj;
    }

    public static Object[] getDayIndicatorMap(MyMyConfig conf, TaUtil tu, List<Indicator> indicators, int futureDays, int tableDays, int intervalDays, List<Date> dateList, Map<Pair, List<StockItem>> pairStockMap, Map<Pair, Map<Date, StockItem>> pairDateMap, int category, Map<Pair, String> pairCatMap, Category[] categories, Pipeline[] datareaders) throws Exception {
        List<Map<String, Object[]>> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = 0;
        for (Indicator indicator : indicators) {
            Map<String, Object> resultMap = indicator.getLocalResultMap();
            objectMapsList.add((Map<String, Object[]>) resultMap.get(PipelineConstants.OBJECT));
            listList.add((Map<String, Double[][]>) resultMap.get(PipelineConstants.LIST));
            arraySize += indicator.getResultSize();
            System.out.println("sizes " + listList.get(listList.size() - 1).size());
        }
        int extraStart = arraySize;
        // for extrareader data
        if (pairDateMap != null) {
            arraySize += 2 * pairDateMap.keySet().size();
            System.out.println("sizes " + arraySize);
        }
        // for extrareader data end
        // for more extra
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        ExtraReader extrareader = (ExtraReader) pipelineMap.get(PipelineConstants.EXTRAREADER);
        //extrareader.setPa
        Map<Pair, Double[][]> retMap = extrareader.getExtraData2(conf, dateList, pairDateMap, pairCatMap, 0, null, null);
        List<Indicator> allIndicators = getAllIndicators(conf, categories, pairCatMap, datareaders);
        for (Indicator indicator : allIndicators) {
            int aSize = indicator.getResultSize();
            arraySize += pairCatMap.size() * aSize;
        }

        // for more extra end
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        List<Double> indicatorLists[] = new ArrayList[arraySize];
        List<Double> indicatorMinMax[] = new ArrayList[arraySize];
        for (int tmpj = 0; tmpj < arraySize; tmpj ++) {
            indicatorLists[tmpj] = new ArrayList<>();
            indicatorMinMax[tmpj] = new ArrayList<>();
        }
        // TODO copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        if (!listList.isEmpty()) {
        ids.addAll(listList.get(0).keySet());
        }
        
        log.info("listsize " + dateList.size());
        // TODO change
        for (int j = futureDays; j < tableDays; j += intervalDays) {
            Map<String, Double[]> indicatorMap = new HashMap<>();
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                int idx = 0;
                for (Map<String, Object[]> objectMap : objectMapsList) {
                    Indicator ind = indicators.get(idx++);
                    Object[] objsIndicator = objectMap.get(id);
                    Object[] arr = ind.getDayResult(objsIndicator, j);
                    if (arr != null && arr.length > 0) {
                        result = (Double[]) ArrayUtils.addAll(result, arr);
                     } else {
                        //System.out.println("No obj for id2 " + id);
                    }
                }
                // for extrareader data
                result = ExtraReader.getExtraData(conf, dateList, pairDateMap, pairCatMap, j, id, result);
                // for extrareader data end
                // for more extrareader data
                result = getExtraIndicatorsResult(conf, categories, j, result, pairDateMap, pairCatMap, datareaders, allIndicators);
                // for more extrareader data end
                if (result.length == arraySize) {
                    indicatorMap.put(id, result);
                    IndicatorUtils.addToLists(indicatorLists, result);
                    log.info("outid " + id + " " + Arrays.asList(result));
               } else {
                    //System.out.println("discarding " + id);
                    continue;
                }
                dayIndicatorMap.put(j, indicatorMap);
            }
        }
        if (!dayIndicatorMap.isEmpty()) {
        for (int i = 0; i < arraySize; i++) {
            indicatorMinMax[i].add(Collections.min(indicatorLists[i]));
            indicatorMinMax[i].add(Collections.max(indicatorLists[i]));
        }
        }
        retobj[0] = dayIndicatorMap;
        retobj[1] = indicatorMinMax;
        retobj[2] = listList;
        return retobj;
    }

    public static int getCategoryFromString(Map<Pair, String> pairCatMap, Pair pairKey) {
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

    private static Double[] getExtraIndicatorsResult(MyMyConfig conf, Category[] categories, int j, Double[] result, Map<Pair, Map<Date, StockItem>> pairDateMap, Map<Pair, String> pairCatMap, Pipeline[] datareaders, List<Indicator> allIndicators) throws Exception {
       for (Indicator indicator : allIndicators) {
            if (indicator.wantForExtras()) {
                Map<String, Object> localIndicatorResults =  indicator.getLocalResultMap();
                Map<String, Map<String, Object[]>> marketObjectMap = (Map<String, Map<String, Object[]>>) localIndicatorResults.get(PipelineConstants.MARKETOBJECT);
                //Map<String, Map<String, Object[]>> marketResultMap = (Map<String, Map<String, Object[]>>) localIndicatorResults.get(PipelineConstants.MARKETRESULT);
                //Map<String, Map<String, Double[]>> marketMomMap = (Map<String, Map<String, Double[]>>) localIndicatorResults.get(PipelineConstants.MARKETCALCULATED);
                for (String market : marketObjectMap.keySet()) {
                    Map<String, Object[]> objectMap = marketObjectMap.get(market);
                    for (String localId : objectMap.keySet()) {
                        Object[] objsIndicator = objectMap.get(localId);
                        Object[] arr = indicator.getDayResult(objsIndicator, j);
                        if (arr != null && arr.length > 0) {
                            result = (Double[]) ArrayUtils.addAll(result, arr);
                        } else {
                            //System.out.println("No obj for id3 " + localId);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static List<Indicator> getAllIndicators(MyMyConfig conf, Category[] categories,
            Map<Pair, String> pairCatMap, Pipeline[] datareaders) throws Exception {
        List<Indicator> allIndicators = new ArrayList<>();
        Set<Pair> marketcatSet = new HashSet<>();
        Set<String> indicatorSet = new HashSet<>();
        for (Pair pair : pairCatMap.keySet()) {
            String market = (String) pair.getFirst();
            String id = (String) pair.getSecond();
            String cat = pairCatMap.get(pair);
            if (market.equals(conf.getMarket())) {
                for (Category category : categories) {
                    if (cat.equals(category.getTitle())) {
                        Map<String, Indicator> indicatorMap = category.getIndicatorMap();
                        for (String indicatorName : indicatorMap.keySet()) {
                            Indicator indicator = indicatorMap.get(indicatorName);
                            if (indicator.wantForExtras()) {
                                allIndicators.add(indicator);
                                indicatorSet.add(indicatorName);
                            }
                        }
                    }
                }
            }
        }
        //else {
        //   Pair marketcat = new Pair(market, cat);
        //if (marketcatSet.add(marketcat)) {
        //if (indicatorSet.add(marketcat)) {
        // TODO make indicator factory
        //Indicator indicator = null;
        if (indicatorSet.add(PipelineConstants.INDICATORMACD) && conf.wantAggregatorsIndicatorExtrasMACD()) {
            allIndicators.add(new IndicatorMACD(conf, null, null, null, null, null, 42, datareaders, true));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORRSI) && conf.wantAggregatorsIndicatorExtrasRSI()) {
            allIndicators.add(new IndicatorRSI(conf, null, null, null, null, null, 42, datareaders, true));       
        }
        //           allIndicators.add(indicator);
        //        }
        //    }
        //}
        return allIndicators;
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
        {
            for (int i = 0; i < macdLists.length; i ++) {
                List<Double> macdList = macdLists[i];
                if (momentum[i] != null) {
                    macdList.add(momentum[i]);
                }
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

    public static Category getWantedCategory(Category[] categories) throws Exception {
        List<String> wantedList = new ArrayList<>();
           wantedList.add(CategoryConstants.PRICE);
           wantedList.add(CategoryConstants.INDEX);
           wantedList.add("cy");
            Category cat = null;
            for (String wanted : wantedList) {
                for (Category category : categories) {
                    System.out.println(category.getTitle());
                    if (cat == null && category.hasContent()) {
                        if (category.getTitle().equals(wanted)) {
                            cat = category;
                            break;
                        }
                    }
                }
            }
        return cat;
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
        for (Pair pair : pairs) {
            int cat = (int) pair.getSecond();
            if (StockUtil.hasStockValue(stocks, cat)) {
                return cat;
            }
        }
        return null;
    }

}
