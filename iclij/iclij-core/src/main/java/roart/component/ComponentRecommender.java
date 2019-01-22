package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.common.pipeline.PipelineConstants;
import roart.config.IclijXMLConfig;
import roart.evolution.fitness.AbstractScore;
import roart.evolution.fitness.impl.ProportionScore;
import roart.executor.MyExecutors;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.util.ServiceUtil;

public class ComponentRecommender extends Component {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORS, Boolean.TRUE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.TRUE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.TRUE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.TRUE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORS, Boolean.FALSE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.FALSE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.FALSE);                
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.FALSE);                
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap, IclijConfig config, Map<String, Object> updateMap) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        if (instance.wantEvolveRecommender()) {
            srv.getEvolveRecommender(true, new ArrayList<>());
        }
        resultMaps = srv.getContent();
        String market = okListMap.values().iterator().next().get(0).getMarket();
        findBuySellRecommendations(resultMaps, nameMap, market, buys, sells, okConfMap, okListMap, srv, config);
    }

    private void findBuySellRecommendations(Map<String, Map<String, Object>> resultMaps, Map<String, String> nameMap, String market, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap, ControlService srv, IclijConfig config) {
        for (int i = 0; i < 2; i++) {
        Object[] keys = new Object[2];
        keys[0] = PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
        keys[1] = i;
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
        //List<MyElement> list1 = new ArrayList<>();
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
            list0.add(new MyElement(key, list.get(i)));
            //list1.add(new MyElement(key, list.get(1)));
        }
        Collections.sort(list0, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        //Collections.sort(list1, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list0);
        //handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list1);
        }
    }

    private void handleBuySell(Map<String, String> nameMap, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells,
            Map<Object[], List<MemoryItem>> okListMap, ControlService srv, IclijConfig config, Object[] keys,
            Double confidenceFactor, List<MyElement> list) {
        int listSize = list.size();
        int recommend = config.recommendTopBottom();
        if (listSize < recommend * 3) {
            return;
        }
        List<MyElement> topList = list.subList(0, recommend);
        List<MyElement> bottomList = list.subList(listSize - recommend, listSize);
        Double max = list.get(0).getValue();
        Double min = list.get(listSize - 1).getValue();
        Double diff = max - min;
        for (MyElement element : topList) {
            if (confidenceFactor == null || element.getValue() == null) {
                int jj = 0;
                continue;
            }
            double confidence = confidenceFactor * (element.getValue() - min) / diff;
            String recommendation = "recommend buy";
            //IncDecItem incdec = getIncDec(element, confidence, recommendation, nameMap, market);
            //incdec.setIncrease(true);
            //buys.put(element.getKey(), incdec);
            IncDecItem incdec = ComponentMLMACD.mapAdder(buys, element.getKey(), confidence, okListMap.get(keys), nameMap, TimeUtil.convertDate(srv.conf.getdate()));
            incdec.setIncrease(true);
        }
        for (MyElement element : bottomList) {
            if (confidenceFactor == null || element.getValue() == null) {
                int jj = 0;
                continue;
            }
            double confidence = confidenceFactor * (element.getValue() - min) / diff;
            String recommendation = "recommend sell";
            //IncDecItem incdec = getIncDec(element, confidence, recommendation, nameMap, market);
            //incdec.setIncrease(false);
            IncDecItem incdec = ComponentMLMACD.mapAdder(sells, element.getKey(), confidence, okListMap.get(keys), nameMap, TimeUtil.convertDate(srv.conf.getdate()));
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
        log.info("market {}", market);
        //log.info("factor {}", factor);
        //srv.conf.getConfigValueMap().put(ConfigConstants.TESTRECOMMENDFACTOR, factor);
        int index = 0;
        Map<Future<List<MemoryItem>>, List<String>> futureMap = new HashMap<>();
        List<Future<List<MemoryItem>>> futureList = new ArrayList<>();
        for (List<String> aList : listPerm) {
            log.info("For disable {}", Integer.toHexString(index++));
            try {
                //List<MemoryItem> memories = ServiceUtil.doRecommender(srv, market, 0, null, false, aList, false);
                Callable callable = new RecommenderCallable(srv, market, 0, null, false, aList, false);  
                Future<List<MemoryItem>> future = MyExecutors.run(callable, 0);
                futureList.add(future);
                futureMap.put(future, aList);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        for (Future<List<MemoryItem>> future : futureList) {
            try {
                List<String> aList = futureMap.get(future);
                List<MemoryItem> memories = future.get();
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
            String categoryTitle, Map<String, List<Double>> recommendBuySell,
            Map<String, Map<String, Object>> result, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec, boolean doSave, boolean doPrint) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        AbstractScore eval = new ProportionScore();
        for (int i = 0; i < 2; i++) {
            getMemories(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell, categoryValueMap, usedsec,
                doSave, memoryList, doPrint, eval, i);
        }
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
                    if (valNow == 0.0) {
                        log.error("Value for division is 0.0 for key {}", entry.getKey());
                        continue;
                    }
                    Double change = valFuture / valNow;
                    changeSet.add(change);
                }
            }
        }
        return changeSet;
    }

    public void getMemories(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec,
            boolean doSave, List<MemoryItem> memoryList,
            boolean doPrint, AbstractScore eval, int position) throws Exception {
        Map<String, List<Double>> resultMap = new HashMap<>();
        for (String key : categoryValueMap.keySet()) {
            List<Double> vals = recommendBuySell.get(key);
            if (vals == null) {
                continue;
            }
            Double score = vals.get(position);
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            Double change = null;
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - futuredays);
                if (valFuture != null && valNow != null) {
                    if (valNow == 0.0) {
                        log.error("Value for division is 0.0 for key {}", key);
                        continue;
                    }
                    change = valFuture / valNow;
                    List<Double> list = new ArrayList<>();
                    list.add(score);
                    list.add(change);
                    resultMap.put(key, list);
                }
            }
        }
        
        double[] resultArray = eval.calculate(resultMap);
        double goodBuy = resultArray[0];
        long totalBuy = (long) resultArray[1];
        MemoryItem memory = new MemoryItem();
        memory.setMarket(market);
        memory.setRecord(LocalDate.now());
        memory.setDate(baseDate);
        memory.setUsedsec(usedsec);
        memory.setFuturedays(futuredays);
        memory.setFuturedate(futureDate);
        memory.setComponent(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        memory.setSubcomponent("rec " + position + " " + eval.name());
        memory.setCategory(categoryTitle);
        memory.setPositives((long) goodBuy);
        memory.setSize(totalBuy);
        memory.setConfidence((double) goodBuy / totalBuy);
        memory.setPosition(position);
        if (doSave) {
            memory.save();
        }
        if (doPrint) {
            System.out.println(memory);
        }
        memoryList.add(memory);
    }

    public void getMemoriesPrev(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec,
            boolean doSave, List<MemoryItem> memoryList, Set<Double> changeSet,
            boolean doPrint) throws Exception {
        int div = 4;
        double goodBuy = 0;
        double goodSell = 0;
        long totalBuy = 0;
        long totalSell = 0;
        List<Double> buyList = new ArrayList<>();
        List<Double> sellList = new ArrayList<>();
        for (List<Double> list : recommendBuySell.values()) {
            buyList.add(list.get(0));
            sellList.add(list.get(1));
        }
        int ups = 0;
        int downs = 0;
        List<BuySellList> buys = new ArrayList<>();
        List<BuySellList> sells = new ArrayList<>();
        for (String key : categoryValueMap.keySet()) {
            List<Double> vals = recommendBuySell.get(key);
            if (vals == null) {
                continue;
            }
            Double buy = vals.get(0);
            Double sell = vals.get(1);
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            Double change = null;
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - futuredays);
                if (valFuture != null && valNow != null) {
                    if (valNow == 0.0) {
                        log.error("Value for division is 0.0 for key {}", key);
                        continue;
                    }
                    change = valFuture / valNow;
                    BuySellList aBuy = new BuySellList();
                    aBuy.id = key;
                    aBuy.change = change;
                    aBuy.score = buy;
                    buys.add(aBuy);
                    BuySellList aSell = new BuySellList();
                    aSell.id = key;
                    aSell.change = change;
                    aSell.score = sell;
                    sells.add(aSell);
                    if (change > 1) {
                        ups++;
                    }
                    if (change < 1) {
                        downs++;
                    }
                }
            }
        }
        //buys.sort(BuySellList.class);
        Collections.sort(buys, (d1, d2) -> Double.compare(d2.score, d1.score));
        Collections.sort(sells, (d1, d2) -> Double.compare(d2.score, d1.score));
        div = 1;
        int myups = ups / div;
        int mydowns = downs / div;
        myups = Math.min(myups, 20);
        mydowns = Math.min(mydowns, 20);
        int buyTop = 0;
        int buyBottom = 0;
        int sellTop = 0;
        int sellBottom = 0;
        for (int i = 0; i < myups; i++) {
            if (buys.get(i).change > 1) {
                buyTop++;
            }
            if (sells.get(i).change > 1) {
                sellTop++;
            }
        }
        int size = buys.size();
        for (int i = 0; i < mydowns; i++) {
            if (buys.get(size - 1 - i).change < 1) {
                buyBottom++;
            }
            if (sells.get(size - 1 - i).change < 1) {
                sellBottom++;
            }
        }
        goodBuy = buyTop + buyBottom;
        goodSell = sellTop + sellBottom;
        log.info("buyselltopbottom {} {} {} {} {} {}", buyTop, buyBottom, sellTop, sellBottom, myups, mydowns);
        totalBuy = myups + mydowns;
        totalSell = totalBuy;
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
        buyMemory.setPosition(0);
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
        sellMemory.setPosition(1);
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

    public void getMemoriesOld(String market, int futuredays, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Double>> recommendBuySell, Map<String, List<List<Double>>> categoryValueMap, Integer usedsec,
            boolean doSave, List<MemoryItem> memoryList, Set<Double> changeSet,
            boolean doPrint) throws Exception {
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
        log.info("Myexpect0 " + minChange + " " + diffChange + " " + diffBuy + " " + maxChange + " " + minChange + " " + buyMax + " " + buyMin);
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            Double change = null;
            Double valf = null;
            Double valn = null;
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                if (valFuture != null && valNow != null) {
                    if (valNow == 0.0) {
                        log.error("Value for division is 0.0 for key {}", key);
                        continue;
                    }
                    change = valFuture / valNow;
                    valf = valFuture;
                    valn = valNow;
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
                log.info("Myexpect " + expectedChange + " " + change + " " + valf + " " + valn + " " + buy + " " + key);
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
        buyMemory.setPosition(0);
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
        sellMemory.setPosition(1);
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
    
    class RecommenderCallable implements Callable {
        private ControlService srv;
       
        private String market;
        
        private Integer offset; 
        
        private String aDate;
        
        private boolean doSave;
        
        private List<String> disableList;
        
        private boolean doPrint;

        public RecommenderCallable(ControlService srv, String market, Integer offset, String aDate, boolean doSave,
                List<String> disableList, boolean doPrint) {
            super();
            this.srv = srv;
            this.market = market;
            this.offset = offset;
            this.aDate = aDate;
            this.doSave = doSave;
            this.disableList = disableList;
            this.doPrint = doPrint;
        }

        public ControlService getSrv() {
            return srv;
        }

        public void setSrv(ControlService srv) {
            this.srv = srv;
        }

        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public String getaDate() {
            return aDate;
        }

        public void setaDate(String aDate) {
            this.aDate = aDate;
        }

        public boolean isDoSave() {
            return doSave;
        }

        public void setDoSave(boolean doSave) {
            this.doSave = doSave;
        }

        public List<String> getDisableList() {
            return disableList;
        }

        public void setDisableList(List<String> disableList) {
            this.disableList = disableList;
        }

        public boolean isDoPrint() {
            return doPrint;
        }

        public void setDoPrint(boolean doPrint) {
            this.doPrint = doPrint;
        }

        @Override
        public List<MemoryItem> call() throws Exception {
             return ServiceUtil.doRecommender(srv, market, 0, null, doSave, disableList, false);
        }

    }
    
    class BuySellList implements Comparable<Double> {
        String id;
        Double change;
        Double score;
        
        @Override
        public int compareTo(Double score0) {
            return Double.compare(score0, score);
        }
    }
}

