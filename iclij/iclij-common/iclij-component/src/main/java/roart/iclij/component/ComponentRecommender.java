package roart.iclij.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.RecommendConstants;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.iclij.evolution.chromosome.impl.RecommenderChromosome2;
import roart.iclij.evolution.chromosome.winner.ConfigMapChromosomeWinner;
import roart.evolution.fitness.AbstractScore;
import roart.evolution.fitness.Fitness;
import roart.evolution.fitness.impl.ProportionScore;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitData;

public class ComponentRecommender extends ComponentNoML {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCH, Boolean.TRUE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSATRDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCIDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDMACDDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDSIGNALDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSIDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.TRUE);
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCH, Boolean.FALSE);                
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORS, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSATRDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDMACDDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDSIGNALDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
    }

    @Override
    public ComponentData handle(MarketActionData action, Market market, ComponentData componentparam, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters, boolean hasParent) {
    
        RecommenderData param = new RecommenderData(componentparam);        
        
        int futuredays = (int) param.getService().conf.getTestIndicatorRecommenderComplexFutureDays();
        futuredays = 0;
        param.setFuturedays(futuredays);

        handle2(action, market, param, profitdata, positions, evolve && param.getConfig().wantEvolveRecommender(), aMap, subcomponent, mlmarket, parameters, hasParent);

        if (!evolve) {
        PipelineData resultMap = param.getResultMap();
        if (resultMap != null) {
        Map<String, Object> resultMap2 = (Map<String, Object>) resultMap.get(PipelineConstants.RESULT);
        Map<String, List<Double>> recommendBuySell = (Map<String, List<Double>>) resultMap2.get(RecommendConstants.COMPLEX);
        param.setRecommendBuySell(recommendBuySell);
        }
        }
        
        //handleEvolve(null, null, null, null, instance.wantEvolveRecommender(), param);
        //Map recommenderMaps = (Map) param.getResultMap(param.getService(), PipelineConstants.MLMACD, new HashMap<>());
        //Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLMACD);
        //param.setCategory(recommenderMaps);
        //param.getAndSetCategoryValueMap();
        //resultMaps = srv.getContent();
        return param;
        //findBuySellRecommendations(profitdata, param);
    }

    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, Memories position, Boolean above, List<MLMetricsItem> mlTests, Parameters parameters) {
        RecommenderData param = (RecommenderData) componentparam;
        //Map resultMaps = (Map) param.getResultMap(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, new HashMap<>());
        PipelineData resultMaps = param.getResultMap();
        for (int i = 0; i < 2; i++) {
        Pair<String, Integer> keyPair = new ImmutablePair(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, i);
        //keyPair = ComponentMLAggregator.getRealKeys(keyPair, profitdata.getInputdata().getConfMap().keySet());
        //System.out.println(okListMap.get(keys));
        Double confidenceFactor = 1.0; // reimplement? profitdata.getInputdata().getConfMap().get(keyPair);
        //System.out.println(okConfMap.keySet());
        //System.out.println(okListMap.keySet());
        Map maps = (Map) resultMaps; //.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Map<String, Map> resultMap0 = (Map<String, Map>) maps.get(PipelineConstants.RESULT);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) resultMap0.get(RecommendConstants.COMPLEX);
        if (resultMap == null) {
            return;
        }
        List<MyElement> list0 = new ArrayList<>();
        //List<MyElement> list1 = new ArrayList<>();
        Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
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
        try {
        handleBuySell(profitdata, (ComponentData) param, keyPair, confidenceFactor, list0, parameters);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list1);
        }
    }

    private void handleBuySell(ProfitData profitdata, ComponentData param, Pair<String, Integer> keys, Double confidenceFactor, List<MyElement> list, Parameters parameters) {
        int listSize = list.size();
        int recommend = param.getConfig().recommendTopBottom();
        if (listSize < recommend * 3) {
            return;
        }
        /*
        for (Triple<String, String, String> key : profitdata.getInputdata().getConfMap().keySet()) {
            try {
                Object keyone = key.getRight();
                String keyonetext = "";
                if (keyone != null) {
                    keyonetext = "" + key.getMiddle() + " " + key.getRight();
                }
                System.out.println("e " + ((String)key.getLeft()) + " " + keyonetext);
            } catch (Exception e) {
                log.error("grr" + key);
            }
        }
        */
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
            IncDecItem incdec = mapAdder(profitdata.getBuys(), element.getKey(), confidence, profitdata.getInputdata().getNameMap(), param.getService().conf.getConfigData().getDate(), param.getService().conf.getConfigData().getMarket(), null, "" + keys.getRight(), JsonUtil.convert(parameters));
            if (incdec != null) {
            incdec.setIncrease(true);
            }
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
            IncDecItem incdec = mapAdder(profitdata.getSells(), element.getKey(), confidence, profitdata.getInputdata().getNameMap(), param.getService().conf.getConfigData().getDate(), param.getService().conf.getConfigData().getMarket(), null, "" + keys.getRight(), JsonUtil.convert(parameters));
            if (incdec != null) {
            incdec.setIncrease(false);
            }
        }
    }

    private IncDecItem getIncDec(MyElement element, double confidence, String recommendation, Map<String, String> nameMap, String market) {
        IncDecItem incdec = new IncDecItem();
        incdec.setRecord(LocalDate.now());
        incdec.setId(element.getKey());
        incdec.setMarket(market);
        incdec.setDescription(recommendation);
        incdec.setName(nameMap.get(element.getKey()));
        //incdec.setParameters(null);
        incdec.setScore(confidence);
        return incdec;
    }

    public List<String> getBuy() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMACDNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMACDDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTSIGNALNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTSIGNALDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSIDELTANODE);
        return buyList;
    }
    
    public List<String> getSell() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMACDNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMACDDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTSIGNALNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTSIGNALDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSIDELTANODE);
        return sellList;
    }
    
    @Override
    public List<String> getConfList() {
        return getBuy();
    }
    
    public List<String> getConfListThreeBuy() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCIBUYWEIGHTCCINODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCIBUYWEIGHTCCIDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHBUYWEIGHTSTOCHNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHBUYWEIGHTSTOCHDELTANODE);
        return list;
    }
    
    public List<String> getConfListThreeSell() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCISELLWEIGHTCCINODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCISELLWEIGHTCCIDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHSELLWEIGHTSTOCHNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHSELLWEIGHTSTOCHDELTANODE);
        return list;
    }
    
    @Deprecated
    private boolean anythingHere(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size == Constants.OHLC && size != array.get(0).size()) {
                return false;
            }
            for (int i = 0; i < array.get(0).size(); i++) {
                if (array.get(0).get(i) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ComponentData improve(MarketActionData action, ComponentData componentparam, Market market, ProfitData profitdata, Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests, Fitness fitness, boolean save) {
	ComponentData param = new ComponentData(componentparam);
        //Map<String, String> retMap = new HashMap<>();
        //List<String> list = getBuy();
	buy = true;
        List<String> confList = buy ? getBuy() : getSell();      
	Map<String, List<List<Double>>> listMap = param.getCategoryValueMap();
	if (wantThree) {
	    confList.addAll(buy ? getConfListThreeBuy() : getConfListThreeSell());
	}
        Map<String, Object> map = null;
        try {
            map = new MiscUtil().loadConfig(componentparam.getService(), componentparam.getInput(), market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent, action, parameters);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (map != null) {
            String configStr = (String) map.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
            if (configStr != null) {
                String[] confArray = JsonUtil.convert(configStr, String[].class);
                if (confArray != null) {
                    confList = Arrays.asList(confArray);
                }
                //List<String> confList2 = JsonUtil.convert(str, new TypeReference<List<String>>() { });
                //config = JsonUtil.convert(configStr, new TypeReference<List<String>>() { });
                //config = JsonUtil.convert(configStr, new TypeReference<String[]>() { });
                //if (config == null) {
                //    config = null; //new ArrayList<>();
                //}
            }
        }

        ConfigMapGene gene = new ConfigMapGene(confList, param.getService().conf);
        RecommenderChromosome2 chromosome = new RecommenderChromosome2(gene);

        //chromosome.setConfList(confList);

        return improve(action, param, chromosome, subcomponent, new ConfigMapChromosomeWinner(), chromosome.getBuy(), fitness, save);
        //return handleBuySell(param, market, profitdata, profitdata.getInputdata().getListMap(), list);
        //list = getSell();
        //retMap.putAll(handleBuySell(param, market, profitdata, conf, profitdata.getInputdata().getListMap(), list));
        //return retMap;
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
    
    public static List<List<String>> getAllPerms(List<String> list) {
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
    
    @Override
    public List<MemoryItem> calculateMemory(MarketActionData actionData, ComponentData componentparam, Parameters parameters) throws Exception {
        RecommenderData param = (RecommenderData) componentparam;
        List<MemoryItem> memoryList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            AbstractScore eval = new ProportionScore(i == 0);
            getMemories(param, memoryList, eval, i, parameters, actionData);
        }
        return memoryList;
    }

    @Deprecated
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

    public void getMemories(RecommenderData param, List<MemoryItem> memoryList, AbstractScore eval, int position, Parameters parameters, MarketActionData actionData) throws Exception {
        Map<String, List<Double>> resultMap = new HashMap<>();
        for (String key : param.getCategoryValueMap().keySet()) {
            if (param.getRecommendBuySell() == null) {
                int jj = 0;
            }
            List<Double> vals = param.getRecommendBuySell().get(key);
            if (vals == null) {
                continue;
            }
            if (! (vals.get(position) instanceof Double)) {
                int jj = 0;
                Map map = param.getRecommendBuySell();
                List<String> objs = (List<String>) map.get(key);
                if ("NaN".equals(objs.get(position))) {
                    continue;
                }
            }
            Double score = vals.get(position);
            List<List<Double>> resultList = param.getCategoryValueMap().get(key);
            List<Double> mainList = resultList.get(0);
            Double change = null;
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - parameters.getFuturedays());
                if (valFuture != null && valNow != null) {
                    if (valNow == 0.0) {
                        log.error("Value for division is 0.0 for key {}", key);
                        continue;
                    }
                    boolean aboveThreshold = (valFuture / valNow) >= parameters.getThreshold();
                    change = valFuture / valNow;
                    List<Double> list = new ArrayList<>();
                    list.add(score);
                    list.add(change);
                    resultMap.put(key, list);
                }
            }
        }
        
        double[] resultArray = eval.calculate(resultMap, parameters.getThreshold());
        double goodBuy = resultArray[0];
        long totalBuy = (long) resultArray[1];
        MemoryItem memory = new MemoryItem();
        memory.setAction(param.getAction());
        memory.setMarket(param.getMarket());
        memory.setRecord(LocalDate.now());
        memory.setDate(param.getBaseDate());
        memory.setUsedsec(param.getUsedsec());
        memory.setFuturedays(param.getFuturedays());
        memory.setFuturedate(param.getFutureDate());
        memory.setComponent(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        memory.setSubcomponent(null);
        memory.setParameters(JsonUtil.convert(parameters));
        memory.setDescription("rec " + position + " " + eval.name());
        memory.setCategory(param.getCategoryTitle());
        memory.setPositives((long) goodBuy);
        memory.setSize(totalBuy);
        memory.setConfidence((double) goodBuy / totalBuy);
        //memory.setPosition(position);
        if (param.isDoSave()) {
            actionData.getDbDao().save(memory);
        }
        if (param.isDoPrint()) {
            System.out.println(memory);
        }
        memoryList.add(memory);
    }

    @Deprecated
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
            //actionData.getDbDao().save(buyMemory);
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
            //actionData.getDbDao().save(sellMemory);
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

    @Deprecated
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
            //actionData.getDbDao().save(buyMemory);
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
            //actionData.getDbDao().save(sellMemory);
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
    
    class BuySellList implements Comparable<Double> {
        String id;
        Double change;
        Double score;
        
        @Override
        public int compareTo(Double score0) {
            return Double.compare(score0, score);
        }
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
    }

    @Override
    public String getThreshold() {
        return ConfigConstants.EVOLVEINDICATORRECOMMENDERCOMPLEXTHRESHOLD;
    }

    @Override
    public String getFuturedays() {
        return ConfigConstants.EVOLVEINDICATORRECOMMENDERCOMPLEXFUTUREDAYS;
    }

}

