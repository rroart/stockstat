package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.fasterxml.jackson.core.type.TypeReference;

import roart.common.config.ConfigConstants;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.constants.RecommendConstants;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentInput;
import roart.component.model.PredictorData;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.common.pipeline.PipelineConstants;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.chromosome.impl.MLMACDChromosome;
import roart.evolution.chromosome.impl.RecommenderChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.AbstractScore;
import roart.evolution.fitness.impl.ProportionScore;
import roart.executor.MyExecutors;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.RecommenderService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

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
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.TRUE);                
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
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
    }

    @Override
    public ComponentData handle(Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) {
    
        RecommenderData param = new RecommenderData(componentparam);        
        
        int futuredays = (int) param.getService().conf.getTestIndicatorRecommenderComplexFutureDays();
        param.setFuturedays(futuredays);

        handle2(market, param, profitdata, positions, evolve && param.getInput().getConfig().wantEvolveRecommender(), aMap);

        Map<String, Object> resultMap = param.getResultMap();
        Map<String, Object> resultMap2 = (Map<String, Object>) resultMap.get(PipelineConstants.RESULT);
        Map<String, List<Double>> recommendBuySell = (Map<String, List<Double>>) resultMap2.get(RecommendConstants.COMPLEX);
        param.setRecommendBuySell(recommendBuySell);

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
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, List<Integer> position) {
        RecommenderData param = (RecommenderData) componentparam;
        //Map resultMaps = (Map) param.getResultMap(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, new HashMap<>());
        Map resultMaps = (Map) param.getResultMap();
        for (int i = 0; i < 2; i++) {
        Object[] keys = new Object[2];
        keys[0] = PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
        keys[1] = i;
        keys = ComponentMLMACD.getRealKeys(keys, profitdata.getInputdata().getConfMap().keySet());
        //System.out.println(okListMap.get(keys));
        Double confidenceFactor = profitdata.getInputdata().getConfMap().get(keys);
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
        handleBuySell(profitdata, (ComponentData) param, keys, confidenceFactor, list0);
        //handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list1);
        }
    }

    private void handleBuySell(ProfitData profitdata, ComponentData param, Object[] keys, Double confidenceFactor, List<MyElement> list) {
        int listSize = list.size();
        int recommend = param.getInput().getConfig().recommendTopBottom();
        if (listSize < recommend * 3) {
            return;
        }
        for (Object[] key : profitdata.getInputdata().getConfMap().keySet()) {
            try {
                Object keyone = key[1];
                String keyonetext = "";
                if (keyone != null) {
                    keyonetext = "" + (int)key[1];
                }
                System.out.println("e " + ((String)key[0]) + " " + keyonetext);
            } catch (Exception e) {
                log.error("grr" + key);
            }
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
            IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getBuys(), element.getKey(), confidence, profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
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
            IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getSells(), element.getKey(), confidence, profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
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
    
    private boolean anythingHere(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size == 3 && size != array.get(0).size()) {
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
    public ComponentData improve(ComponentData componentparam, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy) {
	ComponentData param = new ComponentData(componentparam);
        //Map<String, String> retMap = new HashMap<>();
        //List<String> list = getBuy();
        List<String> confList = buy ? getBuy() : getSell();      
	Map<String, List<List<Double>>> listMap = param.getCategoryValueMap();
	boolean gotThree = anythingHere(listMap, 3);
	if (gotThree) {
	    confList.addAll(buy ? getConfListThreeBuy() : getConfListThreeSell());
	}
        Map<String, Object> map = null;
        try {
            map = ServiceUtil.loadConfig(componentparam, market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy);
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

        RecommenderChromosome chromosome = new RecommenderChromosome(getConfList(), confList, param, profitdata, market, new ArrayList<>(), PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, buy);

        //chromosome.setConfList(confList);
        
        return improve(param, chromosome);
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
    
    @Deprecated
    private ComponentData handleBuySell(ComponentData param, Market market, ProfitData profitdata, Map<Object[], List<MemoryItem>> badListMap, List<String> list) {
        List<String> confList = getConfList();
        RecommenderChromosome chromosome = new RecommenderChromosome(null, confList, param, profitdata, market, new ArrayList<>(), PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null);
        if (true) return improve(param, chromosome);
        Map<String, String> retMap = new HashMap<>();
        List<List<String>> listPerm = getAllPerms(list);
        //List<List<String>> listPerm = disableAllButOne(list);
        String marketName = badListMap.values().iterator().next().get(0).getMarket();
        ControlService srv = new ControlService();
        srv.getConfig();            
        srv.conf.setMarket(marketName);
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
                Callable callable = new RecommenderCallable(srv, new ComponentInput(marketName, LocalDate.now(), 0, false, false), aList);  
                Future<List<MemoryItem>> future = MyExecutors.run(callable, 0);
                futureList.add(future);
                futureMap.put(future, aList);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            {
                boolean evolve = param.getInput().getConfig().wantEvolveRecommender();
                handle(market, param, profitdata, null, evolve, new HashMap());
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
        return param;
    }

    private Map<String, String> handleBuySellNot(ComponentData param, Market market, ProfitData profitdata, MyMyConfig conf, Map<Object[], List<MemoryItem>> badListMap, List<String> list) {
        Map<String, String> retMap = new HashMap<>();
        List<List<String>> listPerm = getAllPerms(list);
        //List<List<String>> listPerm = disableAllButOne(list);
        String marketName = badListMap.values().iterator().next().get(0).getMarket();
        ControlService srv = new ControlService();
        srv.getConfig();            
        srv.conf.setMarket(marketName);
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
                Callable callable = new RecommenderCallable(srv, new ComponentInput(marketName, LocalDate.now(), 0, false, false), aList);  
                Future<List<MemoryItem>> future = MyExecutors.run(callable, 0);
                futureList.add(future);
                futureMap.put(future, aList);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            {
                boolean evolve = param.getInput().getConfig().wantEvolveRecommender();
                handle(market, param, profitdata, null, evolve, new HashMap<>());
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
    public List<MemoryItem> calculateMemory(ComponentData componentparam) throws Exception {
        RecommenderData param = (RecommenderData) componentparam;
        List<MemoryItem> memoryList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            AbstractScore eval = new ProportionScore(i == 0);
            getMemories(param, memoryList, eval, i);
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

    public void getMemories(RecommenderData param, List<MemoryItem> memoryList, AbstractScore eval, int position) throws Exception {
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
                Double valNow = mainList.get(mainList.size() - 1 - param.getFuturedays());
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
        memory.setMarket(param.getMarket());
        memory.setRecord(LocalDate.now());
        memory.setDate(param.getBaseDate());
        memory.setUsedsec(param.getUsedsec());
        memory.setFuturedays(param.getFuturedays());
        memory.setFuturedate(param.getFutureDate());
        memory.setComponent(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        memory.setSubcomponent("rec " + position + " " + eval.name());
        memory.setCategory(param.getCategoryTitle());
        memory.setPositives((long) goodBuy);
        memory.setSize(totalBuy);
        memory.setConfidence((double) goodBuy / totalBuy);
        memory.setPosition(position);
        if (param.isDoSave()) {
            memory.save();
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
       
        private ComponentInput componentInput;
        
        private List<String> disableList;
        
        public RecommenderCallable(ControlService srv, ComponentInput componentInput, List<String> aList) {
            super();
            this.srv = srv;
            this.componentInput = componentInput;
            this.disableList = aList;
        }

        public ControlService getSrv() {
            return srv;
        }

        public void setSrv(ControlService srv) {
            this.srv = srv;
        }

        public List<String> getDisableList() {
            return disableList;
        }

        public void setDisableList(List<String> disableList) {
            this.disableList = disableList;
        }

        @Override
        public List<MemoryItem> call() throws Exception {
            
             return new RecommenderService().doRecommender(componentInput, disableList);
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
    public Map<String, EvolveMLConfig> getMLConfig(Market market, ComponentData componentdata) {
        return null;
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
    }

}

