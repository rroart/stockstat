package roart.indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import roart.config.MyMyConfig;
import roart.evaluation.MACDRecommend;
import roart.util.TaUtil;

public class IndicatorUtils {

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
        for (int j = conf.getTestRecommendFutureDays(); j < conf.getTableDays(); j += conf.getTestRecommendIntervalDays()) {
            Map<String, Double[]> momMap = new HashMap<>();
            for (String id : listMap.keySet()) {
                Object[] objs = objectMap.get(id);
                Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs, j);
                if (momentum != null) {
                    momMap.put(id, momentum);
                    MACDRecommend.addToLists(macdLists, momentum);
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
        for (int j = conf.getTestRecommendFutureDays(); j < conf.getTableDays(); j += conf.getTestRecommendIntervalDays()) {
            Map<String, Double[]> rsiMap = new HashMap<>();
            for (String id : listMap.keySet()) {
                Object[] objs = objectMap.get(id);
                Double[] rsi = tu.getRsiAndDelta(conf.getRSIDeltaDays(), objs, j);
                if (rsi != null) {
                    rsiMap.put(id, rsi);
                    MACDRecommend.addToLists(rsiLists, rsi);
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
        for (int j = conf.getTestRecommendFutureDays(); j < conf.getTableDays(); j += conf.getTestRecommendIntervalDays()) {
            Map<String, Double[]> momrsiMap = new HashMap<>();
            for (String id : listMacdMap.keySet()) {
                Object[] objsMacd = objectMacdMap.get(id);
                Object[] objsRSI = objectRsiMap.get(id);
                Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objsMacd, j);
                Double[] rsi = tu.getRsiAndDelta(conf.getRSIDeltaDays(), objsRSI, j);
                if (momentum != null) {
    	    Double[] momrsi = ArrayUtils.addAll(momentum, rsi);
                    momrsiMap.put(id, momrsi);
                    MACDRecommend.addToLists(macdrsiLists, momrsi);
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
}
