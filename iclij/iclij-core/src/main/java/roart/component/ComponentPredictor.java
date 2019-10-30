package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.chromosome.impl.MLMACDChromosome;
import roart.evolution.chromosome.impl.PredictorChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.gene.ml.impl.TensorflowPredictorLSTMConfigGene;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.PredictorService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class ComponentPredictor extends ComponentML {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.PREDICTORSLSTM, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGSPARKML, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOW, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, Boolean.FALSE);                
        valueMap.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, Boolean.FALSE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, Boolean.FALSE);        
    }

    public static MLConfigs getDisableNonLSTM() {
        EvolveMLConfig config = new EvolveMLConfig();
        config.setEnable(false);
        config.setEvolve(false);
        MLConfigs configs = new MLConfigs();
        configs.getTensorflow().setLir(config);
        configs.getTensorflow().setLic(config);
        configs.getTensorflow().setDnn(config);
        configs.getTensorflow().setMlp(config);
        configs.getTensorflow().setCnn(config);
        configs.getTensorflow().setRnn(config);
        configs.getTensorflow().setLstm(config);
        configs.getTensorflow().setGru(config);
        configs.getSpark().setLor(config);
        configs.getSpark().setMlpc(config);
        configs.getSpark().setOvr(config);
        configs.getSpark().setLsvc(config);
        configs.getPytorch().setMlp(config);
        configs.getPytorch().setCnn(config);
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
    public ComponentData handle(MarketAction action, Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent) {
        //log.info("Component not impl {}", this.getClass().getName());
        
        PredictorData param = new PredictorData(componentparam);
        
        String lstmConf = param.getService().conf.getTensorflowPredictorLSTMConfig();
        int futuredays = 0;
        try { 
            TensorflowPredictorLSTMConfig lstm = new ObjectMapper().readValue(lstmConf, TensorflowPredictorLSTMConfig.class);
            futuredays = lstm.getHorizon();
        } catch (Exception e) {
            log.error("Exception", e);
        }
        param.setFuturedays(futuredays);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent);
        
        Map<String, Object> maps = param.getResultMap();
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) maps.get(PipelineConstants.PROBABILITY);
        
        return param;
    }
    
    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, List<Integer> position) {
        PredictorData param = (PredictorData) componentparam;
        Object[] keys = new Object[2];
        keys[0] = PipelineConstants.PREDICTORSLSTM;
        keys[1] = null;
        keys = ComponentMLAggregator.getRealKeys(keys, profitdata.getInputdata().getConfMap().keySet());
        Double confidenceFactor = profitdata.getInputdata().getConfMap().get(keys);
        Map<String, Object> resultMap = (Map<String, Object>) param.getResultMap().get("result");
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
        handleBuySell(profitdata, param.getService(), param.getInput().getConfig(), keys, confidenceFactor, list0);
        //handleBuySell(nameMap, buys, sells, okListMap, srv, config, keys, confidenceFactor, list1);
    }
    
    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy, String subcomponent) {
	ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        PredictorChromosome chromosome = new PredictorChromosome(action, confList, param, profitdata, market, positions, PipelineConstants.PREDICTORSLSTM, buy, subcomponent);
        TensorflowPredictorLSTMConfig config = new TensorflowPredictorLSTMConfig(5, 5, 5);
        config.full = true;
        Map<String, Object> map = null;
        try {
            map = ServiceUtil.loadConfig(componentparam, market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (map != null) {
            String configStr = (String) map.get(ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTMCONFIG);
            if (configStr != null) {
                config = JsonUtil.convert(configStr, TensorflowPredictorLSTMConfig.class);
            }
        }
        TensorflowPredictorLSTMConfigGene gene = new TensorflowPredictorLSTMConfigGene(config);
        chromosome.setConfig(gene);
        return improve(action, param, chromosome, subcomponent);
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData componentparam) throws Exception {
        PredictorData param = (PredictorData) componentparam;
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) param.getResultMap().get("result");
        List<MemoryItem> memoryList = new ArrayList<>();
        long total = 0;
        long goodInc = 0;
        long goodDec = 0;
        long totalInc = 0;
        long totalDec = 0;
        for (String key : param.getCategoryValueMap().keySet()) {
            List<List<Double>> resultList = param.getCategoryValueMap().get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - param.getFuturedays());
                List<Double> predFutureList = resultMap.get(key);
                if (predFutureList == null) {
                    continue;
                }
                Double predFuture = predFutureList.get(0);
                if (valFuture != null && valNow != null && predFuture != null) {
                    System.out.println("vals " + valNow + " " + valFuture + " " + predFuture);
                    total++;
                    if (predFuture > valNow) {
                        totalInc++;
                        if (valFuture > valNow) {
                            goodInc++;
                        }
                    }
                    if (predFuture < valNow) {
                        totalDec++;
                        if (valFuture < valNow) {
                            goodDec++;
                        }
                    }
                }
            }
        }
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
        MemoryItem incMemory = new MemoryItem();
        incMemory.setMarket(param.getMarket());
        incMemory.setRecord(LocalDate.now());
        incMemory.setDate(param.getBaseDate());
        incMemory.setUsedsec(param.getUsedsec());
        incMemory.setFuturedays(param.getFuturedays());
        incMemory.setFuturedate(param.getFutureDate());
        incMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        incMemory.setSubcomponent("inc");
        incMemory.setCategory(param.getCategoryTitle());
        incMemory.setPositives(goodInc);
        incMemory.setSize(total);
        incMemory.setConfidence((double) goodInc / totalInc);
        if (param.isDoSave()) {
            incMemory.save();
        }
        MemoryItem decMemory = new MemoryItem();
        decMemory.setMarket(param.getMarket());
        decMemory.setRecord(LocalDate.now());
        decMemory.setDate(param.getBaseDate());
        decMemory.setUsedsec(param.getUsedsec());
        decMemory.setFuturedays(param.getFuturedays());
        decMemory.setFuturedate(param.getFutureDate());
        decMemory.setComponent(PipelineConstants.PREDICTORSLSTM);
        decMemory.setSubcomponent("dec");
        decMemory.setCategory(param.getCategoryTitle());
        decMemory.setPositives(goodDec);
        decMemory.setSize(total);
        decMemory.setConfidence((double) goodDec / totalDec);
        if (param.isDoSave()) {
            decMemory.save();
        }
        if (param.isDoPrint()) {
        System.out.println(incMemory);
        System.out.println(decMemory);
        }
        memoryList.add(incMemory);
        memoryList.add(decMemory);
        return memoryList;
    }
    
    private void handleBuySell(ProfitData profitdata, ControlService srv, IclijConfig config, Object[] keys,
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
            IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getBuys(), element.getKey(), confidence, profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(srv.conf.getdate()));
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
            IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getSells(), element.getKey(), confidence, profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(srv.conf.getdate()));
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
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        String localEvolve = componentdata.getInput().getConfig().getFindProfitPredictorEvolutionConfig();
        return JsonUtil.convert(localEvolve, EvolutionConfig.class);
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return componentdata.getInput().getConfig().getFindProfitPredictorMLConfig();
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return getDisableNonLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.PREDICTORSLSTM;
    }
    
    @Override
    public List<String> getConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTMCONFIG);
        return list;
    }

    @Override
    public List<String>[] enableDisable(ComponentData param, List<Integer> positions) {
        List[] list = new ArrayList[2];
        return list;
    }
}

