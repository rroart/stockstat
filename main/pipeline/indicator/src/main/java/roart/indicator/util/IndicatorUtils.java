package roart.indicator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.AbstractCategory;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialMarketStock;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.data.SerialObject;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.etl.db.Extract;
import roart.iclij.config.IclijConfig;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.Indicator;
import roart.indicator.impl.IndicatorATR;
import roart.indicator.impl.IndicatorCCI;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCH;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.ml.model.LearnClassify;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.Calculatable;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.data.ExtraData;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.talib.util.TaUtil;

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
    public static Object[] getDayMomMap(IclijConfig conf, Map<String, SerialTA> objectMap, Map<String, Double[]> listMap,
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
                SerialTA objs = objectMap.get(id);
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
    public static Object[] getDayRsiMap(IclijConfig conf, Map<String, SerialTA> objectMap, Map<String, Double[]> listMap,
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
                SerialTA objs = objectMap.get(id);
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
    public static Object[] getDayMomRsiMap(IclijConfig conf, Map<String, SerialTA> objectMacdMap, Map<String, Double[]> listMacdMap, Map<String, SerialTA> objectRsiMap, Map<String, Double[]> listRsiMap, 
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
                SerialTA objsMacd = objectMacdMap.get(id);
                SerialTA objsRSI = objectRsiMap.get(id);
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
    public static Object[] getDayIndicatorMap(IclijConfig conf, TaUtil tu, List<String> indicators, int futureDays, int tableDays, int intervalDays) throws Exception {
        List<SerialMapTA> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = 0;
        for (String indicator : indicators) {
            PipelineData resultMap = null; //indicator.putData();
            SerialMapTA objMap = PipelineUtils.getMapTA(resultMap);
            if (objMap != null) {
                objectMapsList.add(objMap);
                listList.add(PipelineUtils.sconvertMapDD(resultMap.get(PipelineConstants.LIST)));
                arraySize += 0; // indicator.getResultSize();
                log.debug("Sizes {}", listList.get(listList.size() - 1).size());
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
                result = getCommonResult(conf, indicators, objectMapsList, j, id, result, null, null);
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

    // TODO return stockdate based on date

    // for MLI
    
    public static Object[] getDayIndicatorMap(IclijConfig conf, List<String> indicators, int futureDays, int tableDays, int intervalDays, ExtraData extraData, PipelineData[] datareaders, List<String> dateList, Inmemory inmemory) throws Exception {
        List<SerialMapTA> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = getCommonArraySizeAndObjectMap(conf, indicators, objectMapsList, listList, datareaders, inmemory);
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        if (listList.isEmpty()) {
            return retobj;
        }
        // copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        // TODO get0 may be null
        if (listList != null && listList.get(0) != null) {
        ids.addAll(listList.get(0).keySet());
        }

        List<AbstractIndicator> allIndicators = new ArrayList<>();
        // for extrareader data
        if (extraData != null) {
            arraySize = getExtraDataSize2(conf, extraData, arraySize, allIndicators, inmemory);
        }
        List<Double>[] indicatorLists = new ArrayList[arraySize];
        List<Double>[] indicatorMinMax = new ArrayList[arraySize];
        for (int tmpj = 0; tmpj < arraySize; tmpj ++) {
            indicatorLists[tmpj] = new ArrayList<>();
            indicatorMinMax[tmpj] = new ArrayList<>();
        }

        List<String> commonDates = extraData.dateList;

        // for more extra end
        // change
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        if (tableDays < deltas) {
            deltas = 0;
        }
        for (int j = futureDays; j < tableDays - deltas; j += intervalDays) {
            String commonDate = commonDates.get(commonDates.size() - 1 - j);
            Map<String, Double[]> indicatorMap = new HashMap<>();
            if (listList != null && listList.get(0) != null) {
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                int aJ = dateList.size() - 1 - dateList.indexOf(commonDate);
                result = getCommonResult(conf, indicators, objectMapsList, aJ, id, result, commonDate, datareaders, inmemory);
                if (extraData != null) {
                    // for extrareader data
                    result = ExtraReader.getExtraData(conf, extraData, j, id, result, commonDate);
                    // for extrareader data end
                    // for more extrareader data
                    result = getExtraIndicatorsResult(conf, j, result, extraData.datareaders, allIndicators, commonDate, extraData);
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

    // for ARI
    
    public static Object[] getDayIndicatorMap(IclijConfig conf, List<String> indicators, int futureDays, int tableDays, int intervalDays, ExtraData extraData, PipelineData[] datareaders, Inmemory inmemory) throws Exception {
        List<SerialMapTA> objectMapsList = new ArrayList<>();
        List<Map<String, Double[][]>> listList = new ArrayList<>();
        int arraySize = getCommonArraySizeAndObjectMap(conf, indicators, objectMapsList, listList, datareaders, inmemory);
        Object[] retobj = new Object[3];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = new HashMap<>();
        if (listList.isEmpty()) {
            return retobj;
        }
        // copy the retain from aggregator?
        Set<String> ids = new HashSet<>();
        // TODO get0 may be null
        if (listList != null && listList.get(0) != null) {
        ids.addAll(listList.get(0).keySet());
        }

        List<AbstractIndicator> allIndicators = new ArrayList<>();
        // for extrareader data
        if (extraData != null) {
            arraySize = getExtraDataSize(conf, extraData, arraySize, allIndicators, inmemory);
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
            if (listList != null && listList.get(0) != null) {
            for (String id : listList.get(0).keySet()) {
                Double[] result = new Double[0];
                result = getCommonResult(conf, indicators, objectMapsList, j, id, result, datareaders, inmemory);
                if (extraData != null) {
                    // for extrareader data
                    result = ExtraReader.getExtraData(conf, extraData, j, id, result);
                    // for extrareader data end
                    // for more extrareader data
                    result = getExtraIndicatorsResult(conf, j, result, allIndicators, datareaders, inmemory);
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

    private static void findMinMax(int arraySize, Map dayIndicatorMap,
            List<Double>[] indicatorLists, List<Double>[] indicatorMinMax) {
        try {
            if (!dayIndicatorMap.isEmpty()) {
                for (int i = 0; i < arraySize; i++) {
                    // null in here
                    if (!indicatorLists[i].isEmpty()) {
                        indicatorMinMax[i].add(Collections.min(indicatorLists[i]));
                        indicatorMinMax[i].add(Collections.max(indicatorLists[i]));
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    // for ARI

    private static Double[] getCommonResult(IclijConfig conf, List<String> indicators,
            List<SerialMapTA> objectMapsList, int j, String id, Double[] result, PipelineData[] datareaders, Inmemory inmemory) {
        int idx = 0;
        for (SerialMapTA objectMap : objectMapsList) {
            if (objectMap == null) {
                continue;
            }
            String ind = indicators.get(idx++);
            SerialTA objsIndicator = objectMap.get(id);
            PipelineData pipeline = PipelineUtils.getPipeline(datareaders, ind, inmemory);
            result = appendDayResult(conf, j, result  , pipeline.getName(), objsIndicator);
        }
        return result;
    }
    
    // for MLI

    private static Double[] getCommonResult(IclijConfig conf, List<String> indicators,
            List<SerialMapTA> objectMapsList, int j, String id, Double[] result, String commonDate, PipelineData[] datareaders, Inmemory inmemory) {
        int idx = 0;
        for (SerialMapTA objectMap : objectMapsList) {
            if (objectMap == null) {
                continue;
            }
            String indicatorName = indicators.get(idx++);
            PipelineData meta = PipelineUtils.getPipeline(datareaders, PipelineConstants.META, inmemory);
            PipelineData pipeline = PipelineUtils.getPipeline(datareaders, PipelineUtils.getMetaCat(meta), inmemory);
            List<String> dateList2 = PipelineUtils.getDatelist(pipeline);
            j = dateList2.size() - 1 - dateList2.indexOf(commonDate);
            SerialTA objsIndicator = objectMap.get(id);
            result = appendDayResult(conf, j, result, indicatorName, objsIndicator);
        }
        return result;
    }

    // shared
    
    public static int getCommonArraySizeAndObjectMap(IclijConfig conf, List<String> indicators,
            List<SerialMapTA> objectMapsList, List<Map<String, Double[][]>> listList, PipelineData[] datareaders, Inmemory inmemory) {
        int arraySize = 0;
        for (String indicatorName : indicators) {
            PipelineData meta = PipelineUtils.getPipeline(datareaders, PipelineConstants.META, inmemory);
            PipelineData datareader = PipelineUtils.getPipeline(datareaders, PipelineUtils.getMetaCat(meta), inmemory);
            PipelineData resultMap = PipelineUtils.getPipeline(datareaders, indicatorName, inmemory);
            SerialMapTA objMap = PipelineUtils.getMapTA(resultMap);
            // TODO meta ohlc and indicator.getInputArrays
            // ohlc is false and getinputarrays is 3
            if (objMap.getMap() != null && !objMap.getMap().isEmpty()) { 
                objectMapsList.add(objMap);
                Map<String, Double[][]> list0 = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
                listList.add(list0);
                AbstractIndicator indicator = dummyfactory(conf, indicatorName);
                arraySize += indicator.getResultSize();
                log.info("sizes {}", listList.get(listList.size() - 1).size());
            } else {
                // TODO why? generate exception?
                log.error("Null or empty {}", indicatorName);
                listList.add(null);
            }
        }
        return arraySize;
    }

    // for ARI
    
    private static int getExtraDataSize(IclijConfig conf, ExtraData extraData, int arraySize,
            List<AbstractIndicator> allIndicators, Inmemory inmemory) throws Exception {
        if (extraData.dateList != null) {
            if (!extraData.dateList.isEmpty()) {
                int jj = 0;
            }
            arraySize += 2 * PipelineUtils.getMarketstocks(extraData.extrareader).size();
            log.debug("Sizes {}", arraySize);
        }
        // for extrareader data end
        // for more extra
        getAllIndicators(allIndicators, conf, PipelineUtils.getMarketstocks(extraData.extrareader), extraData.datareaders, inmemory);
        for (AbstractIndicator indicator : allIndicators) {
            int aSize = indicator.getResultSize();
            arraySize += PipelineUtils.getMarketstocks(extraData.extrareader).size() * aSize;
        }
        log.info("listsize {}", extraData.dateList.size());
        return arraySize;
    }

    // for MLI
    
    private static int getExtraDataSize2(IclijConfig conf, ExtraData extraData, int arraySize,
            List<AbstractIndicator> allIndicators, Inmemory inmemory) throws Exception {
        if (extraData.dateList != null) {
            if (!extraData.dateList.isEmpty()) {
                int jj = 0;
            }
            // all from here is already read from eventual inmemory
            Map<String, SerialList<PipelineData>> dataReaderMap = PipelineUtils.getDatareader(extraData.extrareader);
            List<SerialMarketStock> marketStocks = PipelineUtils.getMarketstocks(extraData.extrareader);
            for (SerialMarketStock entry : marketStocks) {
                String market = entry.getMarket();
                SerialList datareaders = dataReaderMap.get(market);
                // ok again
                Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
                String cat = entry.getCategory();
                if (cat == null) {
                    cat = Constants.EXTRA;
                }
                PipelineData datareader = pipelineMap.get(cat);
                if (datareader == null) {
                    datareader = pipelineMap.get(Constants.PRICE);
                    log.debug("TODO temp workaround");
                }
                Map<String, Double[][]> fillListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.FILLLIST));
                if (fillListMap == null) {
                    int jj = 0;
                }
                Double[][] fillList = fillListMap.get(entry.getId());
                // null
                if (fillList == null || fillList[0] == null) {
                	int jj = 0;
                }
                if (fillList != null && Arrays.stream(fillList[0]).anyMatch(Objects::nonNull)) {
                    arraySize += 2;
                } else {
                	int jj = 0;
                }
            }
            //arraySize += 2 * ((Set) extraData.extrareader.getLocalResultMap().get(PipelineConstants.MARKETSTOCKS)).size();
            log.debug("Sizes {}", arraySize);
        }
        // for extrareader data end
        // for more extra
        getAllIndicators(allIndicators, conf, PipelineUtils.getMarketstocks(extraData.extrareader), extraData.datareaders, inmemory);
        int extraSize = getExtraIndicatorsSize(allIndicators, extraData);
        arraySize += extraSize;
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
    
    // for ARI

    private static Double[] getExtraIndicatorsResult(IclijConfig conf, int j, Double[] result, List<AbstractIndicator> allIndicators, PipelineData[] datareaders, Inmemory inmemory) throws Exception {
        for (AbstractIndicator indicator : allIndicators) {
            if (indicator.wantForExtras()) {
                PipelineData localIndicatorResults =  PipelineUtils.getPipeline(datareaders, indicator.indicatorName(), inmemory);
                Map<String, SerialMap<String, SerialTA>> marketObjectMap = PipelineUtils.getMarketObjectMap(localIndicatorResults);
                for (Entry<String, SerialMap<String, SerialTA>> marketEntry : marketObjectMap.entrySet()) {
                    SerialMap<String, SerialTA> objectMap = marketEntry.getValue();
                    for (Entry<String, SerialTA> entry : objectMap.entrySet()) {
                        SerialTA objsIndicator = entry.getValue();
                        result = appendDayResult(conf, j, result, localIndicatorResults.getName(), objsIndicator);
                    }
                }
            }
        }
        return result;
    }

    // for MLI
    
    private static Double[] getExtraIndicatorsResult(IclijConfig conf, int j, Double[] result, PipelineData[] datareaders, List<AbstractIndicator> allIndicators, String commonDate, ExtraData extraData) throws Exception {
        List<SerialMarketStock> marketStocks = PipelineUtils.getMarketstocks(extraData.extrareader);
        Map<String, SerialMarketStock> marketStockMap = new HashMap<>();
        for (SerialMarketStock marketStock : marketStocks) {
            marketStockMap.put(marketStock.getMarket(), marketStock);
        }
        PipelineData localResults =  extraData.extrareader;
        for (AbstractIndicator indicator : allIndicators) {
            if (indicator.wantForExtras()) {
                // this is ok, but rename datareaders
                PipelineData localIndicatorResults =  PipelineUtils.getPipeline(datareaders, indicator.indicatorName());
                // all from here is already read from eventual inmemory
                Map<String, SerialList<PipelineData>> dataReaderMap = PipelineUtils.getDatareader(localResults);
                log.debug("lockeys {}", localResults.keySet());
                //Map<Pair<String, String>, List<StockItem>> pairMap = pairStockMap;
                Map<String, SerialMap<String, SerialTA>> marketObjectMap = PipelineUtils.getMarketObjectMap(localIndicatorResults);
                if (marketObjectMap == null) {
                    continue;
                }
                for (Entry<String, SerialMap<String, SerialTA>> marketEntry : marketObjectMap.entrySet()) {
                    String market = marketEntry.getKey();     
                    SerialList mydatareaders = dataReaderMap.get(market);
                    Map<String, PipelineData> mypipelineMap = getPipelineMap(mydatareaders);

                    SerialMarketStock marketStock = marketStockMap.get(market);
                    String cat = marketStock.getCategory();
                    if (cat == null) {
                        cat = Constants.EXTRA;
                    }

                    PipelineData pipeline = mypipelineMap.get(cat);

                    if (pipeline == null) {
                        pipeline = mypipelineMap.get(Constants.PRICE);
                        log.debug("TODO temp workaround");
                    }

                    List<String> dateList2 = PipelineUtils.getDatelist(pipeline);
                    j = dateList2.size() - 1 - dateList2.indexOf(commonDate);
                    SerialMap<String, SerialTA> objectMap = marketEntry.getValue();
                    for (Entry<String, SerialTA> entry2 : objectMap.entrySet()) {
                        SerialTA objsIndicator = entry2.getValue();
                        PipelineData pipelineData = PipelineUtils.getPipeline(datareaders, indicator.indicatorName());
                        result = appendDayResult(conf, j, result, pipelineData.getName(), objsIndicator);
                    }
                }
            }
        }
        return result;
    }

    private static int getExtraIndicatorsSize(List<AbstractIndicator> allIndicators, ExtraData extraData) throws Exception {
        //DbAccess dbDao = DbDao.instance(conf);
        int size = 0;
        //Set<MarketStock> allMarketStocks = (Set<MarketStock>) extraData.extrareader.get(PipelineConstants.MARKETSTOCKS);
        for (AbstractIndicator indicator : allIndicators) {
            Map<String, Map<String, double[][]>> marketListMap = ((Indicator)indicator).getMarketListMap(extraData.extrareader);
            if (indicator.wantForExtras()) {
                int aSize = indicator.getResultSize();
                for (Entry<String, Map<String, double[][]>> entry : marketListMap.entrySet()) {
                    String market = entry.getKey();
                    Map<String, double[][]> myTruncListMap = entry.getValue();
                    Map<String, Double[][]> newMyTruncListMap = ((Indicator)indicator).convert(myTruncListMap);
                    if (!indicator.anythingHere(newMyTruncListMap)) {
                        continue;
                    }
                    //List<Map> resultList = getMarketCalcResults(dbDao, myTruncListMap);
                    //if (resultList == null || resultList.isEmpty()) {
                    //    continue;
                    //}
                    size += aSize * myTruncListMap.size();                
                }
            }
        }
        return size;
    }

    private static Double[] appendDayResult(IclijConfig conf, int j, Double[] result, String indicator, SerialTA objsIndicator) {
        Object[] arr = dummyfactory(conf, indicator).getDayResult(objsIndicator, j);
        if (arr != null && arr.length > 0) {
            result = (Double[]) ArrayUtils.addAll(result, arr);
        } else {
            int jj = 0;
        }
        return result;
    }

    // shared
    
    private static void getAllIndicators(List<AbstractIndicator> allIndicators, IclijConfig conf, List<SerialMarketStock> marketStocks,
            PipelineData[] datareaders, Inmemory inmemory) throws Exception {
        Set<String> indicatorSet = new HashSet<>();
        for (SerialMarketStock pairEntry : marketStocks) {
            String market = pairEntry.getMarket();
            String cat = pairEntry.getCategory();
            if (market.equals(conf.getConfigData().getMarket())) {
                 if (cat == null) {
                    cat = Constants.EXTRA;
                }
                if (cat == null) {
                    int jj = 0;
                }
                // ok to use as long as only with keys and no values
                Map<String, PipelineData> indicatorMap = PipelineUtils.getPipelineMapStartsWith(datareaders, PipelineConstants.INDICATOR);
                for (Entry<String, PipelineData> entry : indicatorMap.entrySet()) {

                    AbstractIndicator indicator = dummyfactory(conf, entry.getKey());
                    if (indicator.wantForExtras()) {
                        allIndicators.add(indicator);
                        indicatorSet.add(entry.getKey());
                    }
                }
            }
        }
        // make indicator factory
        if (indicatorSet.add(PipelineConstants.INDICATORMACD) && conf.wantAggregatorsIndicatorExtrasMACD()) {
            allIndicators.add(new IndicatorMACD(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORRSI) && conf.wantAggregatorsIndicatorExtrasRSI()) {
            allIndicators.add(new IndicatorRSI(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORATR) && conf.wantAggregatorsIndicatorExtrasATR()) {
            allIndicators.add(new IndicatorATR(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORCCI) && conf.wantAggregatorsIndicatorExtrasCCI()) {
            allIndicators.add(new IndicatorCCI(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORSTOCH) && conf.wantAggregatorsIndicatorExtrasSTOCH()) {
            allIndicators.add(new IndicatorSTOCH(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
        if (indicatorSet.add(PipelineConstants.INDICATORSTOCHRSI) && conf.wantAggregatorsIndicatorExtrasSTOCHRSI()) {
            allIndicators.add(new IndicatorSTOCHRSI(conf, null, null, Constants.NOCOLUMN, datareaders, true, inmemory));       
        }
    }

    public static AbstractIndicator dummyfactory(IclijConfig conf, String indicator) {
        Inmemory inmemory = null;
        if (indicator.equals(PipelineConstants.INDICATORMACD)) {
            return new IndicatorMACD(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        if (indicator.equals(PipelineConstants.INDICATORRSI)) {
            return new IndicatorRSI(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        if (indicator.equals(PipelineConstants.INDICATORATR)) {
            return new IndicatorATR(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        if (indicator.equals(PipelineConstants.INDICATORCCI)) {
            return new IndicatorCCI(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        if (indicator.equals(PipelineConstants.INDICATORSTOCH)) {
            return new IndicatorSTOCH(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        if (indicator.equals(PipelineConstants.INDICATORSTOCHRSI)) {
            return new IndicatorSTOCHRSI(conf, null, null, Constants.NOCOLUMN, null, true, inmemory);       
        }
        return null;

    }
    
    public static void addToLists(List<Double> macdLists[], Double[] momentum) throws Exception {
        for (int i = 0; i < macdLists.length; i ++) {
            List<Double> macdList = macdLists[i];
            if (momentum[i] != null) {
                macdList.add(momentum[i]);
            }
        }
    }

    @Deprecated
    public static Map<String, PipelineData> getPipelineMap(PipelineData[] datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
    }

    public static Map<String, PipelineData> getPipelineMap(SerialList<SerialObject> datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (SerialObject object : datareaders.getList()) {
            PipelineData datareader = (PipelineData) object;
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
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

    public static void filterNonExistingClassifications4(Map<Double, String> labelMapShort, List<LearnClassify> list) {
        log.info("Values " + list);
        // due to tensorflow l classifying to 3rd (not inc dec)
        List<LearnClassify> filterNonExistingClassifications = new ArrayList<>();
        for (LearnClassify entry : list) {
            Double value = (Double) entry.getClassification();
            if (labelMapShort.get(value) == null) {
                filterNonExistingClassifications.add(entry);
            }
        }
        for (LearnClassify key : filterNonExistingClassifications) {
            list.remove(key);
            log.error("Removing key {}", key);
        }
    }

    public static Map<String, SerialTA> doCalculationsArrNonNull(IclijConfig conf, Map<String, double[][]> listMap, String key, Calculatable indicator, boolean wantPercentizedPriceIndex) {
        Map<String, SerialTA> objectMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            double [][] list = listMap.get(id);
            if ("F00000HGSN".equals(id)) {              
                log.debug("braz " + Arrays.toString(list));                
            }
            /*
           if (wantPercentizedPriceIndex && list.length > 0 && list[0].length > 0) {
               double first = list[0][0];
               for(int i = 0; i < list.length; i ++)
                list[i] = ArraysUtil.getPercentizedPriceIndex(list[i], key, indicator.getCategory(), first);
            }
           */
           if ("2647727".equals(id)) {              
               log.debug("braz " + Arrays.toString(list));                
           }
            log.debug("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.debug("list " + list.length + " " + Arrays.asList(list));
            //double momentum = tu.getMom(list, conf.getDays());
            if (list.length == 180) {
                log.debug("180");
            } else {
                log.debug("not");
            }
            if (list[0].length == 0) {
                //continue;
            }
            SerialTA objs = indicator.calculate(list);
            if ("F00000HGSN".equals(id)) {
                log.debug("braz " + Arrays.asList(list));
            }
            objectMap.put(id, objs);
        }
        return objectMap;
    }
    
    public Pipeline[] getDataReaders(IclijConfig conf, String[] periodText, Map<String, MarketData> marketdatamap,
            StockData stockData, Map<String, StockData> stockDataMap, ExtraReader extraReader) throws Exception {
        Pipeline[] datareaders = new Pipeline[Constants.PERIODS + 3];
        datareaders[0] = new DataReader(conf, marketdatamap, Constants.INDEXVALUECOLUMN, conf.getConfigData().getMarket());
        datareaders[1] = new DataReader(conf, marketdatamap, Constants.PRICECOLUMN, conf.getConfigData().getMarket());
        extraDataReadData(conf, marketdatamap, stockData, extraReader, stockDataMap);
        datareaders[2] = extraReader;
        for (int i = 0; i < Constants.PERIODS; i++) {
            datareaders[i + 3] = new DataReader(conf, marketdatamap, i, conf.getConfigData().getMarket());
        }
        return datareaders;
    }

    private void extraDataReadData(IclijConfig conf, Map<String, MarketData> marketdatamap, StockData stockData,
            ExtraReader extraReader, Map<String, StockData> stockDataMap) throws Exception {
        Map<String, Pipeline[]> dataReaderMap;
        dataReaderMap = new HashMap<>();
        for (String market : extraReader.getMarkets()) {
            StockData stockData2 = stockDataMap.get(market);
            Pipeline[] datareaders2 = extraReader.getDataReaders(conf, stockData.periodText,
                    stockData2.marketdatamap, market);
            dataReaderMap.put(market, datareaders2);
        }
        extraReader.readData(conf, marketdatamap, 0, stockData, stockDataMap, dataReaderMap);
    }

    public Map<String, StockData> getExtraStockDataMap(IclijConfig conf, DbDao dbDao,ExtraReader extraReader) {
        Map<String, StockData> stockDataMap;
        stockDataMap = new HashMap<>();
        for (String market : extraReader.getMarkets()) {
            StockData stockData2 = new Extract(dbDao).getStockData(conf, market);
            stockDataMap.put(market, stockData2);
        }
        return stockDataMap;
    }
    
    public Map<String, StockData> getExtraStockDataMap(IclijConfig conf, MyDataSource dbDao,ExtraReader extraReader) {
        Map<String, StockData> stockDataMap;
        stockDataMap = new HashMap<>();
        for (String market : extraReader.getMarkets()) {
            StockData stockData2 = new Extract(dbDao).getStockData(conf, market);
            stockDataMap.put(market, stockData2);
        }
        return stockDataMap;
    }
    
    public PipelineData getMetadata(IclijConfig conf, StockData stockData) {
        PipelineData singlePipelineData = new PipelineData();
        singlePipelineData.setName(PipelineConstants.META);
        MetaItem meta = stockData.marketdatamap.get(conf.getConfigData().getMarket()).meta;
        singlePipelineData.put(PipelineConstants.META, new SerialMeta(meta.getMarketid(), meta.getPeriod(), meta.getPriority(), meta.getReset(), meta.isLhc()));
        singlePipelineData.put(PipelineConstants.CATEGORY, stockData.catName);
        singlePipelineData.put(PipelineConstants.WANTEDCAT, stockData.cat);
        singlePipelineData.put(PipelineConstants.NAME, new SerialMapPlain(stockData.idNameMap));
        singlePipelineData.put(PipelineConstants.DATELIST, new SerialListPlain(stockData.stockdates));
        return singlePipelineData;
    }

    public PipelineData[] createPipelineDataCategories(PipelineData[] pipelinedata, List<AbstractCategory> categories,
            StockData stockData) {
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            if (stockData.catName.equals(categories.get(i).getTitle())) {
                for (Entry<String, AbstractIndicator> entry : categories.get(i).getIndicatorMap().entrySet()) {
                    PipelineData singlePipelinedata = entry.getValue().putData();
                    pipelinedata = ArrayUtils.add(pipelinedata, singlePipelinedata);
                }
            }
        }
        return pipelinedata;
    }

    public List<StockItem> getDayStocks(IclijConfig conf, StockData stockData) {
        String mydate = TimeUtil.format(conf.getConfigData().getDate());
        int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
        if (dateIndex >= 0) {
            mydate = stockData.stockdates.get(dateIndex);
        }
        return stockData.stockdatemap.get(mydate);
    }

    public PipelineData[] createPipelineAggregators(PipelineData[] pipelinedata, List<Aggregator> aggregates) {
        /*
        aggregates.addAll(Arrays.asList(getAggregates(conf, stockData.periodText,
                stockData.marketdatamap, categories.toArray(new AbstractCategory[0]), pipelinedata , disableList, stockData.catName, stockData.cat, stockData.stockdates)));
        */
        
        // add all indicators aggregates
        for (int i = 0; i < aggregates.size(); i++) {
            if (!aggregates.get(i).isEnabled()) {
                continue;
            }
            log.debug("ag {}", aggregates.get(i).getName());
            PipelineData singlePipelinedata = aggregates.get(i).putData();
            pipelinedata = ArrayUtils.add(pipelinedata, singlePipelinedata);
        }
        return pipelinedata;
    }

    public PipelineData[] createDatareaderPipelineData(IclijConfig conf, PipelineData[] pipelinedata,
            StockData stockData, Pipeline[] datareaders) {
        PipelineData singlePipelineData = getMetadata(conf, stockData);
    
        pipelinedata = ArrayUtils.add(pipelinedata, singlePipelineData);
    
        for (Pipeline datareader : datareaders) {
            pipelinedata = ArrayUtils.add(pipelinedata, datareader.putData());
        }
        return pipelinedata;
    }

}
