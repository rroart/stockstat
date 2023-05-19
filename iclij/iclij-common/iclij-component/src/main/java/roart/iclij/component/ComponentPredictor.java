package roart.iclij.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.PredictorData;
import roart.evolution.fitness.Fitness;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.impl.PredictorChromosome;
import roart.iclij.evolution.chromosome.winner.ConfigMapChromosomeWinner;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.ControlService;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;

public class ComponentPredictor extends ComponentML {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.TRUE);                
        //.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, Boolean.TRUE);                
        //valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGSPARKML, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOW, Boolean.TRUE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, Boolean.TRUE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, Boolean.FALSE);                
        //valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, Boolean.FALSE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, Boolean.FALSE);        
    }

    @Deprecated
    private static MLConfigs getDisableNonLSTM() {
        EvolveMLConfig config = new EvolveMLConfig();
        config.setEnable(false);
        config.setEvolve(false);
        MLConfigs configs = new MLConfigs();
        configs.getTensorflow().setLir(config);
        configs.getTensorflow().setLic(config);
        configs.getTensorflow().setDnn(config);
        configs.getTensorflow().setMlp(config);
        configs.getTensorflow().setCnn(config);
        configs.getTensorflow().setCnn2(config);
        configs.getTensorflow().setRnn(config);
        configs.getTensorflow().setLstm(config);
        configs.getTensorflow().setGru(config);
        configs.getSpark().setLor(config);
        configs.getSpark().setMlpc(config);
        configs.getSpark().setOvr(config);
        configs.getSpark().setLsvc(config);
        configs.getPytorch().setMlp(config);
        configs.getPytorch().setCnn(config);
        configs.getPytorch().setCnn2(config);
        configs.getPytorch().setRnn(config);
        configs.getPytorch().setLstm(config);
        configs.getPytorch().setGru(config);
        configs.getGem().setS(config);
        configs.getGem().setI(config);
        configs.getGem().setEwc(config);
        configs.getGem().setGem(config);
        configs.getGem().setMm(config);
        return configs;
    }
    
    @Override
    public ComponentData handle(MarketActionData action, Market market, ComponentData componentparam, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters, boolean hasParent) {
        //log.info("Component not impl {}", this.getClass().getName());
        
        PredictorData param = new PredictorData(componentparam);
        
        int futuredays = (int) param.getService().conf.getPredictorsFuturedays();
        futuredays = 0;
        param.setFuturedays(futuredays);

        /*
        String lstmConf = param.getService().conf.getPredictorTensorflowLSTMConfig();
        int futuredays = 0;
        try { 
            TensorflowPredictorLSTMConfig lstm = new ObjectMapper().readValue(lstmConf, TensorflowPredictorLSTMConfig.class);
            futuredays = lstm.getHorizon();
        } catch (Exception e) {
            log.error("Exception", e);
        }
        param.setFuturedays(futuredays);
        */

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters, hasParent);
        
        Map<String, Object> maps = param.getResultMap();
        
        return param;
    }
    
    //@Override
    public void calculateIncDecNot(ComponentData componentparam, ProfitData profitdata, Memories position, Boolean above, List<MLMetricsItem> mlTests, Parameters parameters) {
        PredictorData param = (PredictorData) componentparam;
        Pair<String, Integer> keyPair = new ImmutablePair(PipelineConstants.PREDICTOR, null);
        //keyPair = ComponentMLAggregator.getRealKeys(keyPair, profitdata.getInputdata().getConfMap().keySet());
        Double confidenceFactor = 1.0; // never mind, will reimplement. profitdata.getInputdata().getConfMap().get(keyPair);
        Map<String, Object> resultMap = (Map<String, Object>) param.getResultMap().get("result");
        List<MyElement> list0 = new ArrayList<>();
        //List<MyElement> list1 = new ArrayList<>();
        List meta =  param.getResultMetaArray().get(0);
        String subcomponent = meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME);
        Map<String, List<List<Double>>> categoryValueMap = param.getCategoryValueMap();
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            String key = entry.getKey();
            List<List<Double>> resultList = entry.getValue();
            List<Double> mainList = resultList.get(0);
            if (mainList == null) {
                continue;
            }
            List<Object> list = (List<Object>) resultMap.get(key);
            if (list == null) {
                continue;
            }
            if (list.isEmpty()) {
                continue;
            }
            /*
            if (list0.isEmpty()) {
                continue;
            }
            */
            if (list.get(0) != null) {
                list0.add(new MyElement(key, (Double) list.get(0)));
            }
            //list1.add(new MyElement(key, list.get(1)));
        }
        Collections.sort(list0, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        //Collections.sort(list1, (o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        handleBuySell(profitdata, param.getService(), param.getInput().getConfig(), keyPair, confidenceFactor, list0, parameters, subcomponent);
        //handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list1);
    }
    
    @Override
    public ComponentData improve(MarketActionData action, ComponentData componentparam, Market market, ProfitData profitdata, Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests, Fitness fitness, boolean save) {
	ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        ConfigMapGene gene = new ConfigMapGene(confList, param.getService().conf);
        ConfigMapChromosome2 chromosome = new PredictorChromosome(gene);
        loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        return improve(action, param, chromosome, subcomponent, new ConfigMapChromosomeWinner(), buy, fitness, save);
    }

    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, Memories positions, Boolean above, List<MLMetricsItem> mlTests, Parameters parameters) {
        PredictorData param = (PredictorData) componentparam;
        if (positions == null) {
            //return;
        }
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        int resultIndex = 0;
        int count = 0;
        if (param.getResultMetaArray() == null) {
            int jj = 0;
        }
        for (List meta : param.getResultMetaArray()) {
            int returnSize = (int) meta.get(ResultMetaConstants.RETURNSIZE);

            boolean emptyMeta = meta.get(ResultMetaConstants.MLNAME) == null;
            
            if (emptyMeta) {
                resultIndex += returnSize;
                count++;                
            }
            
            if (positions == null) {
                int jj = 0;
            }
            
            MLMetricsItem mltest = search(mlTests, meta);
            if (mlTests == null || mltest != null) {
                //&& (positions == null || !positions.containsBelow(getPipeline(), paircount, above, mltest, param.getInput().getConfig().getFindProfitMemoryFilter()))) {
                Double score = mltest.getTestAccuracy();
                for (String key : param.getCategoryValueMap().keySet()) {
                    List<List<Double>> resultList = param.getCategoryValueMap().get(key);
                    List<Double> mainList = resultList.get(0);
                    if (mainList == null) {
                        continue;
                    }
                    List<Object> list = (List<Object>) aResultMap.get(key);
                    if (list == null) {
                        continue;
                    }
                    String tfpn = (String) list.get(resultIndex);
                    if (tfpn == null) {
                        continue;
                    }
                    boolean increase = false;
                    //System.out.println(okConfMap.keySet());
                    //Set<Pair<String, Integer>> keyset = profitdata.getInputdata().getConfMap().keySet();
                    //keyPair = ComponentMLAggregator.getRealKeys(keyPair, keyset);
                    //System.out.println(okListMap.keySet());
                    if (above == null || above == true) {
                    if (tfpn.equals(Constants.ABOVE)) {
                        increase = true;
                        //IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getBuys(), key, profitdata.getInputdata().getAboveConfMap().get(keyPair), profitdata.getInputdata().getAboveListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        IncDecItem incdec = mapAdder(profitdata.getBuys(), key, score, profitdata.getInputdata().getNameMap(), param.getBaseDate(), param.getInput().getMarket(), mltest.getSubcomponent(), mltest.getLocalcomponent(), JsonUtil.convert(parameters));
                        incdec.setIncrease(increase);
                    }
                    }
                    if (above == null || above == false) {
                    if (tfpn.equals(Constants.BELOW)) {
                        increase = false;
                        //IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getSells(), key, profitdata.getInputdata().getBelowConfMap().get(keyPair), profitdata.getInputdata().getBelowListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        IncDecItem incdec = mapAdder(profitdata.getSells(), key, score, profitdata.getInputdata().getNameMap(), param.getBaseDate(), param.getInput().getMarket(), mltest.getSubcomponent(), mltest.getLocalcomponent(), JsonUtil.convert(parameters));
                        incdec.setIncrease(increase);
                    }
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }
    }

    public List<MemoryItem> calculateMemory(MarketActionData actionData, ComponentData componentparam, Parameters parameters) throws Exception {
        PredictorData param = (PredictorData) componentparam;
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) param.getResultMap().get("result");
        List<MemoryItem> memoryList = new ArrayList<>();
        long total = 0;
        long goodInc = 0;
        long goodDec = 0;
        long totalInc = 0;
        long totalDec = 0;
        for (ResultMeta meta : param.getResultMeta()) {
            Double testloss = (Double) meta.getLoss();
            for (String key : param.getCategoryValueMap().keySet()) {
                List<List<Double>> resultList = param.getCategoryValueMap().get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList != null) {
                    Double valFuture = mainList.get(mainList.size() - 1);
                    Double valNow = mainList.get(mainList.size() - 1 - parameters.getFuturedays());
                    List<Double> predFutureList = resultMap.get(key);
                    if (predFutureList == null) {
                        continue;
                    }
                    Double predFuture = predFutureList.get(0);
                    if (valFuture != null && valNow != null && predFuture != null) {
                        System.out.println("vals " + valNow + " " + valFuture + " " + predFuture);
                        total++;
                        //boolean aboveThreshold = (predFuture / valNow) >= parameters.getThreshold();
                        if (predFuture > valNow * parameters.getThreshold()) {
                            totalInc++;
                            if (valFuture > valNow * parameters.getThreshold()) {
                                goodInc++;
                            }
                        }
                        if (predFuture < valNow * parameters.getThreshold()) {
                            totalDec++;
                            if (valFuture < valNow * parameters.getThreshold()) {
                                goodDec++;
                            }
                        }
                    }
                }
            }
            //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
            MemoryItem incMemory = new MemoryItem();
            incMemory.setAction(param.getAction());
            incMemory.setMarket(param.getMarket());
            incMemory.setRecord(LocalDate.now());
            incMemory.setDate(param.getBaseDate());
            incMemory.setUsedsec(param.getUsedsec());
            incMemory.setFuturedays(param.getFuturedays());
            incMemory.setFuturedate(param.getFutureDate());
            incMemory.setComponent(PipelineConstants.PREDICTOR);
            incMemory.setSubcomponent(meta.getMlName() + " " + meta.getModelName());
            incMemory.setTestloss(testloss);
            incMemory.setParameters(JsonUtil.convert(parameters));
            incMemory.setDescription("inc");
            incMemory.setCategory(param.getCategoryTitle());
            incMemory.setPositives(goodInc);
            incMemory.setSize(total);
            incMemory.setConfidence((double) goodInc / totalInc);
            if (param.isDoSave()) {
                actionData.getDbDao().save(incMemory);
            }
            MemoryItem decMemory = new MemoryItem();
            decMemory.setAction(param.getAction());
            decMemory.setMarket(param.getMarket());
            decMemory.setRecord(LocalDate.now());
            decMemory.setDate(param.getBaseDate());
            decMemory.setUsedsec(param.getUsedsec());
            decMemory.setFuturedays(param.getFuturedays());
            decMemory.setFuturedate(param.getFutureDate());
            decMemory.setComponent(PipelineConstants.PREDICTOR);
            decMemory.setSubcomponent(meta.getMlName() + " " + meta.getModelName());
            decMemory.setTestloss(testloss);
            decMemory.setParameters(JsonUtil.convert(parameters));
            decMemory.setDescription("dec");
            decMemory.setCategory(param.getCategoryTitle());
            decMemory.setPositives(goodDec);
            decMemory.setSize(total);
            decMemory.setConfidence((double) goodDec / totalDec);
            if (param.isDoSave()) {
                actionData.getDbDao().save(decMemory);
            }
            if (param.isDoPrint()) {
                System.out.println(incMemory);
                System.out.println(decMemory);
            }
            memoryList.add(incMemory);
            memoryList.add(decMemory);
        }
        return memoryList;
    }
    
    private void handleBuySell(ProfitData profitdata, ControlService srv, IclijConfig config, Pair<String, Integer> keys,
            Double confidenceFactor, List<MyElement> list, Parameters parameters, String subcomponent) {
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
            IncDecItem incdec = mapAdder(profitdata.getBuys(), element.getKey(), confidence, profitdata.getInputdata().getNameMap(), srv.conf.getConfigData().getDate(), srv.conf.getConfigData().getMarket(), subcomponent, null, JsonUtil.convert(parameters));
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
            IncDecItem incdec = mapAdder(profitdata.getSells(), element.getKey(), confidence, profitdata.getInputdata().getNameMap(), srv.conf.getConfigData().getDate(), srv.conf.getConfigData().getMarket(), subcomponent, null, JsonUtil.convert(parameters));
            incdec.setIncrease(false);
        }
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

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return getDisableNonLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.PREDICTOR;
    }
    
    @Override
    public List<String> getConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.MACHINELEARNINGPREDICTORSDAYS);
        list.add(ConfigConstants.MACHINELEARNINGPREDICTORSFUTUREDAYS);
        //list.add(ConfigConstants.MACHINELEARNINGPREDICTORSTHRESHOLD);
        return list;
    }

    @Override
    public List<String>[] enableDisable(ComponentData param, Memories positions, Boolean above) {
        return new ArrayList[] { new ArrayList<String>(), new ArrayList<String>() };
    }

    //@Override
    protected Map<String, String> getMlMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.TENSORFLOW, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(MLConstants.PYTORCH, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH);
        return map;
    }

    //@Override
    public Object[] calculateAccuracyNot(ComponentData componentparam) throws Exception {
        Object[] result = new Object[3];
        ComponentMLData param = (ComponentMLData) componentparam;
        List<Double> testAccuracies = new ArrayList<>();
        if (param.getResultMeta() == null) {
            return result;
        }
        for (ResultMeta meta : param.getResultMeta()) {
            // only different
            Double testaccuracy = meta.getLoss();
            if (testaccuracy != null) {
                testAccuracies.add(testaccuracy);
            }
        }
        // and min difference
        double acc = testAccuracies
                .stream()
                .mapToDouble(e -> e)
                .min()
                .orElse(-1);
        if (acc < 0) {
            return result;
        } else {
            result[0] = acc;
            if (testAccuracies.size() > 1) {
                result[1] = testAccuracies.stream().mapToDouble(e -> e).summaryStatistics().toString();
            }
            result[2] = null;
            return result;
        }
    }

    @Override
    public String getThreshold() {
        return ConfigConstants.MACHINELEARNINGPREDICTORSTHRESHOLD;
        //return "dummy";
    }

    @Override
    public String getFuturedays() {
        return ConfigConstants.MACHINELEARNINGPREDICTORSFUTUREDAYS;
    }
}

