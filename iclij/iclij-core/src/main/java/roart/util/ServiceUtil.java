package roart.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.Action;
import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.UpdateDBAction;
import roart.action.VerifyProfitAction;
import roart.component.Component;
import roart.component.ComponentMLIndicator;
import roart.component.ComponentMLMACD;
import roart.component.ComponentPredictor;
import roart.component.ComponentRecommender;
import roart.config.ConfigConstants;
import roart.config.IclijConfig;
import roart.config.IclijXMLConfig;
import roart.config.MyConfig;
import roart.config.TradeMarket;
import roart.config.VerifyConfig;
import roart.constants.IclijPipelineConstants;
import roart.model.IncDecItem;
import roart.model.MapList;
import roart.model.MemoryItem;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.service.IclijServiceList;
import roart.service.IclijServiceResult;

public class ServiceUtil {
    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    public static List<MemoryItem> doRecommender(String market, Integer offset, String aDate, boolean doSave, List<String> disableList, boolean doPrint) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doRecommender(srv, market, offset, aDate, doSave, disableList, doPrint);
    }

    public static List<MemoryItem> doRecommender(ControlService srv, String market, Integer offset, String aDate, boolean doSave, List<String> disableList, boolean doPrint) throws Exception {
        long time0 = System.currentTimeMillis();
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
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        LocalDate baseDate = TimeUtil.convertDate(dt.parse(baseDateStr));
        LocalDate futureDate = TimeUtil.convertDate(dt.parse(futureDateStr));

        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        srv.getTestRecommender(true, disableList);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> maps = srv.getContent(disableList);
        Map recommendMaps = maps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //System.out.println("m3 " + recommendMaps.keySet());
        if (recommendMaps == null) {
            return null;
        }
        Integer category = (Integer) recommendMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) recommendMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, Map<String, List<Double>>> resultMap = (Map<String, Map<String, List<Double>>>) recommendMaps.get(PipelineConstants.RESULT);
        //System.out.println("m4 " + resultMap.keySet());
        if (resultMap == null) {
            return null;
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

        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        return new ComponentRecommender().calculateRecommender(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell, buyMedian,
                sellMedian, result, categoryValueMap, usedsec, doSave, doPrint);
    }
    private static void getMemoriesOld(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, double buyMedian, double sellMedian,
            Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave,
            List<MemoryItem> memoryList, Double medianChange, boolean doPrint) throws Exception {
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
        buyMemory.setRecord(LocalDate.now());
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
        sellMemory.setRecord(LocalDate.now());
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
        if (doPrint) {
            System.out.println(buyMemory);
            System.out.println(sellMemory);
        }
        memoryList.add(buyMemory);
        memoryList.add(sellMemory);
    }
    public static List<MemoryItem> doPredict(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doPredict(srv, market, offset, aDate, doSave, doPrint);
    }

    public static List<MemoryItem> doPredict(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        long time0 = System.currentTimeMillis();
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
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);

        LocalDate baseDate = TimeUtil.convertDate(dt.parse(baseDateStr));
        LocalDate futureDate = TimeUtil.convertDate(dt.parse(futureDateStr));
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        srv.conf.configValueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        Map<String, Map<String, Object>> result0 = srv.getContent();

        Map<String, Map<String, Object>> maps = result0;
        if (maps == null) {
            return null;
        }
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        String wantedCat = getWantedCategory(maps, PipelineConstants.LSTM);
        if (wantedCat == null) {
            return null;
        }
        Map map = (Map) maps.get(wantedCat).get(PipelineConstants.LSTM);

        //System.out.println("lstm " + map.keySet());
        Integer category = (Integer) map.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) map.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) map.get(PipelineConstants.RESULT);
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        return new ComponentPredictor().calculatePredictor(market, futuredays, baseDate, futureDate, categoryTitle, resultMap, categoryValueMap, usedsec, doSave, doPrint);
    }
    public static List<MemoryItem> doMLMACD(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doMLMACD(srv, market, offset, aDate, doSave, doPrint);
    }

    public static List<MemoryItem> doMLMACD(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        long time0 = System.currentTimeMillis();
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
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        LocalDate baseDate = TimeUtil.convertDate(dt.parse(baseDateStr));
        LocalDate futureDate = TimeUtil.convertDate(dt.parse(futureDateStr));

        srv.conf.setdate(TimeUtil.convertDate(baseDate));
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
            return null;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //System.out.println("m4 " + resultMetaArray);
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            // TODO add more offset
            // TODO verify dates and offsets
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            return new ComponentMLMACD().calculateMLMACD(market, daysafterzero, baseDate, futureDate, categoryTitle, resultMap, resultMetaArray,
                    categoryValueMap, resultMeta, offset, usedsec, doSave, doPrint);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
        return null;
    }
    public static List<MemoryItem> doMLIndicator(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doMLIndicator(srv, market, offset, aDate, doSave, doPrint);
    }

    public static List<MemoryItem> doMLIndicator(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        long time0 = System.currentTimeMillis();
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
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);

        LocalDate baseDate = TimeUtil.convertDate(dt.parse(baseDateStr));
        LocalDate futureDate = TimeUtil.convertDate(dt.parse(futureDateStr));
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
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
            return null;
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
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.configValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            return new ComponentMLIndicator().calculateMLindicator(market, futuredays, baseDate, futureDate, threshold, resultMap, size, categoryValueMap, resultMeta, categoryTitle, usedsec, doSave, doPrint);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(roart.util.Constants.EXCEPTION, e);
        }
        return null;
    }
    public static double median(Set<Double> set) {
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
    
    public static IclijServiceResult getConfig() throws Exception {
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        IclijServiceResult result = new IclijServiceResult();
        result.setIclijConfig(instance);
        return result;
    }
    
    public static IclijServiceResult getContent() throws Exception {
        IclijXMLConfig i = new IclijXMLConfig();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();

        List<IncDecItem> listAll = IncDecItem.getAll();
        List<IclijServiceList> lists = new ArrayList<>();
        List<TradeMarket> markets = conf.getTradeMarkets(instance);
        for (TradeMarket market : markets) {
            LocalDate olddate = LocalDate.now().minusDays(market.getRecordage());
            listAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
            List<IncDecItem> currentIncDecs = listAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
            currentIncDecs = currentIncDecs.stream().filter(m -> market.getMarket().equals(m.getMarket())).collect(Collectors.toList());
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
            List<IclijServiceList> subLists = getServiceList(market.getMarket(), listInc, listDec, listIncDec);
            lists.addAll(subLists);
        }
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(lists);
        return result;
    }

    private static List<IclijServiceList> getServiceList(String market, List<IncDecItem> listInc, List<IncDecItem> listDec,
            List<IncDecItem> listIncDec) {
        List<IclijServiceList> subLists = new ArrayList<>();
        if (!listInc.isEmpty()) {
            IclijServiceList inc = new IclijServiceList();
            inc.setTitle(market + " " + "Increase");
            inc.setList(listInc);
            subLists.add(inc);
        }
        if (!listDec.isEmpty()) {
            IclijServiceList dec = new IclijServiceList();
            dec.setTitle(market + " " + "Decrease");
            dec.setList(listDec);
            subLists.add(dec);
        }
        if (!listIncDec.isEmpty()) {
            IclijServiceList incDec = new IclijServiceList();
            incDec.setTitle(market + " " + "Increase and decrease");
            incDec.setList(listIncDec);
            subLists.add(incDec);
        }
        return subLists;
    }

    private static List<IncDecItem> moveAndGetCommon(List<IncDecItem> listInc, List<IncDecItem> listDec) {
        // and a new list for common items
        List<String> incIds = listInc.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<String> decIds = listDec.stream().map(IncDecItem::getId).collect(Collectors.toList());
        List<IncDecItem> listIncDec = listInc.stream().filter(m -> decIds.contains(m.getId())).collect(Collectors.toList());
        List<IncDecItem> listDecInc = listDec.stream().filter(m -> incIds.contains(m.getId())).collect(Collectors.toList());

        listInc.removeAll(listIncDec);
        listDec.removeAll(listDecInc);
        listIncDec.addAll(listDecInc);
        return listIncDec;
    }

    public static IclijServiceResult getVerify(IclijConfig config) throws InterruptedException, ParseException {
        IclijServiceResult result = new IclijServiceResult();
        result.setLists(new ArrayList<>());
        List<IclijServiceList> retLists = result.getLists();
        String market = config.getMarket();
        if (market == null) {
            return result;
        }
        int days = config.verificationDays();
        LocalDate date = config.getDate();
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        int offset = 0;
        if (date != null) {
            String aDate = TimeUtil.convertDate2(date);
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        } else {
            String aDate = stocks.get(stocks.size() - 1);
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            date = TimeUtil.convertDate(dt.parse(aDate));
        }
        log.info("Main date {} ", date);
        String aDate = stocks.get(stocks.size() - 1 - offset - days);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        LocalDate oldDate = TimeUtil.convertDate(dt.parse(aDate));
        log.info("Old date {} ", oldDate);
        UpdateDBAction updateDbAction = new UpdateDBAction();
        boolean save = false;
        Queue<Action> serviceActions = updateDbAction.findAllMarketComponentsToCheck(market, date, days + offset, save, config);
        FindProfitAction findProfitAction = new FindProfitAction();
        ImproveProfitAction improveProfitAction = new ImproveProfitAction();  
        List<MemoryItem> allMemoryItems = new ArrayList<>();
        for (Action serviceAction : serviceActions) {
            serviceAction.goal(null);
            Map<String, Object> resultMap = serviceAction.getLocalResultMap();
            List<MemoryItem> memoryItems = (List<MemoryItem>) resultMap.get(IclijPipelineConstants.MEMORY);
            allMemoryItems.addAll(memoryItems);            
        }
        IclijServiceList memories = new IclijServiceList();
        memories.setTitle("Memories");
        memories.setList(allMemoryItems);
        Map<String, IncDecItem>[] buysells = findProfitAction.getPicks(market, save, date, allMemoryItems, config);
        List<IncDecItem> listInc = new ArrayList<>(buysells[0].values());
        List<IncDecItem> listDec = new ArrayList<>(buysells[1].values());
        List<IncDecItem> listIncDec = moveAndGetCommon(listInc, listDec);
        List<IclijServiceList> subLists = getServiceList(market, listInc, listDec, listIncDec);
        retLists.addAll(subLists);
        if (config.wantsImproveProfit()) {
            Map<String, String> map = improveProfitAction.getImprovements(market, save, date, allMemoryItems);        
            List<MapList> mapList = improveProfitAction.getList(map);
            IclijServiceList resultMap = new IclijServiceList();
            resultMap.setTitle("Improve Profit Info");
            resultMap.setList(mapList);
            retLists.add(resultMap);
        }

        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Component.disabler(srv.conf);
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);

        VerifyProfitAction verify = new VerifyProfitAction();
        List<MapList> inc = verify.doVerify(listInc, days, true, categoryValueMap, oldDate);
        IclijServiceList incMap = new IclijServiceList();
        incMap.setTitle("Increase verify");
        incMap.setList(inc);
        List<MapList> dec = verify.doVerify(listDec, days, false, categoryValueMap, oldDate);
        IclijServiceList decMap = new IclijServiceList();
        incMap.setTitle("Decrease verify");
        incMap.setList(dec);
        retLists.add(incMap);
        retLists.add(decMap);
        retLists.add(memories);
        return result;
    }

}
