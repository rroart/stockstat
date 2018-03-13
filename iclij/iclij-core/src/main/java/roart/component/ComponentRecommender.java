package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.ServiceUtil;

public class ComponentRecommender extends Component {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.TRUE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.TRUE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.TRUE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.FALSE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.FALSE);                
        conf.configValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.FALSE);                
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        srv.getTestRecommender(true, new ArrayList<>());
        resultMaps = srv.getContent();
        String market = okListMap.values().iterator().next().get(0).getMarket();
        findBuySellRecommendations(resultMaps, nameMap, market, buys, sells, okConfMap, okListMap);
    }

    private void findBuySellRecommendations(Map<String, Map<String, Object>> resultMaps, Map<String, String> nameMap, String market, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap) {
        Object[] keys = new Object[2];
        keys[0] = PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
        keys[1] = null;
        keys = ComponentMLMACD.getRealKeys(keys, okConfMap.keySet());
        //System.out.println(okListMap.get(keys));
        Double confidenceFactor = okConfMap.get(keys);
        //System.out.println(okConfMap.keySet());
        //System.out.println(okListMap.keySet());
        Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) maps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, Map> resultMap0 = (Map<String, Map>) maps.get(PipelineConstants.RESULT);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) resultMap0.get("complex");
        if (resultMap == null) {
            return;
        }
        List<MyElement> list0 = new ArrayList<>();
        List<MyElement> list1 = new ArrayList<>();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            String key = entry.getKey();
            List<List<Double>> resultList = entry.getValue();
            List<Double> mainList = resultList.get(0);
            if (mainList == null) {
                continue;
            }
            List<Double> list = resultMap.get(key);
            if (list == null) {
                continue;
            }
            list0.add(new MyElement(key, list.get(0)));
            list1.add(new MyElement(key, list.get(1)));
        }
        Collections.sort(list0, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        Collections.sort(list1, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        int listSize = list0.size();
        int recommend = 10;
        if (listSize < recommend * 3) {
            return;
        }
        List<MyElement> topList = list0.subList(0, recommend);
        List<MyElement> bottomList = list0.subList(listSize - 10, listSize);
        Double max = list0.get(0).getValue();
        Double min = list0.get(listSize - 1).getValue();
        Double diff = max - min;
        for (MyElement element : topList) {
            double confidence = confidenceFactor * (element.getValue() - min) / diff;
            String recommendation = "recommend buy";
            //IncDecItem incdec = getIncDec(element, confidence, recommendation, nameMap, market);
            //incdec.setIncrease(true);
            //buys.put(element.getKey(), incdec);
            IncDecItem incdec = ComponentMLMACD.mapAdder(buys, element.getKey(), confidence, okListMap.get(keys), nameMap);
            incdec.setIncrease(true);
        }
        for (MyElement element : bottomList) {
            double confidence = confidenceFactor * (element.getValue() - min) / diff;
            String recommendation = "recommend sell";
            //IncDecItem incdec = getIncDec(element, confidence, recommendation, nameMap, market);
            //incdec.setIncrease(false);
            IncDecItem incdec = ComponentMLMACD.mapAdder(sells, element.getKey(), confidence, okListMap.get(keys), nameMap);
            incdec.setIncrease(false);
        }
    }

    private IncDecItem getIncDec(MyElement element, double confidence, String recommendation, Map<String, String> nameMap, String market) {
        IncDecItem incdec = new IncDecItem();
        incdec.setRecord(LocalDate.now());
        incdec.setId(element.getKey());
        incdec.setMarket(market);
        incdec.setDescription(recommendation);
        incdec.setName(nameMap.get(element.getKey()));
        incdec.setScore(confidence);
        return incdec;
    }

    public List<String> getBuy() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE);
        return buyList;
    }
    public List<String> getSell() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE);
        return sellList;
    }
    @Override
    public Map<String, String> improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> badConfMap,
            Map<Object[], List<MemoryItem>> badListMap, Map<String, String> nameMap) {
        Map<String, String> retMap = new HashMap<>();
        List<String> list = getBuy();
        retMap.putAll(handleBuySell(conf, badListMap, list));
        list = getSell();
        retMap.putAll(handleBuySell(conf, badListMap, list));
        return retMap;
    }

    private List<List<String>> disableAllButOne(List<String> list) {
        List<List<String>> listPerm = new ArrayList<>();
        int size = list.size();
        int bitsize = (1 << size) - 1;
        for (int i = 0; i < size; i++) {
            int pattern = bitsize - (1 << i);
            log.info("using {}", Integer.toBinaryString(pattern));
            List<String> aList = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if ((pattern & (1 << j)) != 0) {
                    aList.add(list.get(j));
                }
            }
            listPerm.add(aList);
        }
        return listPerm;
    }
    
    private Map<String, String> handleBuySell(MyMyConfig conf, Map<Object[], List<MemoryItem>> badListMap, List<String> list) {
        Map<String, String> retMap = new HashMap<>();
        //List<List<String>> listPerm = getAllPerms(list);
        List<List<String>> listPerm = disableAllButOne(list);
        String market = badListMap.values().iterator().next().get(0).getMarket();
        ControlService srv = new ControlService();
        srv.getConfig();            
        // plus testrecommendfactor
        int factor = 100;
        log.info("market ", market);
        log.info("factor ", factor);
        srv.conf.configValueMap.put(ConfigConstants.TESTRECOMMENDFACTOR, factor);
        int index = 0;
        for (List<String> aList : listPerm) {
            log.info("For disable {}", Integer.toHexString(index++));
            try {
                List<MemoryItem> memories = ServiceUtil.doRecommender(srv, market, 0, null, false, aList, false);
                if (memories == null) {
                    log.info("No memories in {}", market);
                    continue;
                }
                List<Double> newConfidenceList = new ArrayList<>();
                for(MemoryItem memory : memories) {
                    newConfidenceList.add(memory.getConfidence());
                }
                log.info("New confidences {}", newConfidenceList);
                retMap.put(aList.toString(), newConfidenceList.toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return retMap;
    }

    private List<List<String>> getAllPerms(List<String> list) {
        List<List<String>> listPerm = new ArrayList<>();
        int size = list.size();
        int bitsize = (1 << size) - 1;
        for (int i = 0; i < bitsize; i++) {
            List<String> aList = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if ((i & (1 << j)) != 0) {
                    aList.add(list.get(j));
                }
            }
            listPerm.add(aList);
        }
        return listPerm;
    }
    
    public List<MemoryItem> calculateRecommender(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, double buyMedian, double sellMedian,
            Map<String, Map<String, Object>> result, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave, boolean doPrint) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        Set<Double> changeSet = getChangeSet(futuredays, categoryValueMap);
        Double medianChange = ServiceUtil.median(changeSet);
        /*
        getMemoriesOld(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell, buyMedian, sellMedian,
                categoryValueMap, usedsec, doSave, memoryList, medianChange);
        */
        getMemories(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell, buyMedian, sellMedian,
                categoryValueMap, usedsec, doSave, memoryList, medianChange, changeSet, doPrint);
        return memoryList;
    }

    private Set<Double> getChangeSet(int futuredays, Map<String, List<List<Double>>> categoryValueMap) {
        Set<Double> changeSet = new HashSet<>();
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            List<List<Double>> resultList = entry.getValue();
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
        return changeSet;
    }

    public void getMemories(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, double buyMedian, double sellMedian,
            Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave,
            List<MemoryItem> memoryList, Double medianChange, Set<Double> changeSet, boolean doPrint) throws Exception {
        double goodBuy = 0;
        double goodSell = 0;
        long totalBuy = 0;
        long totalSell = 0;
        Optional<Double> minOpt = changeSet.parallelStream().reduce(Double::min);
        Double minChange = 0.0;
        if (minOpt.isPresent()) {
            minChange = minOpt.get();
        }
        Optional<Double> maxOpt = changeSet.parallelStream().reduce(Double::max);
        Double maxChange = 0.0;
        if (maxOpt.isPresent()) {
            maxChange = maxOpt.get();
        }
        Double diffChange = maxChange - minChange;
        List<Double> buyList = new ArrayList<>();
        List<Double> sellList = new ArrayList<>();
        for (List<Double> list : recommendBuySell.values()) {
            buyList.add(list.get(0));
            sellList.add(list.get(1));
        }
        Optional<Double> buyMaxOpt = buyList.parallelStream().reduce(Double::max);
        if (!buyMaxOpt.isPresent()) {
        }
        Double buyMax = buyMaxOpt.get();
        Optional<Double> buyMinOpt = buyList.parallelStream().reduce(Double::min);
        if (!buyMinOpt.isPresent()) {
        }
        Double buyMin = buyMinOpt.get();
        Double diffBuy = buyMax - buyMin;
        Optional<Double> sellMaxOpt = sellList.parallelStream().reduce(Double::max);
        if (!sellMaxOpt.isPresent()) {
        }
        Double sellMax = sellMaxOpt.get();
        Optional<Double> sellMinOpt = sellList.parallelStream().reduce(Double::min);
        if (!sellMinOpt.isPresent()) {
        }
        Double sellMin = sellMinOpt.get();
        Double diffSell = sellMax - sellMin;
        
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
                //double delta = change / buy * (buyMax / maxChange);
                double expectedChange = minChange + diffChange * (buy - buyMin) / diffBuy;
                //double delta = expectedChange / change;
                double confidence = 1 - Math.abs(expectedChange - change) / diffChange;
                /*
                if (delta > 1) {
                    delta = 1 / delta;
                } 
                */               
                goodBuy += confidence;
            }
            if (sell != null) {
                totalSell++;
                //double delta = change / sell * (sellMax / minChange);
                double expectedChange = minChange + diffChange * (sell - sellMin) / diffSell;
                //double delta = expectedChange / change;
                /*
                if (delta > 1) {
                    delta = 1 / delta;
                }
                */
                double confidence = 1 - Math.abs(expectedChange - change) / diffChange;
                goodSell += confidence;
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
        buyMemory.setPositives((long) goodBuy);
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
        sellMemory.setPositives((long) goodSell);
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

    private class MyElement {
        public String key;
        public Double value;
        
        public MyElement(String key, Double value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        
        public Double getValue() {
            return value;
        }
    }
}

