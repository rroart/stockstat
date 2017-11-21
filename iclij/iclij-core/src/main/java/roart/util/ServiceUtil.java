package roart.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.config.ConfigConstants;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.TradeMarket;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.service.IclijServiceResult;

public class ServiceUtil {
    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    private final static String TP = "TP";
    private final static String TN = "TN";
    private final static String FP = "FP";
    private final static String FN = "FN";

    private static String INC = "Inc";
    private static String DEC = "Dec";
    
    public static void doRecommender(String market, Integer offset, String aDate, boolean doSave) throws Exception {
        long time0 = System.currentTimeMillis();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int futuredays = (int) srv.conf.getTestIndicatorRecommenderComplexFutureDays();
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        //System.out.println("da " + + futuredays + " " + baseDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        Date baseDate = dt.parse(baseDateStr);
        Date futureDate = dt.parse(futureDateStr);

        srv.conf.setdate(baseDate);
        srv.getTestRecommender(true);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> maps = srv.getContent();
        Map recommendMaps = maps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //System.out.println("m3 " + recommendMaps.keySet());
        if (recommendMaps == null) {
            return;
        }
        Integer category = (Integer) recommendMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) recommendMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, Map<String, List<Double>>> resultMap = (Map<String, Map<String, List<Double>>>) recommendMaps.get(PipelineConstants.RESULT);
        //System.out.println("m4 " + resultMap.keySet());
        if (resultMap == null) {
            return;
        }
        Map<String, List<Double>> recommendBuySell = resultMap.get("complex");
        //System.out.println("m5 " + recommendBuySell.keySet());
        Set<Double> buyset = new HashSet<>();
        Set<Double> sellset = new HashSet<>();
        for (String key : recommendBuySell.keySet()) {
            List<Double> vals = recommendBuySell.get(key);
            if (vals.get(0) != null) {
                buyset.add(vals.get(0));
            }
            if (vals.get(1) != null) {
                sellset.add(vals.get(1));
            }
        }
        double buyMedian = median(buyset);
        double sellMedian = median(sellset);

        srv.conf.setdate(futureDate);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        calculateRecommender(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell, buyMedian,
                sellMedian, result, categoryValueMap, usedsec, doSave);
        try {
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
    }
    private static void calculateRecommender(String market, int futuredays, Date baseDate, Date futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, double buyMedian, double sellMedian,
            Map<String, Map<String, Object>> result, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave) throws Exception {
        Set<Double> changeSet = new HashSet<>();
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                if (valFuture != null && valNow != null) {
                    Double change = valFuture / valNow;
                    changeSet.add(change);
                }
            }
        }
        Double medianChange = median(changeSet);
        long goodBuy = 0;
        long goodSell = 0;
        long totalBuy = 0;
        long totalSell = 0;
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            Double change = null;
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                if (valFuture != null && valNow != null) {
                    change = valFuture / valNow;
                }
            }
            if (change == null) {
                continue;
            }
            List<Double> vals = recommendBuySell.get(key);
            if (vals == null) {
                continue;
            }
            Double buy = vals.get(0);
            Double sell = vals.get(1);
            if (buy != null) {
                totalBuy++;
                if (buy > buyMedian && change > medianChange) {
                    goodBuy++;
                }
            }
            if (sell != null) {
                totalSell++;
                if (sell > sellMedian && change < medianChange) {
                    goodSell++;
                }
            }
        }      
        MemoryItem buyMemory = new MemoryItem();
        buyMemory.setMarket(market);
        buyMemory.setRecord(new Date());
        buyMemory.setDate(baseDate);
        buyMemory.setUsedsec(usedsec);
        buyMemory.setFuturedays(futuredays);
        buyMemory.setFuturedate(futureDate);
        buyMemory.setComponent(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        buyMemory.setSubcomponent("buy");
        buyMemory.setCategory(categoryTitle);
        buyMemory.setPositives(goodBuy);
        buyMemory.setSize(totalBuy);
        buyMemory.setConfidence((double) goodBuy / totalBuy);
        if (doSave) {
            buyMemory.save();
        }
        MemoryItem sellMemory = new MemoryItem();
        sellMemory.setMarket(market);
        sellMemory.setRecord(new Date());
        sellMemory.setDate(baseDate);
        sellMemory.setUsedsec(usedsec);
        sellMemory.setFuturedays(futuredays);
        sellMemory.setFuturedate(futureDate);
        sellMemory.setComponent(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        sellMemory.setSubcomponent("sell");
        sellMemory.setCategory(categoryTitle);
        sellMemory.setPositives(goodSell);
        sellMemory.setSize(totalSell);
        sellMemory.setConfidence((double) goodSell / totalSell);
        if (doSave) {
            sellMemory.save();
        }
        //System.out.println("testing buy " + goodBuy + " " + totalBuy + " sell " + goodSell + " " + totalSell);
        //System.out.println("k3 " + categoryValueMap.get("VIX"));
        //System.out.println(result.get("Index").keySet());
        System.out.println(buyMemory);
        System.out.println(sellMemory);
    }
    public static void doPredict(String market, Integer offset, String aDate, boolean doSave) throws Exception {
        long time0 = System.currentTimeMillis();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int futuredays = (int) srv.conf.getPredictorLSTMHorizon();
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        //System.out.println("da " + + futuredays + " " + baseDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);

        Date baseDate = dt.parse(baseDateStr);
        Date futureDate = dt.parse(futureDateStr);
        srv.conf.setdate(baseDate);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        Map<String, Map<String, Object>> result0 = srv.getContent();

        Map<String, Map<String, Object>> maps = result0;
        if (maps == null) {
            return;
        }
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        String wantedCat = getWantedCategory(maps, PipelineConstants.LSTM);
        if (wantedCat == null) {
            return;
        }
        Map map = (Map) maps.get(wantedCat).get(PipelineConstants.LSTM);
        
        //System.out.println("lstm " + map.keySet());
        Integer category = (Integer) map.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) map.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) map.get(PipelineConstants.RESULT);
        srv.conf.setdate(futureDate);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        calculatePredictor(market, futuredays, baseDate, futureDate, categoryTitle, resultMap, categoryValueMap, usedsec, doSave);
        try {
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
    }
    private static void calculatePredictor(String market, int futuredays, Date baseDate, Date futureDate,
            String categoryTitle, Map<String, List<Double>> resultMap,
            Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave) throws Exception {
        long total = 0;
        long goodInc = 0;
        long goodDec = 0;
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                List<Double> predFutureList = resultMap.get(key);
                if (predFutureList == null) {
                    continue;
                }
                Double predFuture = predFutureList.get(0);
                if (valFuture != null && valNow != null && predFuture != null) {
                    total++;
                    if (valFuture > valNow && predFuture > valNow) {
                        goodInc++;
                    }
                    if (valFuture < valNow && predFuture < valNow) {
                        goodDec++;
                    }
                }
            }
        }
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
        MemoryItem incMemory = new MemoryItem();
        incMemory.setMarket(market);
        incMemory.setRecord(new Date());
        incMemory.setDate(baseDate);
        incMemory.setUsedsec(usedsec);
        incMemory.setFuturedays(futuredays);
        incMemory.setFuturedate(futureDate);
        incMemory.setComponent(ConfigConstants.PREDICTORS);
        incMemory.setSubcomponent("inc");
        incMemory.setCategory(categoryTitle);
        incMemory.setPositives(goodInc);
        incMemory.setSize(total);
        incMemory.setConfidence((double) goodInc / total);
        if (doSave) {
            incMemory.save();
        }
        MemoryItem decMemory = new MemoryItem();
        decMemory.setMarket(market);
        decMemory.setRecord(new Date());
        decMemory.setDate(baseDate);
        decMemory.setUsedsec(usedsec);
        decMemory.setFuturedays(futuredays);
        decMemory.setFuturedate(futureDate);
        decMemory.setComponent(ConfigConstants.PREDICTORS);
        decMemory.setSubcomponent("dec");
        decMemory.setCategory(categoryTitle);
        decMemory.setPositives(goodDec);
        decMemory.setSize(total);
        decMemory.setConfidence((double) goodDec / total);
        if (doSave) {
            decMemory.save();
        }
        System.out.println(incMemory);
        System.out.println(decMemory);
    }

    public static void doMLMACD(String market, Integer offset, String aDate, boolean doSave) throws ParseException {
        long time0 = System.currentTimeMillis();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int daysafterzero = (int) srv.conf.getMACDDaysAfterZero();
        String baseDateStr = stocks.get(stocks.size() - 1 - 1 * daysafterzero - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - 0 * daysafterzero - offset);
        //System.out.println("da " + + daysafterzero + " " + baseDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        Date baseDate = dt.parse(baseDateStr);
        Date futureDate = dt.parse(futureDateStr);

        srv.conf.setdate(baseDate);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);
        Map<String, Map<String, Object>> result0 = srv.getContent();

        Map<String, Map<String, Object>> maps = result0;
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        Map mlMACDMaps = (Map) maps.get(PipelineConstants.MLMACD);
        //System.out.println("mlm " + mlMACDMaps.keySet());
        Integer category = (Integer) mlMACDMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlMACDMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //System.out.println("m4 " + resultMetaArray);
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        srv.conf.setdate(futureDate);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            // TODO add more offset
            // TODO verify dates and offsets
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            calculateMLMACD(market, daysafterzero, baseDate, futureDate, categoryTitle, resultMap, resultMetaArray,
                    categoryValueMap, resultMeta, offset, usedsec, doSave);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
    }
    private static void calculateMLMACD(String market, int daysafterzero, Date baseDate, Date futureDate,
            String categoryTitle, Map<String, List<Object>> resultMap, List<List> resultMetaArray,
            Map<String, List<List<Double>>> categoryValueMap, List<ResultMeta> resultMeta, int offset, Integer usedsec, boolean doSave) throws Exception {
        int resultIndex = 0;
        int count = 0;
        for (List meta : resultMetaArray) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.get(2);
            Double testaccuracy = (Double) meta.get(6);
            Map<String, List<Double>> offsetMap = (Map<String, List<Double>>) meta.get(8);
            /*
            Map<String, Integer> countMapLearn = (Map<String, Integer>) meta.get(5);
            Map<String, Integer> countMapClass = (Map<String, Integer>) meta.get(7);
            */
            Map<String, Integer> countMapLearn = (Map<String, Integer>) resultMeta.get(count).getLearnMap();
            Map<String, Integer> countMapClass = (Map<String, Integer>) resultMeta.get(count).getClassifyMap();
            long total = 0;
            long goodTP = 0;
            long goodFP = 0;
            long goodTN = 0;
            long goodFN = 0;
            long tpSize = 0;
            long fpSize = 0;
            long tnSize = 0;
            long fnSize = 0;
            double goodTPprob = 0;
            double goodFPprob = 0;
            double goodTNprob = 0;
            double goodFNprob = 0;
            int size = resultMap.values().iterator().next().size();
            for (String key : categoryValueMap.keySet()) {
                if (key.equals("VIX")) {
                    int jj = 0;
                }
                List<List<Double>> resultList = categoryValueMap.get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList == null) {
                    continue;
                }
                List<Object> list = resultMap.get(key);
                if (list == null) {
                    continue;
                }
                String tfpn = (String) list.get(resultIndex);
                if (tfpn == null) {
                    continue;
                }
                List<Double> off = offsetMap.get(key);
                if (off == null) {
                    log.error("The offset should not be null for " + key);
                    continue;
                }
                int offsetZero = (int) Math.round(off.get(0));
                Double valFuture = mainList.get(mainList.size() - 1 - offset - offsetZero);
                Double valNow = mainList.get(mainList.size() - 1 - daysafterzero - offset - offsetZero);
                Double tfpnProb = null;
                if (returnSize > 1) {
                    tfpnProb = (Double) list.get(resultIndex + 1);
                }
                if (valFuture != null && valNow != null) {
                    //System.out.println("vals " + key + " " + valNow + " " + valFuture);
                    total++;
                    if (tfpn.equals(TP)) {
                        tpSize++;
                        if (valFuture > valNow ) {
                            goodTP++;
                            if (returnSize > 1) {
                                goodTPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(FP)) {
                        fpSize++;
                        if (valFuture < valNow) {
                            goodFP++;
                            if (returnSize > 1) {
                                goodFPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(TN)) {
                        tnSize++;
                        if (valFuture < valNow) {
                            goodTN++;
                            if (returnSize > 1) {
                                goodTNprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(FN)) {
                        fnSize++;
                        if (valFuture > valNow) {
                            goodFN++;
                            if (returnSize > 1) {
                                goodFNprob += tfpnProb;
                            }
                        }
                    }
                }
            }
            //System.out.println("tot " + total + " " + goodTP + " " + goodFP + " " + goodTN + " " + goodFN);
            memory.setMarket(market);
            memory.setRecord(new Date());
            memory.setDate(baseDate);
            memory.setUsedsec(usedsec);
            memory.setFuturedays(daysafterzero);
            memory.setFuturedate(futureDate);
            memory.setComponent(PipelineConstants.MLMACD);
            memory.setCategory(categoryTitle);
            memory.setSubcomponent(meta.get(0) + ", " + meta.get(1) + ", " + meta.get(3) + ", " + meta.get(4));
            memory.setTestaccuracy(testaccuracy);
            //memory.setPositives(goodInc);
            memory.setTp(goodTP);
            memory.setFp(goodFP);
            memory.setTn(goodTN);
            memory.setFn(goodFN);
            if (returnSize > 1) {
                memory.setTpProb(goodTPprob);
                memory.setFpProb(goodFPprob);
                memory.setTnProb(goodTNprob);
                memory.setFnProb(goodFNprob);      
                Double goodTPprobConf = goodTP != 0 ? goodTPprob / goodTP : null;
                Double goodFPprobConf = goodFP != 0 ? goodFPprob / goodFP : null;
                Double goodTNprobConf = goodTN != 0 ? goodTNprob / goodTN : null;
                Double goodFNprobConf = goodFN != 0 ? goodFNprob / goodFN : null;
                memory.setTpProbConf(goodTPprobConf);
                memory.setFpProbConf(goodFPprobConf);
                memory.setTnProbConf(goodTNprobConf);
                memory.setFnProbConf(goodFNprobConf);                
            }
            Integer tpClassOrig = countMapClass.containsKey(TP) ? countMapClass.get(TP) : 0;
            Integer tnClassOrig = countMapClass.containsKey(TN) ? countMapClass.get(TN) : 0;
            Integer fpClassOrig = countMapClass.containsKey(FP) ? countMapClass.get(FP) : 0;
            Integer fnClassOrig = countMapClass.containsKey(FN) ? countMapClass.get(FN) : 0;
            Integer tpSizeOrig = countMapLearn.containsKey(TP) ? countMapLearn.get(TP) : 0;
            Integer tnSizeOrig = countMapLearn.containsKey(TN) ? countMapLearn.get(TN) : 0;
            Integer fpSizeOrig = countMapLearn.containsKey(FP) ? countMapLearn.get(FP) : 0;
            Integer fnSizeOrig = countMapLearn.containsKey(FN) ? countMapLearn.get(FN) : 0;
            boolean doTP = countMapLearn.containsKey(TP);
            boolean doFP = countMapLearn.containsKey(FP);
            boolean doTN = countMapLearn.containsKey(TN);
            boolean doFN = countMapLearn.containsKey(FN);
            int keys = 0;
            if (doTP) {
                keys++;
            }
            if (doFP) {
                keys++;
            }
            if (doTN) {
                keys++;
            }
            if (doFN) {
                keys++;
            }
            Integer totalClass = tpClassOrig + tnClassOrig + fpClassOrig + fnClassOrig;
            Integer totalSize = tpSizeOrig + tnSizeOrig + fpSizeOrig + fnSizeOrig;
            Double learnConfidence = 0.0;
            learnConfidence = keys != 0 && totalClass != 0 && totalSize != 0 ? (double) (
                    ( doTP ? Math.abs((double) tpClassOrig / totalClass - (double) tpSizeOrig / totalSize) : 0) +
                    ( doFP ? Math.abs((double) fpClassOrig / totalClass - (double) fpSizeOrig / totalSize) : 0) +
                    ( doTN ? Math.abs((double) tnClassOrig / totalClass - (double) tnSizeOrig / totalSize) : 0) +
                    ( doFN ? Math.abs((double) fnClassOrig / totalClass - (double) fnSizeOrig / totalSize) : 0)
                    ) / keys : null;
            String info = null; 
            if (tpSizeOrig != null) {
                info = "Classified / learned: ";
                info += "TP " + tpClassOrig + " / " + tpSizeOrig + ", ";
                info += "TN " + tnClassOrig + " / " + tnSizeOrig + ", ";
                info += "FP " + fpClassOrig + " / " + fpSizeOrig + ", ";
                info += "FN " + fnClassOrig + " / " + fnSizeOrig + " ";
            }
            memory.setInfo(info);
            memory.setTpSize(tpSize);
            memory.setTnSize(tnSize);
            memory.setFpSize(fpSize);
            memory.setFnSize(fnSize);
            Double tpConf = (tpSize != 0 ? ((double) goodTP / tpSize) : null);
            Double tnConf = (tnSize != 0 ? ((double) goodTN / tnSize) : null);
            Double fpConf = (fpSize != 0 ? ((double) goodFP / fpSize) : null);
            Double fnConf = (fnSize != 0 ? ((double) goodFN / fnSize) : null);
            memory.setTpConf(tpConf);
            memory.setTnConf(tnConf);
            memory.setFpConf(fpConf);
            memory.setFnConf(fnConf);
            memory.setSize(total);
            Double conf = total != 0 ? ((double) goodTP + goodTN + goodFP + goodFN) / total : null;
            memory.setPositives(goodTP + goodTN + goodFP + goodFN);
            memory.setConfidence(conf);
            memory.setLearnConfidence(learnConfidence);
            memory.setPosition(count);
            if (doSave) {
                memory.save();
            }
            System.out.println(memory);
            resultIndex += returnSize;
            count++;
        }
    }

    public static void doMLIndicator(String market, Integer offset, String aDate, boolean doSave) throws ParseException {
        long time0 = System.currentTimeMillis();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int futuredays = (int) srv.conf.getAggregatorsIndicatorFuturedays();
        double threshold = srv.conf.getAggregatorsIndicatorThreshold();
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        //System.out.println("da " + + futuredays + " " + baseDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);

        Date baseDate = dt.parse(baseDateStr);
        Date futureDate = dt.parse(futureDateStr);
        srv.conf.setdate(baseDate);
        //srv.getTestRecommender(true);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        Map<String, Map<String, Object>> result0 = srv.getContent();

        Map<String, Map<String, Object>> maps = result0;
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        Map mlIndicatorMaps = (Map) maps.get(PipelineConstants.MLINDICATOR);
        //System.out.println("mli " + mlIndicatorMaps.keySet());
        Integer category = (Integer) mlIndicatorMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlIndicatorMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlIndicatorMaps.get(PipelineConstants.RESULT);
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlIndicatorMaps.get(PipelineConstants.PROBABILITY);
        List<Object[]> resultMetaArray = (List<Object[]>) mlIndicatorMaps.get(PipelineConstants.RESULTMETAARRAY);
        List<Object> objectList = (List<Object>) mlIndicatorMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        if (resultMap == null) {
            return;
        }
        int size = resultMap.values().iterator().next().size();
        Map<String, Object>[] aMap = new HashMap[size];
        for (int i = 0; i < size; i++) {
            aMap[i] = new HashMap<>();
        }
        for (String key : resultMap.keySet()) {
            List<Object> list = resultMap.get(key);
            for (int i = 0; i < size; i++) {
                if (list.get(i) != null) {
                    aMap[i].put(key, list.get(i));    
                }
            }
        }
        srv.conf.setdate(futureDate);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            calculateMLindicator(market, futuredays, baseDate, futureDate, threshold, resultMap, size, categoryValueMap, resultMeta, categoryTitle, usedsec, doSave);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
    }
    private static void calculateMLindicator(String market, int futuredays, Date baseDate, Date futureDate, double threshold,
            Map<String, List<Object>> resultMap, int size0, Map<String, List<List<Double>>> categoryValueMap, List<ResultMeta> resultMeta, String categoryTitle, Integer usedsec, boolean doSave) throws Exception {
        int resultIndex = 0;
        int count = 0;
        for (ResultMeta meta : resultMeta) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.getReturnSize();
            Double testaccuracy = (Double) meta.getTestAccuracy();
            //Map<String, double[]> offsetMap = (Map<String, double[]>) meta.get(8);
            /*
            Map<String, Integer> countMapLearn = (Map<String, Integer>) meta.get(5);
            Map<String, Integer> countMapClass = (Map<String, Integer>) meta.get(7);
             */
            Map<String, Integer> countMapLearn = (Map<String, Integer>) resultMeta.get(count).getLearnMap();
            Map<String, Integer> countMapClass = (Map<String, Integer>) resultMeta.get(count).getClassifyMap();
            long total = 0;
            long goodTP = 0;
            long goodFP = 0;
            long goodTN = 0;
            long goodFN = 0;
            long incSize = 0;
            //long fpSize = 0;
            long decSize = 0;
            //long fnSize = 0;
            double goodTPprob = 0;
            double goodFPprob = 0;
            double goodTNprob = 0;
            double goodFNprob = 0;
            int size = resultMap.values().iterator().next().size();
            for (String key : categoryValueMap.keySet()) {
                List<List<Double>> resultList = categoryValueMap.get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList == null) {
                    continue;
                }
                //int offset = (int) offsetMap.get(key)[0];
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - futuredays);
                if (valFuture == null || valNow == null) {
                    continue;
                }
                boolean incThreshold = (valFuture / valNow - 1) >= threshold;
                List<Object> list = resultMap.get(key);
                if (list == null) {
                    continue;
                }
                String incdec = (String) list.get(resultIndex);
                if (incdec == null) {
                    continue;
                }
                Double incdecProb = null;
                if (returnSize > 1) {
                    incdecProb = (Double) list.get(resultIndex + 1);
                }
                total++;
                if (incdec.equals(INC)) {
                    incSize++;
                    if (incThreshold) {
                        goodTP++;
                        if (returnSize > 1) {
                            goodTPprob += incdecProb;
                        }
                    } else {
                        goodFP++;
                        if (returnSize > 1) {
                            goodFPprob += (1 - incdecProb);                                    }
                    }
                }
                if (incdec.equals(DEC)) {
                    decSize++;
                    if (!incThreshold) {
                        goodTN++;
                        if (returnSize > 1) {
                            goodTNprob += incdecProb;
                        }
                    } else {
                        goodFN++;
                        if (returnSize > 1) {
                            goodFNprob += (1 - incdecProb);
                        }
                    }
                }
            }
            //System.out.println("tot " + total + " " + goodTP + " " + goodFP + " " + goodTN + " " + goodFN);
            memory.setMarket(market);
            memory.setRecord(new Date());
            memory.setDate(baseDate);
            memory.setUsedsec(usedsec);
            memory.setFuturedays(futuredays);
            memory.setFuturedate(futureDate);
            memory.setComponent(PipelineConstants.MLINDICATOR);
            memory.setCategory(categoryTitle);
            memory.setSubcomponent(meta.getMlName() + ", " + meta.getModelName() + ", " + meta.getSubType() + ", " + meta.getSubSubType());
            memory.setTestaccuracy(testaccuracy);
            //memory.setPositives(goodInc);
            memory.setTp(goodTP);
            memory.setFp(goodFP);
            memory.setTn(goodTN);
            memory.setFn(goodFN);
            if (returnSize > 1) {
                memory.setTpProb(goodTPprob);
                memory.setFpProb(goodFPprob);
                memory.setTnProb(goodTNprob);
                memory.setFnProb(goodFNprob);      
                Double goodTPprobConf = goodTPprob / goodTP;
                Double goodFPprobConf = goodFPprob / goodFP;
                Double goodTNprobConf = goodTNprob / goodTN;
                Double goodFNprobConf = goodFNprob / goodFN;
                memory.setTpProbConf(goodTPprobConf);
                memory.setFpProbConf(goodFPprobConf);
                memory.setTnProbConf(goodTNprobConf);
                memory.setFnProbConf(goodFNprobConf);                
            }
            Integer tpClassOrig = countMapClass.containsKey(INC) ? countMapClass.get(INC) : 0;
            Integer tnClassOrig = countMapClass.containsKey(DEC) ? countMapClass.get(DEC) : 0;
            //Integer fpClassOrig = goodTP - ;
            //Integer fnClassOrig = countMapClass.containsKey(FN) ? countMapClass.get(FN) : 0;
            Integer tpSizeOrig = countMapLearn.containsKey(INC) ? countMapLearn.get(INC) : 0;
            Integer tnSizeOrig = countMapLearn.containsKey(DEC) ? countMapLearn.get(DEC) : 0;
            //Integer fpSizeOrig = countMapLearn.containsKey(FP) ? countMapLearn.get(FP) : 0;
            //Integer fnSizeOrig = countMapLearn.containsKey(FN) ? countMapLearn.get(FN) : 0;
            int keys = 2;
            Integer totalClass = tpClassOrig + tnClassOrig;
            Integer totalSize = tpSizeOrig + tnSizeOrig;
            Double learnConfidence = 0.0;
            learnConfidence = (double) (
                    ( true ? Math.abs((double) tpClassOrig / totalClass - (double) tpSizeOrig / totalSize) : 0) +
                    //( true ? Math.abs((double) fpClassOrig / totalClass - (double) fpSizeOrig / totalSize) : 0) +
                    ( true ? Math.abs((double) tnClassOrig / totalClass - (double) tnSizeOrig / totalSize) : 0)
                    //( true ? Math.abs((double) fnClassOrig / totalClass - (double) fnSizeOrig / totalSize) : 0)
                    ) / keys;
            String info = null; 
            if (tpSizeOrig != null) {
                info = "Classified / learned: ";
                info += "TP " + tpClassOrig + " / " + tpSizeOrig + ", ";
                info += "TN " + tnClassOrig + " / " + tnSizeOrig + ", ";
                info += "FP " + (tpSizeOrig - tpClassOrig) + " / " + tpSizeOrig + ", ";
                info += "FN " + (tnSizeOrig - tnClassOrig) + " / " + tnSizeOrig + " ";
            }
            memory.setInfo(info);
            memory.setTpSize(incSize);
            memory.setTnSize(decSize);
            memory.setFpSize(incSize);
            memory.setFnSize(decSize);
            Double tpConf = (incSize != 0 ? ((double) goodTP / incSize) : null);
            Double tnConf = (decSize != 0 ? ((double) goodTN / decSize) : null);
            Double fpConf = (incSize != 0 ? ((double) goodFP / incSize) : null);
            Double fnConf = (decSize != 0 ? ((double) goodFN / decSize) : null);
            memory.setTpConf(tpConf);
            memory.setTnConf(tnConf);
            memory.setFpConf(fpConf);
            memory.setFnConf(fnConf);
            memory.setSize(total);
            Double conf = ((double) goodTP + goodTN) / total;
            memory.setPositives(goodTP + goodTN);
            memory.setConfidence(conf);
            memory.setLearnConfidence(learnConfidence);
            memory.setPosition(count);
            if (doSave) {
                memory.save();
            }
            System.out.println(memory);
            resultIndex += returnSize;
            count++;
        }
        /*
        int total = 0;
        int goodInc = 0;
        int goodDec = 0;
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                List<Object> list = resultMap.get(key);
                if (list == null) {
                    continue;
                }
                for (int i = 0; i < size; i++) {
                    String incDec = (String) list.get(i);
                    if (incDec == null) {
                        continue;
                    }
                    if (valFuture != null && valNow != null) {
                        double change = valFuture / valNow;
                        total++;
                        if (change >= threshold && incDec.equals("Inc")) {
                            goodInc++;
                        }
                        if (change < threshold && incDec.equals("Dec")) {
                            goodDec++;
                        }
                    }
                }
            }
        }
        */
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
    }

    private static double median(Set<Double> set) {
        Double[] scores = set.toArray(new Double[set.size()]);
        Arrays.sort(scores);
        //System.out.print("Sorted Scores: ");
        for (double x : scores) {
            //System.out.print(x + " ");
        }
        //System.out.println("");

        // Calculate median (middle number)
        double median = 0;
        double pos1 = Math.floor((scores.length - 1.0) / 2.0);
        double pos2 = Math.ceil((scores.length - 1.0) / 2.0);
        if (pos1 == pos2 ) {
            median = scores[(int)pos1];
        } else {
            median = (scores[(int)pos1] + scores[(int)pos2]) / 2.0 ;
        }
        return median;
    }

    private static double median2(Set<Double> set) {
        Double[] numArray = set.toArray(new Double[set.size()]);
        Arrays.sort(numArray);
        Arrays.sort(numArray);
        int middle = numArray.length/2;
        double medianValue = 0; //declare variable 
        if (numArray.length%2 == 1) {
            medianValue = numArray[middle];
        } else {
            medianValue = (numArray[middle-1] + numArray[middle]) / 2;
        }
        return medianValue;
    }

    public static String getWantedCategory(Map<String, Map<String, Object>> maps, String type) throws Exception {
        List<String> wantedList = new ArrayList<>();
        wantedList.add(Constants.PRICE);
        wantedList.add(Constants.INDEX);
        wantedList.add("cy");
        String cat = null;
        for (String wanted : wantedList) {
            Map<String, Object> map = maps.get(wanted);
            if (map != null) {
                if (map.containsKey(type)) {
                    LinkedHashMap<String, Object> tmpMap = (LinkedHashMap<String, Object>) map.get(type);
                    if (tmpMap.get(PipelineConstants.RESULT) != null) {
                        return wanted;
                    }
                }
            }
        }
        return cat;
    }
    public static IclijServiceResult getContent() throws Exception {
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        
        List<IncDecItem> listAll = IncDecItem.getAll();
        List<List> lists = new ArrayList<>();
        List<TradeMarket> markets = instance.getTradeMarkets();
        for (TradeMarket market : markets) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, - market.getRecordage() );
            Date olddate = cal.getTime();
            listAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
            List<IncDecItem> currentIncDecs = listAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            currentIncDecs = currentIncDecs.stream().filter(m -> market.getMarket().equals(m.getMarket())).collect(Collectors.toList());
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            lists.add(listInc);
            lists.add(listDec);
        }
        IclijServiceResult result = new IclijServiceResult();
        result.lists = lists;
        return result;
    }

}
